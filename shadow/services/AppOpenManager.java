package com.yahya.shadow.services;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback;
import com.yahya.shadow.MyApp;

public class AppOpenManager implements Application.ActivityLifecycleCallbacks {
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final MyApp myApp;

    public AppOpenManager(MyApp myApp) {
        this.myApp = myApp;
        this.myApp.registerActivityLifecycleCallbacks(this);
        loadAd();
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        AppOpenAd.load(myApp, "YOUR_AD_UNIT_ID", adRequest,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        AppOpenManager.this.appOpenAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                    }
                });
    }

    public void showAdIfAvailable(final Activity activity) {
        if (appOpenAd != null) {
            appOpenAd.show(activity);
        } else {
            loadAd();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {
        showAdIfAvailable(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
