package com.wolffincdevelopment.hiit_it.util;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by kylewolff on 6/29/2016.
 */
public class ConvertTimeUtils {

    public static String timeWithColon;
    public static DecimalFormat dfTwoPlaces = new DecimalFormat("#0.00");
    public static DecimalFormat dfNoPlaces = new DecimalFormat("00");

    public ConvertTimeUtils()
    {

    }

    public static String convertMilliSecToStringWithColon(long milliseconds)
    {
        if(milliseconds < 60000)
        {
            timeWithColon = addColonFormat( (double) TimeUnit.MILLISECONDS.toSeconds(milliseconds), "seconds");
        }
        else if(milliseconds >= 60000 && milliseconds < 3600000)
        {
            timeWithColon = addColonFormat(toMinutes(milliseconds), "minutes");
        }

        return timeWithColon;
    }

    private static double toMinutes(long milliseconds)
    {
        double seconds =  ((milliseconds / 1000) % 60) * .01;
        double minutes = ((milliseconds / (1000 * 60)) % 60);
        double time = minutes + seconds;

        return time;
    }

    /**
     * timeSubstring is the substring of 00:00
     *
     * time would be sec or min
     *
     * @param timeSubstring
     * @param time
     * @return
     */
    public static long getMilliSeconds(String timeSubstring, String time)
    {
        long seconds, minutes, milli = 0;

        if(time.compareTo("sec") == 0)
        {
            seconds = Long.parseLong(timeSubstring);
            milli = TimeUnit.SECONDS.toMillis(seconds);
        }
        else if(time.compareTo("min") == 0)
        {
            minutes = Long.parseLong(timeSubstring);
            milli = TimeUnit.MINUTES.toMillis(minutes);
        }

        return milli;
    }

    private static String addColonFormat(double time, String unitOfMeasure)
    {
        String zeros = "00";
        String colon = ":";
        String converted = "00:00";

        if(unitOfMeasure.matches("seconds"))
        {
            converted = zeros.concat(colon).concat(dfNoPlaces.format(time));
        }
        else if(unitOfMeasure.matches("minutes"))
        {
            String formattedTime = dfTwoPlaces.format(time);

            if(String.valueOf(time).matches("^([0-9]{1}).([0-9])*$"))
            {
                // Converting examples such as -> 1.20 , 3.2455555
                converted = ("0").concat(String.valueOf(formattedTime.charAt(0))).concat(colon).concat(formattedTime.substring(2,4));
            }
            else if(String.valueOf(time).matches("^([0-9]{2}).([0-9])*$"))
            {
                // Converting examples such as -> 20.5666666666666666 , 12.345
                converted = String.valueOf(formattedTime.substring(0,2)).concat(colon).concat(formattedTime.substring(3,5));
            }
        }

        return converted;
    }
}
