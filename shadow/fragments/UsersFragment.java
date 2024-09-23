package com.yahya.shadow.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yahya.shadow.FriendsRecyclerView.FriendsAdapter;
import com.yahya.shadow.R;
import com.yahya.shadow.UsersRecyclerView.UserObject;

import java.util.ArrayList;

public class UsersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView.Adapter mUsersAdapter;
    private RecyclerView.LayoutManager mUserLayoutManager;
    private String currentUserId;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseAuth mAuth;
    private TextView mNoFriends;
    private EditText mSearchBox;
    private ImageView mAdd;
    private ArrayList<UserObject> resultsUsers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        initViews(view);
        setupRecyclerView(view);
        setupSwipeRefreshLayout();
        setupSearchBox();

        getUserIds(); // Initial data fetch

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        resultsUsers.clear(); // Clear data to prevent duplicates
        getUserIds(); // Refresh data when fragment is resumed
    }

    private void initViews(View view) {
        mAdd = view.findViewById(R.id.add);
        mSearchBox = view.findViewById(R.id.searchBox);
        mNoFriends = view.findViewById(R.id.NoFriends);
        mSwipeRefreshLayout = view.findViewById(R.id.swiper);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
    }

    private void setupRecyclerView(View view) {
        RecyclerView mUsersRecyclerView = view.findViewById(R.id.users);
        mUsersRecyclerView.setNestedScrollingEnabled(false);
        mUsersRecyclerView.setHasFixedSize(true);
        mUserLayoutManager = new GridLayoutManager(getActivity(), 2);
        mUsersRecyclerView.setLayoutManager(mUserLayoutManager);
        mUsersAdapter = new FriendsAdapter(resultsUsers, getActivity());
        mUsersRecyclerView.setAdapter(mUsersAdapter);
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.purple_700,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    }

    private void setupSearchBox() {
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    mNoFriends.setVisibility(View.GONE);
                    resultsUsers.clear();
                    mUsersAdapter.notifyDataSetChanged();
                    filter(s.toString().trim());
                } else {
                    getUserIds();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filter(String userName) {
        DatabaseReference userIdDatabase = FirebaseDatabase.getInstance().getReference().child("chats");
        userIdDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resultsUsers.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot users : snapshot.getChildren()) {
                        if (users.getKey().toLowerCase().contains(userName.toLowerCase())) {
                            UserObject obj = new UserObject(users.getKey(), "", "");
                            resultsUsers.add(obj);
                        }
                    }
                    if (resultsUsers.isEmpty()) {
                        mNoFriends.setText("No Rooms with this name found !!");
                        mNoFriends.setVisibility(View.VISIBLE);
                    } else {
                        mNoFriends.setVisibility(View.GONE);
                    }
                    mUsersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onRefresh() {
        resultsUsers.clear();
        getUserIds();
    }

    private void getUserIds() {
        mSwipeRefreshLayout.setRefreshing(true);
        mNoFriends.setVisibility(View.GONE);
        resultsUsers.clear();

        DatabaseReference userIdDatabase = FirebaseDatabase.getInstance().getReference().child("chats");
        userIdDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resultsUsers.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot users : snapshot.getChildren()) {
                        UserObject obj = new UserObject(users.getKey(), "", "");
                        resultsUsers.add(obj);
                    }
                    mUsersAdapter.notifyDataSetChanged();
                } else {
                    mNoFriends.setText("No friends found!");
                    mNoFriends.setVisibility(View.VISIBLE);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
