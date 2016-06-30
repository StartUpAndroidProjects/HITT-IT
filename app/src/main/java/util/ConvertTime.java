package util;

import android.text.format.Formatter;
import android.util.TimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * Created by kylewolff on 6/29/2016.
 */
public class ConvertTime {

    private String timeWithColon;
    private DecimalFormat dfTwoPlaces, dfNoPlaces;


    public ConvertTime() {

        dfTwoPlaces = new DecimalFormat("#0.00");
        dfNoPlaces = new DecimalFormat("##");
    }

    public String convertMilliSecToStringWithColon(long milliseconds) {

        if(milliseconds < 60000) {
            timeWithColon = addColonFormat( (double) TimeUnit.MILLISECONDS.toSeconds(milliseconds), "seconds");
        } else if(milliseconds >= 60000 && milliseconds < 3600000) {
            timeWithColon = addColonFormat(toMinutes(milliseconds), "minutes");
        }

        return timeWithColon;
    }

    public double toMinutes(long milliseconds) {

        double seconds = milliseconds / 1000;
        double minutes = seconds / 60;

        return minutes;
    }

    public String addColonFormat(double time, String unitOfMeasure) {

        String zeros = "00";
        String colon = ":";
        String converted = "00:00";

        if(unitOfMeasure.matches("seconds")) {
            converted = zeros.concat(colon).concat(dfNoPlaces.format(time));
        } else if(unitOfMeasure.matches("minutes")) {

            String formattedTime = dfTwoPlaces.format(time);

            if(String.valueOf(time).matches("^([0-9]{1}).([0-9])*$")) {

                // Converting examples such as -> 1.20 , 3.2455555
                converted = ("0").concat(String.valueOf(formattedTime.charAt(0))).concat(colon).concat(formattedTime.substring(2,4));
            } else if(String.valueOf(time).matches("^([0-9]{2}).([0-9])*$")) {

                // Converting examples such as -> 20.5666666666666666 , 12.345
                converted = String.valueOf(formattedTime.substring(0,2)).concat(colon).concat(formattedTime.substring(3,5));
            }
        }

        return converted;
    }
}
