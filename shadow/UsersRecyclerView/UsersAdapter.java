package com.yahya.shadow.UsersRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yahya.shadow.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private List<UserObject> itemList;
    private Context context;
    private FirebaseAuth mAuth;

    public UsersAdapter(List<UserObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item,null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        UserViewHolder rcv = new UserViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.userName.setText(itemList.get(position).getUserName());
        holder.userKey.setText(itemList.get(position).getUserKey());

        //setting pics
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("users").child(holder.userKey.getText().toString());
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (snapshot.child("profile").exists()){
                        String profile = snapshot.child("profile").getValue().toString();
                        Picasso.get().load(profile).into(holder.mProfile);
                    }else{
                        holder.mProfile.setImageResource(R.drawable.account);
                    }
                    // user already followed
                    // unfollow

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //setting texts
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("following");
        ref.child(holder.userKey.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.mFollow.setText("unfollow");
                    holder.mCard.setElevation(3);
                    // user already followed
                    // unfollow
                    holder.mFollow.setTextColor(R.color.black);

                } else {
                    // User does not exist. NOW call createUserWithEmailAndPassword
                    // Your previous code here.
                    holder.mFollow.setText("follow");
                    holder.mFollow.setTextColor(R.color.black);
                    holder.mCard.setElevation(10);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
