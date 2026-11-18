package com.example.electricitybill_hisyam;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;

/** Student information, app description, website link and full usage instructions. */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupScreen(R.string.subtitle_about, true);

        String url = getString(R.string.app_website_url);

        TextView tvUrl = findViewById(R.id.tvWebsite);
        tvUrl.setText(HtmlCompat.fromHtml("<a href=\"" + url + "\">" + url + "</a>",
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        tvUrl.setMovementMethod(LinkMovementMethod.getInstance());

        findViewById(R.id.btnOpenWebsite).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.notice_no_browser, Toast.LENGTH_SHORT).show();
            }
        });

        ((TextView) findViewById(R.id.tvInstructions)).setText(
                HtmlCompat.fromHtml(getString(R.string.about_instructions), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
}
