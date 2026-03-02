/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

/**
 *
 * @author aravindhmuthuswamy
 */
public class NumberUtils {

    public static Double NullToZero(Double number) {
        if (number != null) {
            return number;
        }
        return 0D;
    }
}
