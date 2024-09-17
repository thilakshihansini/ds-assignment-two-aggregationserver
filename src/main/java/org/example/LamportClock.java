package org.example;

public class LamportClock {
    private int clock;

    public LamportClock() {
        this.clock = 0;
    }

    public synchronized int getClock() {
        return clock;
    }

    public synchronized void increment() {
        clock++;
    }

    public synchronized void update(int receivedClock) {
        clock = Math.max(clock, receivedClock) + 1;
    }
}
