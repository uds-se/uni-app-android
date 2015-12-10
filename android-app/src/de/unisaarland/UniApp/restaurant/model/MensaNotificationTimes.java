package de.unisaarland.UniApp.restaurant.model;

public class MensaNotificationTimes {
    /**
     * Each time is encoded in 12 bits:
     * 0     : is 0 if this time is disabled, 1 otherwise
     * 1..5  : encodes the hour (0..23)
     * 6..11 : encode the minute (0..59)
     *
     * All days (0..4  == Monday..Friday) are encoded in one long value, each day
     * d at position (12*d)..(12*d+11).
     * Bits 60..63 are always 1, so that we can use 0 as "uninitialized" value.
     */
    private long times;
    // default: disabled at 11:45
    public static final long DEFAULT_TIMES =
            //    v-  mon   -vv-  tue   -vv-  wed   -vv-  thu   -vv-  fri   -v
            0b1111101101010110101101010110101101010110101101010110101101010110L;

    public MensaNotificationTimes(long times) {
        this.times = times;
    }

    public int getHour(int day) {
        return (int)(times >> (12*day + 1)) & 0x1f;
    }

    public int getMinute(int day) {
        return (int)(times >> (12*day + 6)) & 0x3f;
    }

    public boolean isEnabled(int day) {
        return ((times >> 12*day) & 0x1) == 1;
    }

    public void setHour(int day, int hour) {
        if (day < 0 || day > 4 || hour < 0 || hour > 23)
            throw new IllegalArgumentException("day "+day + "; hour " + hour);
        long mask = ~(0x1fL << 12*day+1);
        times = (times & mask) | ((long)hour << 12*day+1);
    }

    public void setMinute(int day, int minute) {
        if (day < 0 || day > 4 || minute < 0 || minute > 59)
            throw new IllegalArgumentException("day "+day + "; minute " + minute);
        long mask = ~(0x3fL << 12*day+6);
        times = (times & mask) | ((long)minute << 12*day+6);
    }

    public void setEnabled(int day, boolean enabled) {
        if (day < 0 || day > 4)
            throw new IllegalArgumentException("day "+day);
        long mask = ~(1L << 12*day);
        times = (times & mask) | ((enabled ? 1L : 0L) << 12*day);
    }

    public long getRaw() {
        return times;
    }

    public void setRaw(long times) {
        this.times = times;
    }
}
