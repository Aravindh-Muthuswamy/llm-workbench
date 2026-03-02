/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils.csv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 *
 * @author aravindhmuthuswamy
 */
public class CsvFileUtils {
    public static String escapeSpecialCharacters(String data){
        if(data == null){
            throw new IllegalArgumentException("Input data cannot be null");
        }
        String escapedData = data.replaceAll("\\R", " ");
        if(data.contains(",") || data.contains("\"") || data.contains("'")){
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static String convertToCsv(String[] data){
        return Stream.of(data).map(CsvFileUtils::escapeSpecialCharacters).collect(Collectors.joining(","));
    }

    public static List<String[]> createCsvDataFileFromData(String[] header, List<String[]> data){
        List<String[]> dataToExport = new ArrayList<>();
        dataToExport.add(header);
        dataToExport.addAll(data);
        return dataToExport;
    }

    public static String getCsvFileName(String fileName){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh:mm");
        LocalDateTime localDateTime = LocalDateTime.now();
        return fileName + "-" + dateTimeFormatter.format(localDateTime) + ".csv";
    }
    
}
