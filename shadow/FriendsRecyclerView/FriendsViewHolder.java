package com.yahya.shadow.FriendsRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.yahya.shadow.TextingActivity;

import java.util.Objects;

public class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView userName;
    public CardView mCard;

    public FriendsViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        userName = itemView.findViewById(R.id.chatRoom);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), TextingActivity.class);
        Bundle b = new Bundle();
        b.putString("chatRoom",userName.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);

    }
}
