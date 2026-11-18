package com.example.electricitybill_hisyam;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.math.BigDecimal;

/** Checks the calculator against the sample calculations in the assignment brief. */
public class BillCalculatorTest {

    private static void assertTotal(String expected, int units) {
        assertEquals(0, new BigDecimal(expected).compareTo(BillCalculator.totalCharges(units)));
    }

    @Test public void sample150() { assertTotal("32.7", 150); }      // brief prints "RM 32" (150 x 0.218 = 32.70)
    @Test public void sample250() { assertTotal("60.3", 250); }
    @Test public void sample467() { assertTotal("163.172", 467); }
    @Test public void sample551() { assertTotal("206.516", 551); }
    @Test public void sample115() { assertTotal("25.07", 115); }

    @Test public void blockBoundaries() {
        assertTotal("0.218", 1);
        assertTotal("43.6", 200);
        assertTotal("77", 300);          // 43.6 + 33.4
        assertTotal("231.8", 600);       // 77 + 300 x 0.516
        assertTotal("450.2", 1000);      // 231.8 + 400 x 0.546
    }

    @Test public void rebateExampleFromBrief() {
        // RM32 - (32 x 0.05) = RM 30.40
        assertEquals(0, new BigDecimal("30.4")
                .compareTo(BillCalculator.finalCost(new BigDecimal("32"), 5)));
    }

    @Test public void zeroRebateLeavesTotalUnchanged() {
        assertEquals(0, new BigDecimal("163.172")
                .compareTo(BillCalculator.finalCost(new BigDecimal("163.172"), 0)));
    }

    @Test public void formatting() {
        assertEquals("RM 163.17", BillCalculator.formatRM(163.172));
        assertEquals("RM 1,000.00", BillCalculator.formatRM(1000));
        assertEquals("2.5%", BillCalculator.formatPercent(2.5));
        assertEquals("5%", BillCalculator.formatPercent(5));
    }
}
