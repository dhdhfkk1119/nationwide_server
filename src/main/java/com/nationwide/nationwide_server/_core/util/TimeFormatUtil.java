package com.nationwide.nationwide_server._core.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatUtil {

    // =========================
    // ✅ TimeStamp 포맷
    // =========================

    public static String timestampFormat(Timestamp time){
        Date currentData = new Date(time.getTime());
        return DateFormatUtils.format(currentData,"yyyy년MM월dd HH:MM");
    }

    public static String timestampChatFormat(Timestamp time){
        Date currentData = new Date(time.getTime());
        return DateFormatUtils.format(currentData,"HH:MM");
    }

    // =========================
    // ✅ LocalDateTime 포맷
    // =========================

    // 일반 날짜 + 시간
    public static String localDateTimeFormat(LocalDateTime localDateTime) {
        if (localDateTime == null) return "";
        return localDateTime.format(
                DateTimeFormatter.ofPattern("yyyy년MM월dd HH:mm")
        );
    }

    // 채팅용 시간만
    public static String localDateTimeChatFormat(LocalDateTime localDateTime) {
        if (localDateTime == null) return "";
        return localDateTime.format(
                DateTimeFormatter.ofPattern("HH:mm")
        );
    }
}
