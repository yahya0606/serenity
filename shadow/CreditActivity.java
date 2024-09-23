package com.yahya.shadow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CreditActivity extends AppCompatActivity {

    private TextView creditText;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        creditText = findViewById(R.id.creditsText);
        closeButton = findViewById(R.id.closeButton);

        // Load privacy policy text with HTML formatting
        String privacyPolicyText = getString(R.string.privacy_policy_text);
        creditText.setText(Html.fromHtml(privacyPolicyText, Html.FROM_HTML_MODE_LEGACY));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }
}