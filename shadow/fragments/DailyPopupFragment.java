package com.yahya.shadow.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;

import com.yahya.shadow.R;

import java.util.Random;

public class DailyPopupFragment extends DialogFragment {

    private static final int SPRINKLE_COUNT = 50; // Number of sprinkles to be animated
    private static final int SPRINKLE_DURATION = 3000; // Duration for each sprinkle animation

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_popup, container, false);


        // Add animation to the happyImage View
        View happyImage = view.findViewById(R.id.happyImage);
        Animation scaleAnimation = new ScaleAnimation(
                0.8f, 1.0f, 0.8f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(800);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        happyImage.startAnimation(scaleAnimation);

        // Add sprinkles animation
        FrameLayout rootLayout = (FrameLayout) view;
        addSprinkles(rootLayout);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }

    private void addSprinkles(final FrameLayout rootLayout) {
        final Handler handler = new Handler();
        final Random random = new Random();

        for (int i = 0; i < SPRINKLE_COUNT; i++) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    createAndAnimateSprinkle(rootLayout, random);
                }
            }, i * 200); // Delay each sprinkle start time slightly
        }
    }
    private void createAndAnimateSprinkle(FrameLayout rootLayout, Random random) {
        if (getContext() == null) {
            return;
        }
        final View sprinkle = new View(getContext());
        sprinkle.setBackgroundResource(R.drawable.sprinkle);

        int sprinkleSize = random.nextInt(40) + 20; // Random size between 20 and 60
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sprinkleSize, sprinkleSize);
        params.leftMargin = random.nextInt(rootLayout.getWidth() - sprinkleSize);
        params.topMargin = rootLayout.getHeight(); // Start from the bottom
        sprinkle.setLayoutParams(params);

        rootLayout.addView(sprinkle);

        // Create the upward and downward animations
        ObjectAnimator upAnimator = ObjectAnimator.ofFloat(sprinkle, "translationY", 0, -rootLayout.getHeight()*3 / 4f);
        upAnimator.setDuration(SPRINKLE_DURATION / 2);
        upAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator downAnimator = ObjectAnimator.ofFloat(sprinkle, "translationY", -rootLayout.getHeight()*3 / 4f, 0);
        downAnimator.setDuration(SPRINKLE_DURATION / 2);
        downAnimator.setInterpolator(new AccelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(upAnimator, downAnimator);
        animatorSet.start();

        animatorSet.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) { }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                rootLayout.removeView(sprinkle);
                dismiss();
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                rootLayout.removeView(sprinkle);
            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) { }
        });
    }
}