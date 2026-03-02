/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.infomatrices.vanigam;

import com.infomatrices.vanigam.dbdriver.SQLiteDatabaseCreator;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf;
import com.infomatrices.vanigam.config.HibernateConfig;
import com.infomatrices.vanigam.dashboard.DashboardForm;
import javax.swing.ImageIcon;

/**
 *
 * @author aravindhmuthuswamy
 */
public class LlmWorkbench {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        String loadingMessage = "Loading...";
        
        NewLoadingJFrame newLoadingJFrame = new NewLoadingJFrame();
        newLoadingJFrame.setVisible(true);
        newLoadingJFrame.loadingJLabel.setText("Loading UI...");
        newLoadingJFrame.jProgressBar.setValue(10);
        
        try {
                UIManager.setLookAndFeel(new FlatLightLaf());
                
            } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        System.setProperty( "apple.laf.useScreenMenuBar", "true" );
        System.setProperty( "apple.awt.application.appearance", "system" );
        newLoadingJFrame.loadingJLabel.setText("Initialising Database...");
        newLoadingJFrame.jProgressBar.setValue(20);
        SQLiteDatabaseCreator.createDatabaseOrCheckConnection(newLoadingJFrame.loadingJLabel, newLoadingJFrame.jProgressBar);
        DashboardForm dashboardForm = new DashboardForm();
        try{
            dashboardForm.setIconImage(new ImageIcon(dashboardForm.getClass().getResource("/icons/attachment.png")).getImage());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        newLoadingJFrame.jProgressBar.setValue(95);
        newLoadingJFrame.loadingJLabel.setText("Checking System config...");
//        newLoadingSplashJDialog.setVisible(false);
        HibernateConfig config = new HibernateConfig();
        newLoadingJFrame.jProgressBar.setValue(100);
        newLoadingJFrame.setVisible(false);
        dashboardForm.setVisible(true);
    }
}
