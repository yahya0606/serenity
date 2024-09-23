package com.yahya.shadow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CreditsActivity extends AppCompatActivity {

    private TextView creditsText;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        creditsText = findViewById(R.id.creditsText);
        closeButton = findViewById(R.id.closeButton);

        // Load the HTML content from string resource
        String htmlContent = getString(R.string.about_page_text);
        Spanned spanned = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY); // Use FROM_HTML_MODE_LEGACY for better compatibility
        creditsText.setText(spanned);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }
}