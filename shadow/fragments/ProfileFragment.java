package com.yahya.shadow.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.yahya.shadow.CheckUpsActivity;
import com.yahya.shadow.LoginActivity;
import com.yahya.shadow.R;
import com.yahya.shadow.SettingsActivity;

import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference mDatabase2,profileReference,mDatabase4,mDatabase5;

    private CircleImageView mProfile;
    private CardView mRateHistory,settings,donate;
    private TextView mRating;
    private CardView mSignOut;
    private static final String PREFS_NAME = "DailyGoalsPrefs";
    private static final String STREAK_COUNT_KEY = "StreakCount";
    private static final String TAG = "ProfileFragment";
    private RewardedAd rewardedAd;

    private PaymentsClient paymentsClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView mUsername = (TextView) view.findViewById(R.id.usertxt);
        mRating = view.findViewById(R.id.thanks);
        mRateHistory = view.findViewById(R.id.RateHistory);
        mProfile = view.findViewById(R.id.profile);
        mSignOut = view.findViewById(R.id.logout);
        settings = view.findViewById(R.id.editProfile);
        donate = view.findViewById(R.id.donate);
        //filling the blanks
        mAuth = FirebaseAuth.getInstance();
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        profileReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);

        loadRewardedAd();

        //profile
        profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() >0){
                    if (snapshot.hasChild("profile")){
                        String Image = snapshot.child("profile").getValue().toString();
                        Picasso.get().load(Image).into(mProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //username
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("user");
        mDatabase2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user = dataSnapshot.getValue(String.class);
                mUsername.setText(user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(),"Error user !",Toast.LENGTH_SHORT).show();
            }
        });

        mRateHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CheckUpsActivity.class);
                startActivity(intent);
            }
        });
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });
        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rewardedAd != null) {
                    //rewardedAd.show(requireActivity(), new OnUserEarnedRewardListener() {
                    //    @Override
                    //    public void onUserEarnedReward(@NonNull RewardItem reward) {
                            Toast.makeText(requireContext(), "Thank you for your donation!", Toast.LENGTH_SHORT).show();
                    //    }
                    //});
                } else {
                    Toast.makeText(requireContext(), "Ad is not loaded yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkAndResetGoals();

        return view;
    }

    private void loadRewardedAd() {
        RewardedAd.load(requireContext(), "ca-app-pub-7662096701692256/8702877982", new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                Log.d(TAG, "Rewarded ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
                Log.d(TAG, "Failed to load rewarded ad: " + loadAdError.getMessage());
            }
        });
    }

    private void checkAndResetGoals() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int streakCount = prefs.getInt(STREAK_COUNT_KEY, 0);

        mRating.setText(String.valueOf(streakCount));
    }
}