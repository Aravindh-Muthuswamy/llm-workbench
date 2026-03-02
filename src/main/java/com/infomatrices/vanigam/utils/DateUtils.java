/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author aravindhmuthuswamy
 */
public class DateUtils {
    public static String getDateFormatted(String date) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.S");
        LocalDateTime localDateTime = LocalDateTime.parse(date, dtf);
        return localDateTime.getDayOfMonth() + "/" + localDateTime.getMonthValue() + "/" + localDateTime.getYear();
    }
    public static String getDateInIndianFormat(LocalDate date){
        return date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear();
    }
    public static String prefixBuilder(String prefixFromSetting) {
        int year = YearMonth.now().getYear();
        int month = YearMonth.now().getMonthValue();
        if(prefixFromSetting != null) {
            return prefixFromSetting + year + "/" + month + "/";
        }else{
            return "No/" + year + "/" + month + "/";
        }
    }
    public static String buildSalesOrPurchaseNumber(Integer count, String prefix) {
        return prefix + String.format("%05d", count + 1);
    }
}
