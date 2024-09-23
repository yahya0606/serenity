package com.yahya.shadow;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback;
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private final String AD_UNIT_ID = "ca-app-pub-7662096701692256/7333121843"; // Replace with your ad unit ID

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {});

        // Load the App Open Ad
        loadAppOpenAd();
    }

    private void loadAppOpenAd() {
        if (isLoadingAd || appOpenAd != null) {
            return;
        }

        isLoadingAd = true;

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(this, AD_UNIT_ID, request, new AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(AppOpenAd ad) {
                appOpenAd = ad;
                isLoadingAd = false;
                Log.d(TAG, "App Open Ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                isLoadingAd = false;
                Log.d(TAG, "Failed to load App Open Ad: " + error.getMessage());

                // Reload the ad
                loadAppOpenAd();
            }
        });
    }

    public void showAdIfAvailable(Activity activity) {
        if (appOpenAd != null) {
            //appOpenAd.show(activity);
            Log.d(TAG, "Showing App Open Ad.");
            loadAppOpenAd(); // Load a new ad for future use
        } else {
            Toast.makeText(activity, "No ads available", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "App Open Ad is not loaded yet.");
            loadAppOpenAd(); // Optionally reload if needed
        }
    }
}
