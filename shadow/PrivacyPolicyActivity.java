package com.yahya.shadow;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private TextView privacyPolicyText;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        privacyPolicyText = findViewById(R.id.privacyPolicyText);
        closeButton = findViewById(R.id.closeButton);

        // Load privacy policy text with HTML formatting
        privacyPolicyText.setText(Html.fromHtml(getString(R.string.privacy_policy_text)));

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }
}
