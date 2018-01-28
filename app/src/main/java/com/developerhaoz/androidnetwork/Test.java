package com.developerhaoz.androidnetwork;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Haoz
 * @date 2018/1/28.
 */
public class Test implements Parcelable {

    private int age;
    private String name;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.age);
        dest.writeString(this.name);
    }

    protected Test(Parcel in) {
        this.age = in.readInt();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Test> CREATOR = new Parcelable.Creator<Test>() {
        @Override
        public Test createFromParcel(Parcel source) {
            return new Test(source);
        }

        @Override
        public Test[] newArray(int size) {
            return new Test[size];
        }
    };
}
