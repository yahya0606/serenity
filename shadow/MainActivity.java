package com.yahya.shadow;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import android.Manifest;
import com.yahya.shadow.fragments.JournalFragment;
import com.yahya.shadow.fragments.UsersFragment;
import com.yahya.shadow.fragments.HomeFragment;
import com.yahya.shadow.fragments.ProfileFragment;
import com.yahya.shadow.services.ForegroundService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyApp myApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        tabLayout = findViewById(R.id.tabs);
        Bundle StalkPosition = getIntent().getExtras();
        viewPager = findViewById(R.id.views);
        tabLayout.setupWithViewPager(viewPager);

        myApp = (MyApp) getApplication();
        myApp.showAdIfAvailable(this);

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        int current = StalkPosition.getInt("current");

        viewPager.setCurrentItem(current);

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check if permissions are granted
        //    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        //            == PackageManager.PERMISSION_GRANTED &&
        //            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        //                    == PackageManager.PERMISSION_GRANTED) {

                // Start the foreground service
                //Intent intent = new Intent(this, ForegroundService.class);
                //ContextCompat.startForegroundService(this, intent);
        //    }
        //}



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume called.");
        myApp.showAdIfAvailable(this);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(1).setText("Serenity");
        tabLayout.setTabTextColors(Color.parseColor("#dfdfdf"), Color.parseColor("#ffffff"));
        tabLayout.getTabAt(0).setCustomView(R.layout.custom_view_message);
        tabLayout.getTabAt(2).setCustomView(R.layout.custom_journal);
        tabLayout.getTabAt(3).setCustomView(R.layout.custom_view_profile);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new UsersFragment(), "");
        adapter.addFrag(new HomeFragment(), "");
        adapter.addFrag(new JournalFragment(), "");
        adapter.addFrag(new ProfileFragment(), "");
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}