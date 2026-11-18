package com.example.electricitybill_hisyam;

import java.math.BigDecimal;

/** One saved bill estimate (a row in the bills table). */
public class Bill {
    public long id;
    public String month;
    public int units;
    public double totalCharges;
    public double rebatePercent;
    public double finalCost;

    public Bill(long id, String month, int units, double totalCharges,
                double rebatePercent, double finalCost) {
        this.id = id;
        this.month = month;
        this.units = units;
        this.totalCharges = totalCharges;
        this.rebatePercent = rebatePercent;
        this.finalCost = finalCost;
    }

    /** Builds a bill by running the calculator on the raw inputs. */
    public static Bill calculate(String month, int units, double rebatePercent) {
        BigDecimal total = BillCalculator.totalCharges(units);
        BigDecimal finalCost = BillCalculator.finalCost(total, rebatePercent);
        return new Bill(-1, month, units, total.doubleValue(), rebatePercent, finalCost.doubleValue());
    }
}
