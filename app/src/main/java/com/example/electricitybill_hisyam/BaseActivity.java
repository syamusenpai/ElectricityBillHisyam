package com.example.electricitybill_hisyam;

import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;

/** Shared setup: edge-to-edge insets and the branded toolbar (title bar). */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupScreen(int subtitleRes, boolean showBack) {
        EdgeToEdge.enable(this);
        View root = findViewById(R.id.main);
        View appBar = findViewById(R.id.appBar);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            // The teal app bar extends under the status bar; content stops above nav bar / keyboard.
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            appBar.setPadding(0, bars.top, 0, 0);
            return insets;
        });
        // White status bar icons over the dark toolbar colour.
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightStatusBars(false);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setSubtitle(subtitleRes);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
