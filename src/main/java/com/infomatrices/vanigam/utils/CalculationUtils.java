/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

/**
 *
 * @author aravindhmuthuswamy
 */
public class CalculationUtils {

    private final CurrencyUtils currencyUtils = new CurrencyUtils();

    private Double calculateTotalRate(Double rate, Double quantity) {
        return rate * quantity;
    }

    private Double percentToNumber(Double percent) {
        return percent / 100;
    }

    public String formatValue(Double value) {
        return currencyUtils.formatCurrency(value);
    }

    public String formatValueInWords(Double value) {
        return currencyUtils.convertToIndianCurrencyWords(value);
    }

}
