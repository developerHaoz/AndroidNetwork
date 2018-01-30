package com.developerhaoz.androidnetwork.volley;

import android.text.TextUtils;

/**
 * @author Haoz
 * @date 2018/1/30.
 */
public class Header {
    private final String mName;
    private final String mValue;

    public Header(String name, String value){
        this.mName = name;
        this.mValue = value;
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        Header header = (Header) obj;

        return TextUtils.equals(mName, header.getName())
                && TextUtils.equals(mValue, header.getValue());
    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result += 31 * result + mValue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Header[name=]");
        sb.append(mName);
        sb.append(",value=");
        sb.append(mValue);
        sb.append("]");
        return sb.toString();
    }
}



















