package com.yahya.shadow.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.yahya.shadow.R;
import com.yahya.shadow.WelcomeActivity;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String PREFS_KEY_FIRST_RUN = "first_run";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        Button skipButton = findViewById(R.id.skip_button);

        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        adapter.addFragment(OnboardingFragment.newInstance(R.layout.fragment_onboarding_page1));
        adapter.addFragment(OnboardingFragment.newInstance(R.layout.fragment_onboarding_page2));
        adapter.addFragment(OnboardingFragment.newInstance(R.layout.fragment_onboarding_page3));
        adapter.addFragment(OnboardingFragment.newInstance(R.layout.fragment_onboarding_page4));
        // Add more fragments as needed

        viewPager.setAdapter(adapter);
        skipButton.setText("Next");

        skipButton.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                checkPage(viewPager, adapter, skipButton);
            } else {
                finishOnboarding();
            }
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(PREFS_KEY_FIRST_RUN, true)) {
            // Show the onboarding screen
            finishOnboarding();
            //prefs.edit().putBoolean(PREFS_KEY_FIRST_RUN, false).apply();
        } else {
            // Skip onboarding if already completed
            finishOnboarding();
        }
    }

    private void checkPage(ViewPager2 viewPager, OnboardingPagerAdapter adapter, Button skipButton) {
        if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
            skipButton.setText("Next");
        } else {
            skipButton.setText("End");
        }
    }

    private void finishOnboarding() {
        Intent intent = new Intent(OnboardingActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}