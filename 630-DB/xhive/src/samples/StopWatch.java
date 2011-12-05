package samples;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive General
 *
 * [DESCRIPTION]
 * StopWatch is a convenience class that can be used to test performance of xhive functions.
 *
 */
public class StopWatch {

    long time = 0;
    long lastTime = 0;

    /*
    * Reset and start the stopwatch.
    */
    public void start() {
        lastTime = 0;
        time = System.currentTimeMillis();
    }

    /*
    * Save the time elapsed between start and last stop.
    */
    public void stop() {
        lastTime = check();
    }

    /*
    * Returns the time elapsed between start and the last stop
    */
    public long getLastTime() {
        return lastTime;
    }

    /*
    * Returns the time elapsed since start
    */
    public long check() {
        long thisTime = System.currentTimeMillis();
        return thisTime - time;
    }
}
