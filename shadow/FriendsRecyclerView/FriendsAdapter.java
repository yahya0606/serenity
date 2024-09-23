package com.yahya.shadow.FriendsRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsViewHolder> {
    private List<UserObject> itemList;
    private Context context;
    private FirebaseAuth mAuth;

    public FriendsAdapter(List<UserObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_card,null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        FriendsViewHolder rcv = new FriendsViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        holder.userName.setText(itemList.get(position).getUserName());

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


}
