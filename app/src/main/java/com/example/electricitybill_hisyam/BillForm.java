package com.example.electricitybill_hisyam;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Calendar;

/**
 * The three inputs (month, units, rebate) used on both the calculator page and the
 * edit section of the detail page. Wraps the views from layout/bill_form.xml.
 */
public class BillForm {

    private final Context context;
    private final TextInputLayout monthLayout;
    private final AutoCompleteTextView monthInput;
    private final TextInputLayout unitsLayout;
    private final TextInputEditText unitsInput;
    private final Slider rebateSlider;
    private final TextView rebateValue;
    private Runnable onChange;

    public BillForm(View root) {
        context = root.getContext();
        monthLayout = root.findViewById(R.id.monthLayout);
        monthInput = root.findViewById(R.id.monthInput);
        unitsLayout = root.findViewById(R.id.unitsLayout);
        unitsInput = root.findViewById(R.id.unitsInput);
        rebateSlider = root.findViewById(R.id.rebateSlider);
        rebateValue = root.findViewById(R.id.rebateValue);

        monthInput.setAdapter(new NoFilterAdapter(context, BillCalculator.MONTHS));
        monthInput.setOnItemClickListener((parent, view, position, id) -> {
            monthLayout.setError(null);
            changed();
        });

        unitsInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                validateUnits(false);   // live feedback while typing
                changed();
            }
        });

        rebateSlider.setLabelFormatter(BillCalculator::formatPercent);
        rebateSlider.addOnChangeListener((slider, value, fromUser) -> {
            updateRebateLabel();
            changed();
        });
        updateRebateLabel();
    }

    /** Pre-selects the current calendar month. */
    public void selectCurrentMonth() {
        monthInput.setText(BillCalculator.MONTHS[Calendar.getInstance().get(Calendar.MONTH)], false);
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    public void setValues(Bill bill) {
        monthInput.setText(bill.month, false);
        unitsInput.setText(String.valueOf(bill.units));
        rebateSlider.setValue((float) bill.rebatePercent);
        updateRebateLabel();
    }

    public void reset() {
        selectCurrentMonth();
        unitsInput.setText(null);
        unitsLayout.setError(null);
        monthLayout.setError(null);
        rebateSlider.setValue(BillCalculator.MIN_REBATE);
    }

    /**
     * Validates every field, showing an error message under each bad one.
     * Returns the calculated bill, or null if any input is invalid.
     */
    public Bill validateAndCalculate() {
        String month = monthInput.getText().toString();
        boolean monthOk = BillCalculator.monthIndex(month) >= 0;
        monthLayout.setError(monthOk ? null : context.getString(R.string.error_month));

        Integer units = validateUnits(true);
        if (!monthOk) {
            monthInput.requestFocus();
            return null;
        }
        if (units == null) {
            unitsInput.requestFocus();
            return null;
        }
        return Bill.calculate(month, units, rebateSlider.getValue());
    }

    /** Same as validateAndCalculate but never changes error messages or focus (for live previews). */
    public Bill validateAndCalculateQuietly() {
        String month = monthInput.getText().toString();
        if (BillCalculator.monthIndex(month) < 0 || unitsInput.getText() == null) return null;
        try {
            int units = Integer.parseInt(unitsInput.getText().toString().trim());
            if (units < BillCalculator.MIN_UNITS || units > BillCalculator.MAX_UNITS) return null;
            return Bill.calculate(month, units, rebateSlider.getValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Returns the units if valid; shows/clears the error. When not strict, an empty field is not flagged. */
    private Integer validateUnits(boolean strict) {
        String text = unitsInput.getText() == null ? "" : unitsInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            unitsLayout.setError(strict ? context.getString(R.string.error_units_empty) : null);
            return null;
        }
        int units;
        try {
            units = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            unitsLayout.setError(context.getString(R.string.error_units_range));
            return null;
        }
        if (units < BillCalculator.MIN_UNITS || units > BillCalculator.MAX_UNITS) {
            unitsLayout.setError(context.getString(R.string.error_units_range));
            return null;
        }
        unitsLayout.setError(null);
        return units;
    }

    private void updateRebateLabel() {
        rebateValue.setText(BillCalculator.formatPercent(rebateSlider.getValue()));
    }

    private void changed() {
        if (onChange != null) onChange.run();
    }

    /** Dropdown adapter that always lists all 12 months (no filtering by the current text). */
    private static class NoFilterAdapter extends ArrayAdapter<String> {
        private final String[] items;

        NoFilterAdapter(Context context, String[] items) {
            super(context, R.layout.item_dropdown, Arrays.asList(items));
            this.items = items;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults r = new FilterResults();
                    r.values = items;
                    r.count = items.length;
                    return r;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
    }
}
