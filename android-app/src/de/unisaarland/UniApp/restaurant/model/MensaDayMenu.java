package de.unisaarland.UniApp.restaurant.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;

public class MensaDayMenu implements Serializable, Parcelable, Comparable<MensaDayMenu> {

    private static final long serialVersionUID = -6465656406062632461L;

    private final long dayStartMillis;
    private final MensaItem[] items;

    public MensaDayMenu(long dayStartMillis, MensaItem[] items) {
        this.dayStartMillis = dayStartMillis;
        this.items = items;
    }

    public long getDayStartMillis() {
        return dayStartMillis;
    }

    public MensaItem[] getItems() {
        return items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dayStartMillis);
        dest.writeParcelableArray(items, 0);
    }

    public static final Parcelable.Creator<MensaDayMenu> CREATOR
            = new MensaDayMenu.Creator<MensaDayMenu>() {
        public MensaDayMenu createFromParcel(Parcel in) {
            return new MensaDayMenu(in.readLong(),
                    (MensaItem[]) in.readParcelableArray(MensaItem.class.getClassLoader()));
        }

        public MensaDayMenu[] newArray(int size) {
            return new MensaDayMenu[size];
        }
    };

    @Override
    public boolean equals(Object o0) {
        if (this == o0)
            return true;
        if (o0.getClass() != MensaDayMenu.class)
            return false;
        MensaDayMenu o = (MensaDayMenu) o0;
        if (o.dayStartMillis != dayStartMillis)
            return false;
        if (!Arrays.equals(o.items, items))
            return false;
        return true;
    }

    @Override
    public int compareTo(MensaDayMenu o) {
        if (this == o) // short path
            return 0;

        if (dayStartMillis != o.dayStartMillis)
            return dayStartMillis > o.dayStartMillis ? 1 : -1;

        int len = Math.max(items.length, o.items.length);
        for (int i = 0; i < len; ++i) {
            if (items.length <= i)
                return -1;
            if (o.items.length <= i)
                return 1;
            int cmp = items[i].compareTo(o.items[i]);
            if (cmp != 0)
                return cmp;
        }

        return 0;
    }

}
