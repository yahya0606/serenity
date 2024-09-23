package com.yahya.shadow.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";

    public static OnboardingFragment newInstance(int layoutResId) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        int layoutResId = getArguments() != null ? getArguments().getInt(ARG_LAYOUT_RES_ID) : 0;
        return inflater.inflate(layoutResId, container, false);
    }
}