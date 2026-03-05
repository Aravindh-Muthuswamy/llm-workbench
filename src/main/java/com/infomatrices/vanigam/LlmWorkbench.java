/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.infomatrices.vanigam;

import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.UIManager;
import com.infomatrices.vanigam.config.HibernateConfig;
import com.infomatrices.vanigam.dashboard.DashboardForm;
import javax.swing.ImageIcon;

/**
 *
 * @author aravindhmuthuswamy
 */
public class LlmWorkbench {

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "LLM Workbench");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // Set dock/taskbar/app-switcher icon
        try {
            java.awt.Taskbar.getTaskbar().setIconImage(
                    new ImageIcon(LlmWorkbench.class.getResource("/icons/workbench.png")).getImage());
        } catch (Exception e) {
            System.out.println("Taskbar icon not supported: " + e.getMessage());
        }

        NewLoadingJFrame newLoadingJFrame = new NewLoadingJFrame();
        newLoadingJFrame.setVisible(true);
        newLoadingJFrame.loadingJLabel.setText("Loading UI...");
        newLoadingJFrame.jProgressBar.setValue(10);

        newLoadingJFrame.loadingJLabel.setText("Initialising Database...");
        newLoadingJFrame.jProgressBar.setValue(20);

        DashboardForm dashboardForm = new DashboardForm();

        try {
            dashboardForm.setIconImage(new ImageIcon(
                    dashboardForm.getClass().getResource("/icons/attachment.png")).getImage());
        } catch (Exception e) {
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
