package com.example.electricitybill_hisyam;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Shows one saved bill in full, and lets the user edit or delete it. */
public class DetailActivity extends BaseActivity {

    public static final String EXTRA_BILL_ID = "bill_id";

    private BillDbHelper db;
    private Bill bill;
    private BillForm form;
    private View viewSection, editSection;
    private TextView tvMonth, tvUnits, tvTotal, tvRebate, tvFinal, tvPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setupScreen(R.string.subtitle_detail, true);

        db = new BillDbHelper(this);
        bill = db.getById(getIntent().getLongExtra(EXTRA_BILL_ID, -1));
        if (bill == null) {
            Toast.makeText(this, R.string.notice_record_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewSection = findViewById(R.id.viewSection);
        editSection = findViewById(R.id.editSection);
        tvMonth = findViewById(R.id.tvDetailMonth);
        tvUnits = findViewById(R.id.tvDetailUnits);
        tvTotal = findViewById(R.id.tvDetailTotal);
        tvRebate = findViewById(R.id.tvDetailRebate);
        tvFinal = findViewById(R.id.tvDetailFinal);
        tvPreview = findViewById(R.id.tvPreview);

        form = new BillForm(findViewById(R.id.form));
        form.setOnChange(this::updatePreview);

        findViewById(R.id.btnEdit).setOnClickListener(v -> showEdit(true));
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());
        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> showEdit(false));
        findViewById(R.id.btnSaveEdit).setOnClickListener(v -> saveEdit());

        bindDetails();
        boolean editing = savedInstanceState != null && savedInstanceState.getBoolean("editing");
        showEdit(editing);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (editSection != null) outState.putBoolean("editing", editSection.getVisibility() == View.VISIBLE);
    }

    private void bindDetails() {
        tvMonth.setText(bill.month);
        tvUnits.setText(getString(R.string.units_value, bill.units));
        tvTotal.setText(BillCalculator.formatRM(bill.totalCharges));
        tvRebate.setText(BillCalculator.formatPercent(bill.rebatePercent));
        tvFinal.setText(BillCalculator.formatRM(bill.finalCost));
    }

    private void showEdit(boolean edit) {
        if (edit && editSection.getVisibility() != View.VISIBLE) {
            form.setValues(bill);
        }
        viewSection.setVisibility(edit ? View.GONE : View.VISIBLE);
        editSection.setVisibility(edit ? View.VISIBLE : View.GONE);
        if (edit) updatePreview();
    }

    /** Live preview of the recalculated amounts while editing. */
    private void updatePreview() {
        if (editSection.getVisibility() != View.VISIBLE) return;
        Bill preview = form.validateAndCalculateQuietly();
        if (preview == null) {
            tvPreview.setText(R.string.preview_invalid);
        } else {
            tvPreview.setText(getString(R.string.preview_value,
                    BillCalculator.formatRM(preview.totalCharges),
                    BillCalculator.formatRM(preview.finalCost)));
        }
    }

    private void saveEdit() {
        Bill updated = form.validateAndCalculate();
        if (updated == null) {
            Toast.makeText(this, R.string.notice_fix_errors, Toast.LENGTH_SHORT).show();
            return;
        }
        updated.id = bill.id;
        if (!db.update(updated)) {
            Toast.makeText(this, R.string.notice_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        bill = updated;
        bindDetails();
        showEdit(false);
        Toast.makeText(this, R.string.notice_updated, Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, bill.month,
                        BillCalculator.formatRM(bill.finalCost)))
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    db.delete(bill.id);
                    Toast.makeText(this, R.string.notice_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
