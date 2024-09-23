package com.yahya.shadow.UsersRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yahya.shadow.R;

import java.util.Objects;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView userName;
    public TextView userKey;
    public TextView mFollow;
    public CardView mCard;
    public ImageView mProfile;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        userName = itemView.findViewById(R.id.usernameItem);
        userKey = itemView.findViewById(R.id.userKeyItem);
        mFollow = itemView.findViewById(R.id.follow);
        mCard = itemView.findViewById(R.id.followCard);
        mProfile = itemView.findViewById(R.id.profilePic);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("following");
                ref.child(userKey.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            // user already followed
                            // unfollow
                            ref.child(userKey.getText().toString()).removeValue();
                            mFollow.setTextColor(R.color.black);
                            mFollow.setText("follow");
                            mCard.setElevation(10);

                        } else {
                            // User does not exist. NOW call createUserWithEmailAndPassword
                            // Your previous code here.
                            DatabaseReference current_user_db7 = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("following");
                            current_user_db7.child(userKey.getText().toString()).setValue("no");
                            DatabaseReference current_user_db8 = FirebaseDatabase.getInstance().getReference().child("users").child(userKey.getText().toString()).child("friend requests");
                            current_user_db8.child(user_id).setValue("no");
                            mFollow.setTextColor(R.color.purple_200);
                            mFollow.setText("followed");
                            mCard.setElevation(3);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });
    }

    @Override
    public void onClick(View v) {


    }
}
