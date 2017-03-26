package com.wolffincdevelopment.hiit_it.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by kylewolff on 6/29/2016.
 */
public class ConvertTimeUtils {

    public ConvertTimeUtils() {
    }

    public static String convertMilliSecToString(long milliseconds) {

        String time = convertMilliSecToStringWithColonWithoutLeadingMinZero(milliseconds);
        String totalTime;

        if (time.length() != 2 && time.substring(0,2).contains("00")) {
            totalTime = time.substring(3, 5);
        } else {
            totalTime = time;
        }

        return totalTime;
    }

    public static String convertMilliSecToStringWithColonWithoutLeadingMinZero(long milliseconds) {

        String time = String.format(Locale.US,"%2d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

        if (time.length() != 2 && time.substring(0,2).contains(" 0")) {
            time = "";
            time = String.format(Locale.US,"%02d", TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        }

        return time;
    }

    public static String convertMilliSecToStringWithColon(long milliseconds) {
        return  String.format(Locale.US,"%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    /**
     * timeSubstring is the substring of 00:00
     * <p>
     * time would be sec or min
     *
     * @param timeSubstring
     * @param time
     * @return
     */
    public static long getMilliSeconds(String timeSubstring, String time) {
        long seconds, minutes, milli = 0;

        if (time.compareTo("sec") == 0) {
            seconds = Long.parseLong(timeSubstring);
            milli = TimeUnit.SECONDS.toMillis(seconds);
        } else if (time.compareTo("min") == 0) {
            minutes = Long.parseLong(timeSubstring);
            milli = TimeUnit.MINUTES.toMillis(minutes);
        }

        return milli;
    }
}
