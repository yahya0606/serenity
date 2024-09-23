package com.yahya.shadow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class QuestionFragment extends Fragment {

    private static final String ARG_QUESTION_TEXT = "arg_question_text";

    private String questionText;

    public static QuestionFragment newInstance(String questionText) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTION_TEXT, questionText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionText = getArguments().getString(ARG_QUESTION_TEXT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        TextView textViewQuestion = view.findViewById(R.id.questionText);
        textViewQuestion.setText(questionText);
        return view;
    }
}