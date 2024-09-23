package com.yahya.shadow.MessageRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yahya.shadow.R;
import com.yahya.shadow.SenderProfileDialog;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgViewHolder> {
    public static final int My_Msg = 0;
    public static final int My_Successive_msg = 1;
    public static final int others_msg = 2;
    public static final int others_msg_same_sender = 3;

    private int actulaVT;
    private List<MsgObject> msgList;
    private Context context;
    private FirebaseAuth mAuth;
    private FragmentManager fragmentManager;

    public MsgAdapter (List<MsgObject> msgList, Context context, FragmentManager fragmentManager) {
        this.msgList = msgList;
        this.context = context;
        this.fragmentManager = fragmentManager;
        mAuth = FirebaseAuth.getInstance();
        actulaVT = 0;
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == My_Msg){
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_msg,null,false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);
            MsgViewHolder rcv = new MsgViewHolder(layoutView);
            return rcv;
        }else if (viewType == others_msg){
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.others_msg,null,false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);
            MsgViewHolder rcv = new MsgViewHolder(layoutView);
            return rcv;
        }else if (viewType == My_Successive_msg){
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_msg_successive,null,false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);
            MsgViewHolder rcv = new MsgViewHolder(layoutView);
            return rcv;
        }else {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.others_msg_same_sender,null,false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);
            MsgViewHolder rcv = new MsgViewHolder(layoutView);
            return rcv;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        holder.msg.setText(msgList.get(position).getMsg());

        if (actulaVT == 2){
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("users").child(msgList.get(position).getSender());
            ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("profile").exists()) {
                            String profile = snapshot.child("profile").getValue().toString();
                            Picasso.get().load(profile).into(holder.mSenderProfile);
                        } else {
                            holder.mSenderProfile.setImageResource(R.drawable.account);
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            holder.mSenderProfile.setOnClickListener(v -> {
                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child("user").exists()) {
                                String user = snapshot.child("user").getValue().toString();
                                if (snapshot.child("profile").exists()){
                                    String userPic = snapshot.child("profile").getValue().toString();
                                    SenderProfileDialog dialogFragment = SenderProfileDialog.newInstance(user,userPic,msgList.get(position).getSender());
                                    dialogFragment.show(fragmentManager, "profile_dialog");
                                }else{
                                    String userPic = "";
                                    SenderProfileDialog dialogFragment = SenderProfileDialog.newInstance(user,userPic,msgList.get(position).getSender());
                                    dialogFragment.show(fragmentManager, "profile_dialog");
                                }
                            }else{
                                String user = "No user found";
                            }
                            if (snapshot.child("profile").exists()){
                                if (snapshot.child("profile").exists()) {
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            });

        }
    }

    private void getUsername(String sender) {

    }

    @Override
    public int getItemViewType(int position) {
        if (msgList.get(position).getSender().equals(mAuth.getCurrentUser().getUid())){
            if (position+1<=msgList.size()-1 && msgList.get(position).getSender().equals(msgList.get(position+1).getSender())){
                actulaVT = 1;
                return My_Successive_msg;
            }else{
                actulaVT = 1;
                return My_Msg;
            }
        }else if (position+1<=msgList.size()-1 && msgList.get(position).getSender().equals(msgList.get(position+1).getSender())){
            actulaVT = 1;
            return others_msg_same_sender;
        }else {
            actulaVT = 2;
            return others_msg;
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }
}
