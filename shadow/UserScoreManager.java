package com.yahya.shadow;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserScoreManager {
    private static UserScoreManager instance;
    private final DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users");

    private UserScoreManager() {}

    public static synchronized UserScoreManager getInstance() {
        if (instance == null) {
            instance = new UserScoreManager();
        }
        return instance;
    }

    public boolean blockUser(String currentUserId, String targetUserId) {
        final boolean[] success = {false};
        DatabaseReference currentUserRef = db.child(currentUserId);
        DatabaseReference targetUserRef = db.child(targetUserId);

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currentCounter = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("counter").getValue(Long.class);
                Long banUntil = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("banUntil").getValue(Long.class);
                Long blockedTimestamp = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("blockedUsers").child(targetUserId).getValue(Long.class);
                long currentTime = System.currentTimeMillis();

                if (currentCounter == null) currentCounter = 0L;
                if (banUntil != null && currentTime < banUntil) {
                    // User is currently banned
                    return;
                }

                if (blockedTimestamp != null) {
                    // User has already blocked this target
                    success[0] = false;
                    return;
                }

                // Update blockedUsers
                currentUserRef.child("blockedUsers").child(targetUserId).setValue(currentTime);

                // Update counter
                long newCounter = currentCounter - 5;
                currentUserRef.child("counter").setValue(newCounter);

                if (newCounter <= -5) {
                    // Ban the user for 24 hours
                    currentUserRef.child("banUntil").setValue(currentTime + 24 * 60 * 60 * 1000);
                }

                success[0] = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        return success[0];
    }

    public void likeUser(String currentUserId, String targetUserId) {
        DatabaseReference currentUserRef = db.child(currentUserId);
        long currentTime = System.currentTimeMillis();

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currentCounter = dataSnapshot.child("counter").getValue(Long.class);
                Long banUntil = dataSnapshot.child("banUntil").getValue(Long.class);

                if (currentCounter == null) currentCounter = 0L;
                if (banUntil != null && currentTime < banUntil) {
                    // User is currently banned
                    return;
                }

                // Update likedUsers
                currentUserRef.child("likedUsers").child(targetUserId).setValue(currentTime);

                // Update counter
                long newCounter = currentCounter + 1;
                currentUserRef.child("counter").setValue(newCounter);

                // Optionally, handle cases for counter limits
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}
