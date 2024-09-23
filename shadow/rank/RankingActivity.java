package com.yahya.shadow.rank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.yahya.shadow.R;
import com.yahya.shadow.UsersRecyclerView.UserObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class RankingActivity extends AppCompatActivity {

    private RecyclerView.Adapter mRankAdapter;
    private RecyclerView.LayoutManager mRankLayoutManager;
    private Integer q;
    private SlidrInterface slidr;
    private float point1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        slidr = Slidr.attach(this);

        //Ranks recyclerView
        RecyclerView mRankRecyclerView = (RecyclerView) findViewById(R.id.rank_rv);
        mRankRecyclerView.setNestedScrollingEnabled(false);
        mRankRecyclerView.setHasFixedSize(true);
        mRankLayoutManager = new LinearLayoutManager(RankingActivity.this);
        mRankRecyclerView.setLayoutManager(mRankLayoutManager);
        mRankAdapter = new RankAdapter(getDataSetRanks(), RankingActivity.this);
        mRankRecyclerView.setAdapter(mRankAdapter);

        DatabaseReference reputation = FirebaseDatabase.getInstance().getReference().child("users");
        reputation.orderByChild("reputation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    q=1;
                    for (DataSnapshot users : snapshot.getChildren()){
                        String userID = users.getKey();
                        String Rank = String.valueOf(q);
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
                        user.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String Username = snapshot.getValue(String.class);
                                    user.child("reputation").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                float Points = snapshot.getValue(float.class);
                                                String p = String.format("%.1f", Points);
                                                if (Points<0){
                                                    point1 = -1 * Float.parseFloat(p);
                                                }else{
                                                    point1 = Float.parseFloat(p);
                                                }

                                                String ranked_pic_id = "likep.png";
                                                RankObject obj = new RankObject(Rank, Username, point1, ranked_pic_id, userID);
                                                resultsRanks.add(obj);
                                                Collections.sort(resultsRanks, new Comparator<RankObject>() {
                                                    @Override
                                                    public int compare(RankObject lhs, RankObject rhs) {
                                                        return Float.compare( rhs.getRankedPoints(),lhs.getRankedPoints());
                                                    }
                                                });
                                                mRankAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        q++;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private ArrayList resultsRanks = new ArrayList<RankObject>();
    private ArrayList<RankObject> getDataSetRanks() {
        return resultsRanks;
    }
}