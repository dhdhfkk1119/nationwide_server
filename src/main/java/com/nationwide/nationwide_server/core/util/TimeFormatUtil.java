package com.nationwide.nationwide_server.core.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.sql.Date;
import java.sql.Timestamp;

public class TimeFormatUtil {

    public static String timestampFormat(Timestamp time){
        Date currentData = new Date(time.getTime());
        return DateFormatUtils.format(currentData,"yyyy년MM월dd HH:MM");
    }

    public static String timestampChatFormat(Timestamp time){
        Date currentData = new Date(time.getTime());
        return DateFormatUtils.format(currentData,"HH:MM");
    }
}
