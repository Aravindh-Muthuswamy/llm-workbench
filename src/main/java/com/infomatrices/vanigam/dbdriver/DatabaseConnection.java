/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.dbdriver;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 *
 * @author aravindhmuthuswamy
 */
public class DatabaseConnection {

    public static Connection getConnection() {
        try {
            String databaseUrl = getDatabaseUrl();
            Connection conn = DriverManager.getConnection(databaseUrl);
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Connection cannot be established", e);
        }

    }
    

    public static String getDatabaseUrl() {
        try {
            Properties props = new Properties();
            InputStream inputStream = DatabaseConnection.class.getResourceAsStream("/application.properties");
//            System.out.println(inputStream.)
            props.load(inputStream);
            return props.getProperty("databaseUrl");
        } catch (Exception e) {
            throw new RuntimeException("No file found");
        }
    }
}
