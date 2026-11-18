package com.example.electricitybill_hisyam;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Lists saved bills (month and final cost only). Tap a row to open its detail page. */
public class HistoryActivity extends BaseActivity {

    private BillDbHelper db;
    private BillAdapter adapter;
    private TextView tvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setupScreen(R.string.subtitle_history, true);

        db = new BillDbHelper(this);
        adapter = new BillAdapter();
        tvHint = findViewById(R.id.tvHint);

        ListView listView = findViewById(R.id.listBills);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyView));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_BILL_ID, id);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time so edits/deletes made on the detail page show up.
        adapter.setBills(db.getAll());
        tvHint.setVisibility(adapter.getCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private class BillAdapter extends BaseAdapter {
        private List<Bill> bills = new ArrayList<>();

        void setBills(List<Bill> bills) {
            this.bills = bills;
            notifyDataSetChanged();
        }

        @Override public int getCount() { return bills.size(); }
        @Override public Bill getItem(int position) { return bills.get(position); }
        @Override public long getItemId(int position) { return bills.get(position).id; }
        @Override public boolean hasStableIds() { return true; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_bill, parent, false);
            }
            Bill bill = getItem(position);
            ((TextView) convertView.findViewById(R.id.tvMonth)).setText(bill.month);
            ((TextView) convertView.findViewById(R.id.tvFinalCost))
                    .setText(BillCalculator.formatRM(bill.finalCost));
            return convertView;
        }
    }
}
