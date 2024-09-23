package com.yahya.shadow.rank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

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
import com.yahya.shadow.rank.RankViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankViewHolder> {

    private List<RankObject> rankList;
    private Context context;
    private FirebaseAuth mAuth;
    private RecyclerView.Adapter adapter;
    private PopupWindow popupWindow;
    private View popupView;
    private int i;
    private View relativeLayout;


    public RankAdapter (List<RankObject> rankList, Context context) {
        this.rankList = rankList;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        itemList.clear();
    }

    @NonNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_user,null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        RankViewHolder rcv = new RankViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder holder, int position) {

        holder.mUser.setText(rankList.get(position).getRankedUser());
        holder.mRank.setText(rankList.get(position).getRank());
        holder.mPoints.setText(String.valueOf(rankList.get(position).getRankedPoints()));

        //populating pic
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("users").child(rankList.get(position).getUserId());
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (snapshot.child("profile").exists()){
                        String profile = snapshot.child("profile").getValue().toString();
                        Picasso.get().load(profile).into(holder.mRankedProfile);
                    }else{
                        holder.mRankedProfile.setImageResource(R.drawable.account);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList itemList = new ArrayList<RankObject>();
    private ArrayList<RankObject> getDataSetRanks() {
        return itemList;
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }
}
