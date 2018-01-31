package com.developerhaoz.androidnetwork.volley.toolbox;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.developerhaoz.androidnetwork.volley.Cache;
import com.developerhaoz.androidnetwork.volley.Header;
import com.developerhaoz.androidnetwork.volley.VolleyLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 不得不说，缓存这一块的思想真的很厉害，之前还一直不明白 Volley 为什么还要多弄一个 CacheHeader，
 * 抄了一遍代码总算明白了，CacheHeader 主要是为了保存有关 Entry 的一些轻量级的信息，然后将最重量级的
 * Entry.data 写入到文件中，各司其职，稳得不行。
 *
 * @author Haoz
 * @date 2018/1/30.
 */
public class DiskBasedCache implements Cache {

    private final Map<String, CacheHeader> mEntries =
            new LinkedHashMap<>(16, .75f, true);

    private long mTotalSize = 0;

    private final File mRootDirectory;

    private final int mMaxCacheSizeInBytes;

    private static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;

    /**
     * 表示内存占用达到 90% 开始扩容
     */
    private static final float HYSTERESIS_FACTOR = 0.9f;

    private static final int CACHE_MAGIC = 0x20150306;

    public DiskBasedCache(File rootDirectory, int maxCacheSizeInBytes) {
        mRootDirectory = rootDirectory;
        mMaxCacheSizeInBytes = maxCacheSizeInBytes;
    }

    public DiskBasedCache(File rootDirectory) {
        this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    }

    @Override
    public Entry get(String key) {
        CacheHeader entry = mEntries.get(key);

        if(entry == null){
            return null;
        }
        File file = getFileForKey(key);
        try{
            CountingInputStream cis = new CountingInputStream(
                    new BufferedInputStream(createInputStream(file)), file.length());
            try{
                CacheHeader entryOnDisk = CacheHeader.readHeader(cis);
                if(!TextUtils.equals(key, entryOnDisk.key)){
                    VolleyLog.d("%s: key=%s, found=%s",
                            file.getAbsolutePath(), key, entryOnDisk.key);
                    removeEntry(key);
                    return null;
                }
                byte[] data = streamToBytes(cis, cis.bytesRemaining());
                return entry.toCacheEntry(data);
            }finally {
                cis.close();
            }
        }catch (IOException e){
            VolleyLog.d("%s: %s", file.getAbsolutePath(), e.toString());
            remove(key);
            return null;
        }
    }

    @Override
    public synchronized void put(String key, Entry entry) {
        pruneIfNeeded(entry.data.length);
        File file = getFileForKey(key);
        try{
            BufferedOutputStream fos = new BufferedOutputStream(createOutputStream(file));
            CacheHeader e = new CacheHeader(key, entry);
            boolean success = e.writeHeader(fos);
            if(!success){
                fos.close();
                VolleyLog.d("Failed to write header for %s", file.getAbsolutePath());
                throw new IOException();
            }
            fos.write(entry.data);
            fos.close();
            putEntry(key, e);
            return;
        }catch (IOException e){
        }
        // 说实话不是很明白为什么直接就删了，查到比较靠谱的说法是「出现异常，就删除文件」
        // 如果真是这样的话，为啥不放在 catch 里面
        boolean deleted = file.delete();
        if(!deleted){
            VolleyLog.d("Could not clean up file %s", file.getAbsolutePath());
        }
    }

    @Override
    public void initialize() {
        if (!mRootDirectory.exists()) {
            if (!mRootDirectory.mkdirs()) {
                VolleyLog.e("Unable to create cache dir %s", mRootDirectory.getAbsolutePath());
            }
            return;
        }

        File[] files = mRootDirectory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                long entrySize = file.length();
                CountingInputStream cis = new CountingInputStream(
                        new BufferedInputStream(createInputStream(file)), entrySize);
                try {
                    CacheHeader entry = CacheHeader.readHeader(cis);
                    entry.size = entrySize;
                    putEntry(entry.key, entry);
                }finally {
                    cis.close();
                }
            } catch (IOException e) {
                file.delete();
            }
        }
    }

    @Override
    public void invalidate(String key, boolean fullExpire) {
        Entry entry = get(key);
        if(entry != null){
            entry.softTtl = 0;
            if(fullExpire){
                entry.ttl = 0;
            }
            put(key, entry);
        }
    }

    @Override
    public synchronized  void remove(String key) {
        boolean deleted = getFileForKey(key).delete();
        removeEntry(key);
        if(!deleted){
            VolleyLog.d("Could not delete cache entry for key=%s, filename=%s",
                    key, getFilenameForKey(key));
        }
    }

    @Override
    public synchronized void clear() {
        File[] files = mRootDirectory.listFiles();
        if(files != null){
            for (File file : files) {
                file.delete();
            }
        }
        mEntries.clear();
        mTotalSize = 0;
        VolleyLog.d("Cache cleared");
    }

    private String getFilenameForKey(String key){
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

    public File getFileForKey(String key){
        return new File(mRootDirectory, getFilenameForKey(key));
    }

    private void pruneIfNeeded(int neededSpace){
        if((mTotalSize + neededSpace) < mMaxCacheSizeInBytes){
            return;
        }

        if(VolleyLog.DEBUG){
            VolleyLog.v("Pruning old cache entries");
        }

        long before = mTotalSize;
        int prunedFiles = 0;
        long startTime = SystemClock.elapsedRealtime();

        Iterator<Map.Entry<String, CacheHeader>> iterator = mEntries.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, CacheHeader> entry = iterator.next();
            CacheHeader e = entry.getValue();
            boolean deleted = getFileForKey(e.key).delete();
            if(deleted){
                mTotalSize -= e.size;
            }else{
                VolleyLog.d("Could not delete cache entry for key=%s");
            }
            iterator.remove();
            prunedFiles++;

            if((mTotalSize + neededSpace) < mMaxCacheSizeInBytes * HYSTERESIS_FACTOR){
                break;
            }
        }

        if(VolleyLog.DEBUG){
            VolleyLog.v("pruned %d files, %d bytes, %d ms",
                    prunedFiles, before - mTotalSize, SystemClock.elapsedRealtime() - startTime);
        }
    }

    private void putEntry(String key, CacheHeader entry) {
        if (!mEntries.containsKey(key)) {
            mTotalSize += entry.size;
        } else {
            CacheHeader oldEntry = mEntries.get(key);
            mTotalSize += (entry.size - oldEntry.size);
        }
        mEntries.put(key, entry);
    }

    private void removeEntry(String key) {
        CacheHeader removed = mEntries.remove(key);
        if(removed != null){
            mTotalSize -= removed.size;
        }
    }

    static byte[] streamToBytes(CountingInputStream cis, long length) throws IOException {
        long maxLength = cis.bytesRemaining();
        if (length < 0 || length > maxLength || (int) length != length) {
            throw new IOException("streamToBytes length=" + length + ", maxLength=" + maxLength);
        }
        byte[] bytes = new byte[(int) length];
        new DataInputStream(cis).readFully(bytes);
        return bytes;
    }

    InputStream createInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    OutputStream createOutputStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    static class CacheHeader {

        long size;

        final String key;

        final String etag;

        final long serverDate;

        final long lastModified;

        final long ttl;

        final long softTtl;

        final List<Header> allResponseHeaders;

        private CacheHeader(String key, String etag, long serverDate, long lastModified, long ttl,
                            long softTtl, List<Header> allResponseHeaders) {
            this.key = key;
            this.etag = ("".equals(etag)) ? null : etag;
            this.serverDate = serverDate;
            this.lastModified = lastModified;
            this.ttl = ttl;
            this.softTtl = softTtl;
            this.allResponseHeaders = allResponseHeaders;
        }

        CacheHeader(String key, Entry entry) {
            this(key, entry.etag, entry.serverDate, entry.lastModified, entry.ttl,
                    entry.softTtl, entry.allResponseHeaders);
            size = entry.data.length;
        }

        private static List<Header> getAllResponseHeaders(Entry entry) {
            if (entry.allResponseHeaders != null) {
                return entry.allResponseHeaders;
            }
            return HttpHeaderParser.toAllHeaderList(entry.responseHeaders);
        }

        static CacheHeader readHeader(CountingInputStream is) throws IOException {
            int magic = readInt(is);
            if (magic != CACHE_MAGIC) {
                throw new IOException();
            }
            String key = readString(is);
            String etag = readString(is);
            long serverDate = readLong(is);
            long lastModified = readLong(is);
            long ttl = readLong(is);
            long softTtl = readLong(is);
            List<Header> allResponseHeaders = readHeaderList(is);
            return new CacheHeader(
                    key, etag, serverDate, lastModified, ttl, softTtl, allResponseHeaders);
        }

        Entry toCacheEntry(byte[] data) {
            Entry e = new Entry();
            e.data = data;
            e.etag = etag;
            e.serverDate = serverDate;
            e.lastModified = lastModified;
            e.ttl = ttl;
            e.softTtl = softTtl;
            e.responseHeaders = HttpHeaderParser.toHeaderMap(allResponseHeaders);
            e.allResponseHeaders = Collections.unmodifiableList(allResponseHeaders);
            return e;
        }

        boolean writeHeader(OutputStream os) {
            try {
                writeInt(os, CACHE_MAGIC);
                writeString(os, key);
                writeString(os, etag == null ? "" : etag);
                writeLong(os, serverDate);
                writeLong(os, lastModified);
                writeLong(os, ttl);
                writeLong(os, softTtl);
                writeHeaderList(allResponseHeaders, os);
                os.flush();
                return true;
            } catch (IOException e) {
                VolleyLog.d("%s", e.toString());
                return false;
            }
        }
    }

    /**
     * 一个具有记忆功能的 InputStream
     */
    static class CountingInputStream extends FilterInputStream {
        private final long length;
        private long bytesRead;

        CountingInputStream(InputStream in, long length) {
            super(in);
            this.length = length;
        }

        @Override
        public int read() throws IOException {
            int result = super.read();
            if (result != -1) {
                bytesRead++;
            }
            return result;
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);
            if (result != -1) {
                bytesRead += result;
            }
            return result;
        }

        long bytesRead() {
            return bytesRead;
        }

        long bytesRemaining() {
            return length - bytesRead;
        }
    }

    private static int read(InputStream is) throws IOException {
        int b = is.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

    /**
     * TODO: 说实话，不是很懂这些设计，难道是为了安全？
     */
    static void writeInt(OutputStream os, int n) throws IOException {
        os.write((n >> 0) & 0xff);
        os.write((n >> 8) & 0xff);
        os.write((n >> 16) & 0xff);
        os.write((n >> 24) & 0xff);
    }

    static int readInt(InputStream is) throws IOException {
        int n = 0;
        n |= (read(is) << 0);
        n |= (read(is) << 8);
        n |= (read(is) << 16);
        n |= (read(is) << 24);
        return n;
    }

    static void writeLong(OutputStream os, long n) throws IOException {
        os.write((byte) (n >> 0));
        os.write((byte) (n >> 8));
        os.write((byte) (n >> 16));
        os.write((byte) (n >> 24));
        os.write((byte) (n >> 32));
        os.write((byte) (n >> 40));
        os.write((byte) (n >> 48));
    }

    static long readLong(InputStream is) throws IOException {
        long n = 0;
        n |= ((read(is) & 0xFFL) << 0);
        n |= ((read(is) & 0xFFL) << 8);
        n |= ((read(is) & 0xFFL) << 16);
        n |= ((read(is) & 0xFFL) << 24);
        n |= ((read(is) & 0xFFL) << 32);
        n |= ((read(is) & 0xFFL) << 40);
        n |= ((read(is) & 0xFFL) << 48);
        n |= ((read(is) & 0xFFL) << 56);
        return n;
    }

    static void writeString(OutputStream os, String s) throws IOException {
        byte[] b = s.getBytes("UTF-8");
        writeLong(os, b.length);
        os.write(b, 0, b.length);
    }

    static String readString(CountingInputStream cis) throws IOException {
        long n = readLong(cis);
        byte[] b = streamToBytes(cis, n);
        return new String(b, "UTF-8");
    }

    static void writeHeaderList(List<Header> headers, OutputStream os) throws IOException {
        if (headers != null) {
            writeInt(os, headers.size());
            for (Header header : headers) {
                writeString(os, header.getName());
                writeString(os, header.getValue());
            }
        } else {
            writeInt(os, 0);
        }
    }

    static List<Header> readHeaderList(CountingInputStream cis) throws IOException {
        int size = readInt(cis);
        if (size < 0) {
            throw new IOException("readHeaderList size=" + size);
        }
        List<Header> result = (size == 0)
                ? Collections.<Header>emptyList()
                : new ArrayList<Header>();
        for (int i = 0; i < size; i++) {
            String name = readString(cis).intern();
            String value = readString(cis).intern();
            result.add(new Header(name, value));
        }
        return result;
    }
}























