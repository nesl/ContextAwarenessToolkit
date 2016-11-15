package edu.ucla.nesl.toolkit.executor.common.util;

/**
 * Created by cgshen on 11/15/16.
 */

public class SensorRate {
    public static final int NEXUS5X_ACC_UI = 50;
    public static final int NEXUS5X_ACC_NORMAL = 50;
    public static final int NEXUS5X_ACC_GAME = 50;
    public static final int NEXUS5X_ACC_FASTEST = 400;
    public static final int NEXUS5X_GRAV_UI = 25;
    public static final int NEXUS5X_GRAV_NORMAL = 12;
    public static final int NEXUS5X_GRAV_GAME = 50;
    public static final int NEXUS5X_GRAV_FASTEST = 200;

    private int freq;
    private int delay;

    public static SensorRate getDefaultSensorRate() {
        return new SensorRate(NEXUS5X_ACC_GAME, ((int) (1000.0 / NEXUS5X_ACC_GAME)));
    }

    public SensorRate(int freq, int delay) {
        this.freq = freq;
        this.delay = delay;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
