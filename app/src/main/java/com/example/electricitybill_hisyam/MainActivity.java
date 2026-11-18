package com.example.electricitybill_hisyam;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/** Calculator page: enter month, units and rebate, see the result on the same page, save it. */
public class MainActivity extends BaseActivity {

    private static final String STATE_SHOWING_RESULT = "showing_result";

    private BillForm form;
    private BillDbHelper db;
    private MaterialCardView resultCard;
    private TextView tvTotal, tvRebateAmount, tvFinal, tvBreakdown, tvResultMonth;
    private Button btnSave;
    private Bill current;   // last successful calculation, null when inputs changed since

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupScreen(R.string.subtitle_main, false);

        db = new BillDbHelper(this);
        form = new BillForm(findViewById(R.id.form));
        if (savedInstanceState == null) form.selectCurrentMonth();
        form.setOnChange(this::clearResult);

        resultCard = findViewById(R.id.resultCard);
        tvResultMonth = findViewById(R.id.tvResultMonth);
        tvTotal = findViewById(R.id.tvTotal);
        tvRebateAmount = findViewById(R.id.tvRebateAmount);
        tvFinal = findViewById(R.id.tvFinal);
        tvBreakdown = findViewById(R.id.tvBreakdown);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnCalculate).setOnClickListener(v -> calculate(true));
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            form.reset();
            clearResult();
        });
        btnSave.setOnClickListener(v -> save());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);
        if (state.getBoolean(STATE_SHOWING_RESULT)) calculate(false);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOWING_RESULT, current != null);
    }

    private void calculate(boolean userAction) {
        Bill bill = form.validateAndCalculate();
        if (bill == null) {
            if (userAction) {
                Toast.makeText(this, R.string.notice_fix_errors, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        current = bill;
        hideKeyboard();

        double rebateAmount = bill.totalCharges - bill.finalCost;
        tvResultMonth.setText(getString(R.string.result_heading, bill.month, bill.units));
        tvTotal.setText(BillCalculator.formatRM(bill.totalCharges));
        tvRebateAmount.setText(getString(R.string.rebate_amount,
                BillCalculator.formatPercent(bill.rebatePercent),
                BillCalculator.formatRM(rebateAmount)));
        tvFinal.setText(BillCalculator.formatRM(bill.finalCost));
        tvBreakdown.setText(TextUtils.join("\n", BillCalculator.breakdown(bill.units)));

        resultCard.setVisibility(View.VISIBLE);
        btnSave.setEnabled(true);
        if (userAction) {
            resultCard.post(() -> findViewById(R.id.scroll).scrollTo(0, resultCard.getTop()));
        }
    }

    private void clearResult() {
        current = null;
        resultCard.setVisibility(View.GONE);
        btnSave.setEnabled(false);
    }

    private void save() {
        if (current == null) return;
        if (db.monthExists(current.month, -1)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_duplicate_title)
                    .setMessage(getString(R.string.dialog_duplicate_message, current.month))
                    .setPositiveButton(R.string.action_save_anyway, (d, w) -> insertCurrent())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            insertCurrent();
        }
    }

    private void insertCurrent() {
        if (db.insert(current) == -1) {
            Toast.makeText(this, R.string.notice_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        btnSave.setEnabled(false);
        Snackbar.make(findViewById(R.id.main),
                        getString(R.string.notice_saved, current.month), Snackbar.LENGTH_LONG)
                .setAction(R.string.action_view, v -> startActivity(new Intent(this, HistoryActivity.class)))
                .show();
    }

    private void hideKeyboard() {
        View focus = getCurrentFocus();
        if (focus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            focus.clearFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_help) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.how_to_use)
                    .setMessage(R.string.help_dialog_text)
                    .setPositiveButton(R.string.action_got_it, null)
                    .show();
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
