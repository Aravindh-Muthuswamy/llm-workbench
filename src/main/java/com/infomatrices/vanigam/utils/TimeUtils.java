/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

/**
 *
 * @author aravindhmuthuswamy
 */
    import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
public class TimeUtils {


/**
 * Utility class for converting local date and time to IST (Indian Standard Time)
 * IST is UTC+5:30
 */

    
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    
    /**
     * Convert current system time to IST
     * @return ZonedDateTime in IST timezone
     */
    public static ZonedDateTime getCurrentIST() {
        return ZonedDateTime.now(IST_ZONE);
    }
    
    /**
     * Convert LocalDateTime to IST
     * Assumes the input is in system default timezone
     * @param localDateTime the local date time to convert
     * @return ZonedDateTime in IST timezone
     */
    public static ZonedDateTime toIST(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.withZoneSameInstant(IST_ZONE);
    }
    
    /**
     * Convert LocalDateTime from a specific timezone to IST
     * @param localDateTime the local date time to convert
     * @param sourceZoneId the timezone of the input datetime
     * @return ZonedDateTime in IST timezone
     */
    public static ZonedDateTime toIST(LocalDateTime localDateTime, ZoneId sourceZoneId) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(sourceZoneId);
        return zonedDateTime.withZoneSameInstant(IST_ZONE);
    }
    
    /**
     * Convert ZonedDateTime to IST
     * @param zonedDateTime the zoned date time to convert
     * @return ZonedDateTime in IST timezone
     */
    public static ZonedDateTime toIST(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(IST_ZONE);
    }
    
    /**
     * Convert Instant to IST
     * @param instant the instant to convert
     * @return ZonedDateTime in IST timezone
     */
    public static ZonedDateTime toIST(Instant instant) {
        return instant.atZone(IST_ZONE);
    }
    
    /**
     * Convert date-time string to IST
     * @param dateTimeStr the date-time string (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @param sourceZoneId the timezone of the input string
     * @return ZonedDateTime in IST timezone
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static ZonedDateTime toIST(String dateTimeStr, ZoneId sourceZoneId) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr);
        return toIST(localDateTime, sourceZoneId);
    }
    
    /**
     * Format IST datetime as string
     * @param zonedDateTime the zoned date time in IST
     * @return formatted string
     */
    public static String formatIST(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DEFAULT_FORMATTER);
    }
    
    /**
     * Format IST datetime with custom pattern
     * @param zonedDateTime the zoned date time in IST
     * @param pattern the pattern to format with
     * @return formatted string
     */
    public static String formatIST(ZonedDateTime zonedDateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return zonedDateTime.format(formatter);
    }
    
    /**
     * Get time difference between IST and another timezone in hours
     * @param otherZoneId the other timezone
     * @return difference in hours (positive if IST is ahead)
     */
    public static double getTimeDifference(ZoneId otherZoneId) {
        Instant now = Instant.now();
        ZonedDateTime istTime = now.atZone(IST_ZONE);
        ZonedDateTime otherTime = now.atZone(otherZoneId);
        
        long istOffset = istTime.getOffset().getTotalSeconds();
        long otherOffset = otherTime.getOffset().getTotalSeconds();
        
        return (istOffset - otherOffset) / 3600.0;
    }
}
