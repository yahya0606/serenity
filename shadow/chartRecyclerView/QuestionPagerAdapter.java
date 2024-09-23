package com.yahya.shadow.chartRecyclerView;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.yahya.shadow.QuestionFragment;
import com.yahya.shadow.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionPagerAdapter extends FragmentPagerAdapter {

    private final List<String> questions;

    public QuestionPagerAdapter(@NonNull FragmentManager fm, List<String> questions) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.questions = questions;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return QuestionFragment.newInstance(questions.get(position));
    }

    @Override
    public int getCount() {
        return questions.size();
    }
}