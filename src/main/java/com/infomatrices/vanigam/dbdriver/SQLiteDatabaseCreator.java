/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.dbdriver;

import com.infomatrices.vanigam.liquibase.DatabaseMigrator;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author aravindhmuthuswamy
 */

public class SQLiteDatabaseCreator {
    
    public static void createDatabaseOrCheckConnection(JLabel jLabel, JProgressBar jProgressBar){
        String url = "jdbc:sqlite:vanigam.db";
            String changelogFile = "db/changelog/initial-schema.sql"; // Path to your changelog
          
            
        

        try (var conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                // Get database metadata to confirm the connection and driver
                jLabel.setText("Checking Db Connection...");
                jProgressBar.setValue(50);
                var meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created or connected to.");
                jLabel.setText("Loading Database Configs...");
                jProgressBar.setValue(70);
                DatabaseMigrator.updateDatabase(DatabaseConnection.getDatabaseUrl(), "", "", changelogFile);
                jLabel.setText("Finished loading database configs");
                jProgressBar.setValue(90);
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to or creating the database: " + e.getMessage());
        }
    }
    
}
