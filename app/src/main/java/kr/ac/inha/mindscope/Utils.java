package kr.ac.inha.mindscope;

import android.util.Log;

public class Utils{
    public static long getThreadId()
    {
        Thread t = Thread.currentThread();
        return t.getId();
    }

    public static String getThreadSignature()
    {
        Thread t = Thread.currentThread();
        long l = t.getId();
        String name = t.getName();
        long p = t.getPriority();
        String gname = t.getThreadGroup().getName();
        return (name
                + ":(id)" + l
                + ":(priority)" + p
                + ":(group)" + gname);
    }

    public static void logThreadSignature(final String TAG)
    {
        Log.d(TAG + " ThreadUtils", getThreadSignature());
    }

    public static void sleepForInSecs(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException x) {
            throw new RuntimeException("interrupted", x);
        }
    }
}