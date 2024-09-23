package com.yahya.shadow.MessageRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yahya.shadow.R;

public class MsgViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView mSenderProfile;
    public TextView msg;
    public MsgViewHolder(@NonNull View itemView) {
        super(itemView);
        mSenderProfile = itemView.findViewById(R.id.friendPic);
        msg = itemView.findViewById(R.id.msg);

    }

    @Override
    public void onClick(View v) {

    }
}
