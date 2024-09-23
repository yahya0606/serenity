package com.yahya.shadow;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SenderProfileDialog extends DialogFragment {
    private static final String ARG_USERNAME = "username";
    private static final String ARG_PROFILE_PIC_URL = "profile_pic_url";
    private static final String ARG_CURRENT_USER = "current_user";
    private final DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users");

    public static SenderProfileDialog newInstance(String username, String profilePicUrl, String currentUser) {
        SenderProfileDialog fragment = new SenderProfileDialog();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_PROFILE_PIC_URL, profilePicUrl);
        args.putString(ARG_CURRENT_USER, currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sender_popup, container, false);

        ImageView profilePicture = view.findViewById(R.id.profile_picture);
        TextView usernameTextView = view.findViewById(R.id.username);
        Button blockButton = view.findViewById(R.id.block_button);
        Button reportButton = view.findViewById(R.id.report_button);
        Button likeButton = view.findViewById(R.id.like_button);
        TextView scoreText = view.findViewById(R.id.scoreText);

        if (getArguments() != null) {
            String username = getArguments().getString(ARG_USERNAME);
            String profilePicUrl = getArguments().getString(ARG_PROFILE_PIC_URL);
            String currentUser = getArguments().getString(ARG_CURRENT_USER);

            usernameTextView.setText(username);
            if (!profilePicUrl.isEmpty()){
                Picasso.get().load(profilePicUrl).into(profilePicture);
            }

            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser).child("counter");
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        scoreText.setText("Score : "+snapshot.getValue(Long.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            blockButton.setOnClickListener(v -> {
                boolean success = UserScoreManager.getInstance().blockUser(currentUser, username);
                if (success) {
                    // Handle successful block
                } else {
                    // Handle already blocked scenario
                }
                dismiss();
            });

            likeButton.setOnClickListener(v -> {
                UserScoreManager.getInstance().likeUser(currentUser, username);
                dismiss();
            });

            reportButton.setOnClickListener(v -> {
                // Handle report user action
                DatabaseReference db1 = FirebaseDatabase.getInstance().getReference().child("reports").child(ARG_CURRENT_USER);
                db1.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("reported");
                Toast.makeText(getContext(), "Thanks for making chats a safer place", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}