package com.aigo.analysis.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/6/9 10:33
 */
public class DateUtil {

    public static long getLocalUnixTimestamp() {
        long timestamp = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
            Date date = sdf.parse("1970-01-01 00:00:00");
            timestamp =  new Date().getTime() - date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return timestamp;
    }
}
