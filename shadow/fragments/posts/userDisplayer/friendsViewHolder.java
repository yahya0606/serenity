package com.yahya.shadow.fragments.posts.userDisplayer;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yahya.shadow.R;


public class friendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView mProfile;
    public String Id;
    public friendsViewHolder(@NonNull View itemView) {
        super(itemView);
        mProfile = itemView.findViewById(R.id.friend_profile);

    }

    @Override
    public void onClick(View v) {

    }
}
