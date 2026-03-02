/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.liquibase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 *
 * @author aravindhmuthuswamy
 */
public class DatabaseMigrator {
    public static void updateDatabase(String jdbcUrl, String username, String password, String changelogFile) {
            Connection connection = null;
            Liquibase liquibase = null;
            try {
                connection = DriverManager.getConnection(jdbcUrl, username, password);
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                liquibase = new Liquibase(changelogFile, new ClassLoaderResourceAccessor(), database);
                liquibase.update("main"); // Apply all changesets with the context "main"
                System.out.println("Database updated successfully.");
            } catch (SQLException | LiquibaseException e) {
                System.err.println("Error updating database: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        // Log or handle the exception
                    }
                }
            }
        }

    
}
