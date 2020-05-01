package io.apef.base.utils;

public class ActiveChecker {
    private short maxIdleTimes;
    private short currentNo;
    private short lastAccessedNo;
    private short idleTimes;

    public ActiveChecker(short maxIdleTimes) {
        this.maxIdleTimes = maxIdleTimes;
    }

    public void update() {
        this.currentNo++;
        if (this.currentNo > 32760)
            this.currentNo = 0;
    }

    public boolean isActive() {
        if (lastAccessedNo != this.currentNo) {
            this.lastAccessedNo = this.currentNo;
            idleTimes = 0;
            return true;
        }
        if (++idleTimes > maxIdleTimes) {
            return false;
        }
        return true;
    }
}
