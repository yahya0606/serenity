package com.yahya.shadow.rank;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yahya.shadow.R;

public class RankViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView mUser,mRank,mPoints;
    public ImageView mRankedProfile;

    public RankViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mUser = itemView.findViewById(R.id.user_inRankList);
        mRank = itemView.findViewById(R.id.rank_inRankList);
        mPoints = itemView.findViewById(R.id.points_inRankList);
        mRankedProfile = itemView.findViewById(R.id.profilePic_inRankList);

    }

    @Override
    public void onClick(View view) {

    }
}
