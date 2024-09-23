package com.yahya.shadow.fragments.posts.userDisplayer;

import android.content.Context;
import android.view.Gravity;
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
import com.yahya.shadow.UsersRecyclerView.UserObject;
import com.yahya.shadow.UsersRecyclerView.UserViewHolder;

import java.util.List;

public class friendPicsAdapter extends RecyclerView.Adapter<friendsViewHolder>{

    private List<UserObject> itemList;
    private Context context;
    private FirebaseAuth mAuth;


    public friendPicsAdapter(List<UserObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }


    @NonNull
    @Override
    public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.followed_pic_item,null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //lp.width =(int) (parent.getWidth()*0.15);
        layoutView.setLayoutParams(lp);
        friendsViewHolder rcv = new friendsViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull friendsViewHolder holder1, int position) {
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("users").child(itemList.get(position).getUserKey());
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (snapshot.child("profile").exists()){
                        String profile = snapshot.child("profile").getValue().toString();
                        Picasso.get().load(profile).into(holder1.mProfile);
                    }else{
                        holder1.mProfile.setImageResource(R.drawable.account);
                    }
                    holder1.Id = itemList.get(position).getUserKey();

                    // user already followed
                    // unfollow

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
