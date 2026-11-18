package com.example.electricitybill_hisyam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Block-tariff electricity bill calculator.
 *
 * Block           Rate (sen/kWh)
 *   1 - 200          21.8
 * 201 - 300          33.4
 * 301 - 600          51.6
 * 601 - 1000         54.6
 *
 * BigDecimal is used so results match the hand calculations exactly
 * (for example 467 kWh = RM 163.172, not 163.17199999...).
 */
public final class BillCalculator {

    public static final int MIN_UNITS = 1;
    public static final int MAX_UNITS = 1000;
    public static final float MIN_REBATE = 0f;
    public static final float MAX_REBATE = 5f;

    /** Cumulative upper bound (kWh) of each block. */
    private static final int[] BLOCK_LIMITS = {200, 300, 600, MAX_UNITS};
    /** Rate of each block in RM per kWh. */
    private static final BigDecimal[] BLOCK_RATES = {
            new BigDecimal("0.218"),
            new BigDecimal("0.334"),
            new BigDecimal("0.516"),
            new BigDecimal("0.546")
    };

    public static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private BillCalculator() { }

    /** Total charges in RM, before rebate. */
    public static BigDecimal totalCharges(int units) {
        BigDecimal total = BigDecimal.ZERO;
        int lower = 0;
        for (int i = 0; i < BLOCK_LIMITS.length && units > lower; i++) {
            int inBlock = Math.min(units, BLOCK_LIMITS[i]) - lower;
            total = total.add(BLOCK_RATES[i].multiply(BigDecimal.valueOf(inBlock)));
            lower = BLOCK_LIMITS[i];
        }
        return total.stripTrailingZeros();
    }

    /** Final cost = total - (total * rebate%). */
    public static BigDecimal finalCost(BigDecimal total, double rebatePercent) {
        BigDecimal rebate = total.multiply(BigDecimal.valueOf(rebatePercent))
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return total.subtract(rebate).stripTrailingZeros();
    }

    /** One readable line per block used, e.g. "Block 1: 200 kWh x RM 0.218 = RM 43.60". */
    public static List<String> breakdown(int units) {
        List<String> lines = new ArrayList<>();
        int lower = 0;
        for (int i = 0; i < BLOCK_LIMITS.length && units > lower; i++) {
            int inBlock = Math.min(units, BLOCK_LIMITS[i]) - lower;
            BigDecimal charge = BLOCK_RATES[i].multiply(BigDecimal.valueOf(inBlock));
            lines.add(String.format(Locale.US, "Block %d: %d kWh × RM %s = %s",
                    i + 1, inBlock, BLOCK_RATES[i].toPlainString(), formatRM(charge.doubleValue())));
            lower = BLOCK_LIMITS[i];
        }
        return lines;
    }

    public static String formatRM(double value) {
        return String.format(Locale.US, "RM %,.2f", value);
    }

    public static String formatPercent(double value) {
        if (value == Math.floor(value)) {
            return String.format(Locale.US, "%d%%", (int) value);
        }
        return String.format(Locale.US, "%.1f%%", value);
    }

    public static int monthIndex(String month) {
        for (int i = 0; i < MONTHS.length; i++) {
            if (MONTHS[i].equals(month)) return i;
        }
        return -1;
    }
}
