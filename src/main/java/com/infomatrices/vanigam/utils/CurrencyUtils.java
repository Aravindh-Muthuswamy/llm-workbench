/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.utils;

import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;

/**
 *
 * @author aravindhmuthuswamy
 */
public class CurrencyUtils {
    private static final String[] units = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };
    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };
    private static final String[] largeNumbers = {
            "", "Thousand", "Lakh", "Crore"
    };
    public String convertToIndianCurrencyWords(double amount) {
        // Split into rupees and paise
        long rupees = (long) amount;
        int paise = (int) Math.round((amount - rupees) * 100);

        String rupeesInWords = convertToWords(rupees, paise > 0, false) + " Rupees";
        String paiseInWords = (paise > 0) ? " and " + convertToWords(paise, false, true) + " Paise" : "";

        return rupeesInWords + paiseInWords;
    }

    private static String convertToWords(long number, boolean paiseExists, boolean paiseValue) {
        if (number == 0) {
            return "Zero";
        }

        StringBuilder words = new StringBuilder();
        int index = 0;

        while (number > 0) {
            if (number % 1000 != 0) {
                words.insert(0, convertHundreds((int) (number % 1000), paiseExists, paiseValue) + " " + largeNumbers[index] + " ");
            }
            number /= (index == 1) ? 100 : 1000; // Use 100 for thousand, and 1000 for lakh/crore groups
            index++;
        }

        return words.toString().trim();
    }

    private static String convertHundreds(int number, boolean paiseExists, boolean paiseValue) {
        String word = "";
        
        if (number > 99) {
            word += units[number / 100] + " Hundred ";
            number %= 100;
        }
        
        if (number > 19) {
        	if(!paiseExists && !paiseValue) {
        		word += " and ";
        	}
            word += tens[number / 10] + " " + units[number % 10];
        } else {
            word += units[number];
        }
        
        return word.trim();
    }
    public String formatCurrency(Double value) {
		      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        // Create a DecimalFormat with no grouping to format the amount
        DecimalFormat formatter = new DecimalFormat("#0.00", symbols);
        String formattedAmount = formatter.format(value);

        // Apply custom Indian numbering format (lakh and crore)
        formattedAmount = applyIndianNumberingSystem(formattedAmount);

        // Prepend the currency symbol manually
        formattedAmount = "₹" + formattedAmount;

        return formattedAmount;
	}

	private static String applyIndianNumberingSystem(String amount) {
        // Split amount into integer and decimal parts
        String[] parts = amount.split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts.length > 1 ? "." + parts[1] : "";

        // Apply regex to format with lakhs and crores
        integerPart = integerPart.replaceAll("(\\d)(?=(\\d\\d)+\\d$)", "$1,");

        // Recombine integer and decimal parts
        return integerPart + decimalPart;
    }
}
