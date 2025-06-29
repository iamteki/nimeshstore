package com.nimesh.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting decimal values consistently throughout the application.
 */
public class DecimalFormatter {
    
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Formats a BigDecimal to a string with 2 decimal places
     * @param value The value to format
     * @return The formatted string
     */
    public static String format(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE).toString();
    }
    
    /**
     * Formats a BigDecimal as currency (with LKR prefix)
     * @param value The value to format
     * @return The formatted currency string
     */
    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "LKR 0.00";
        }
        return "LKR " + value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE).toString();
    }
    
    /**
     * Standardizes a BigDecimal value by setting it to 2 decimal places
     * @param value The value to standardize
     * @return The standardized BigDecimal with 2 decimal places
     */
    public static BigDecimal standardize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        }
        return value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
    
    /**
     * Safely parses a string to a BigDecimal with 2 decimal places
     * @param value The string to parse
     * @return The parsed BigDecimal or zero if the string cannot be parsed
     */
    public static BigDecimal parse(String value) {
        try {
            return new BigDecimal(value).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        } catch (NumberFormatException | NullPointerException e) {
            return BigDecimal.ZERO.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        }
    }
}