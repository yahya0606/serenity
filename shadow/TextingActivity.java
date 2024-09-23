package com.yahya.shadow;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.yahya.shadow.MessageRecyclerView.MsgAdapter;
import com.yahya.shadow.MessageRecyclerView.MsgObject;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TextingActivity extends AppCompatActivity {
    private RecyclerView mMsgsRecyclerView;
    private RecyclerView.Adapter mMsgsAdapter;
    private String uniqueMsgId;
    private ImageView mProfile, mSend, mImageSend;
    private EditText msg;
    private TextView friendUsername;
    private SlidrInterface slidr;
    private long lastUpdateTime = 0; // To keep track of last update time
    private DatabaseReference chatRef;
    private ArrayList<MsgObject> resultsMsgs = new ArrayList<>();
    private boolean hasAcceptedCodeOfConduct = false;
    private Integer messageCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texting);

        initializeViews();
        setupRecyclerView();

        slidr = Slidr.attach(this);

        Bundle user = getIntent().getExtras();
        if (user != null) {
            String chatRoomId = user.getString("chatRoom");
            friendUsername.setText(chatRoomId);
            chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);

            // Check if the user is authorized to access this chat room
            checkUserAuthorization(chatRoomId, new AuthorizationCallback() {
                @Override
                public void onAuthorized() {
                    FetchMsgs();
                    setupSendButton();
                    listenForNewMessages();
                }

                @Override
                public void onUnauthorized() {
                    showCodeOfConductDialog(chatRoomId);
                }
            });
        }
    }

    private void initializeViews() {
        mProfile = findViewById(R.id.msg_receiver_pic);
        friendUsername = findViewById(R.id.msg_receiver_name);
        msg = findViewById(R.id.msg);
        mImageSend = findViewById(R.id.image_msg);
        mSend = findViewById(R.id.send);
    }

    private void setupRecyclerView() {
        mMsgsRecyclerView = findViewById(R.id.msgs);
        mMsgsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);  // Start from the bottom
        layoutManager.setReverseLayout(false); // Reverse the layout
        mMsgsRecyclerView.setLayoutManager(layoutManager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMsgsAdapter = new MsgAdapter(resultsMsgs ,this,fragmentManager);
        mMsgsRecyclerView.setAdapter(mMsgsAdapter);
    }

    private void setupSendButton() {
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messageText = msg.getText().toString();
        if (messageText.isEmpty()) {
            showToast("Message cannot be empty.");
            return;
        }

        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(senderId);

        userRef.child("counter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageCounter = snapshot.getValue(Integer.class);
                if (messageCounter == null) {
                    messageCounter = 0;
                }

                userRef.child("banUntil").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long banEndTime = snapshot.getValue(Long.class);
                        long currentTime = System.currentTimeMillis();

                        if (banEndTime != null && currentTime < banEndTime) {
                            long remainingBanTime = banEndTime - currentTime;
                            showBanPopup(remainingBanTime);
                            return;
                        }

                        if (messageCounter <= -5) {
                            // Set ban end time to 24 hours from now
                            banEndTime = currentTime + TimeUnit.HOURS.toMillis(24);
                            userRef.child("banUntil").setValue(banEndTime);
                            messageCounter = 0; // Reset counter after banning
                        }

                        if (messageCounter > -5) {
                            // Allow sending message
                            uniqueMsgId = UUID.randomUUID().toString();
                            String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

                            // Prepare message data
                            DatabaseReference messageRef = chatRef.child("messages").child(uniqueMsgId);
                            messageRef.child("message").setValue(messageText);
                            messageRef.child("sender").setValue(senderId);
                            messageRef.child("delivered").setValue("no");
                            messageRef.child("notified").setValue("no");
                            messageRef.child("seen").setValue("no");
                            messageRef.child("timestamp").setValue(timeStamp);

                            // Update last message info
                            chatRef.child("infos").child("lastMsg").setValue(messageText);
                            chatRef.child("infos").child("sender").setValue(senderId);
                            chatRef.child("infos").child("seen").setValue("false");
                            chatRef.child("infos").child("timestamp").setValue(timeStamp);

                            // Update counter
                            userRef.child("messageCounter").setValue(messageCounter - 1);

                            // Update UI
                            MsgObject obj = new MsgObject(senderId, " ", messageText);
                            resultsMsgs.add(obj);
                            mMsgsAdapter.notifyItemInserted(resultsMsgs.size() - 1);
                            mMsgsRecyclerView.scrollToPosition(resultsMsgs.size() - 1);
                            msg.setText(null);
                        } else {
                            showToast("You are banned from sending messages.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void showBanPopup(long remainingBanTime) {
        new AlertDialog.Builder(this)
                .setTitle("You Are Banned")
                .setMessage("You are banned from sending messages for " + TimeUnit.MILLISECONDS.toMinutes(remainingBanTime)/60+" hours and " + TimeUnit.MILLISECONDS.toMinutes(remainingBanTime)%60+ " minutes.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }


    private void FetchMsgs() {
        chatRef.child("messages").orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                    FetchMsgInformations(msgSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void FetchMsgInformations(String msgId) {
        chatRef.child("messages").child(msgId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Update message status
                    chatRef.child("messages").child(msgId).child("delivered").setValue("yes");
                    chatRef.child("messages").child(msgId).child("seen").setValue("yes");

                    // Create and add message object
                    String sender = snapshot.child("sender").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);
                    MsgObject obj = new MsgObject(sender, "receiver", message);
                    resultsMsgs.add(obj);
                    mMsgsAdapter.notifyItemInserted(resultsMsgs.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void listenForNewMessages() {
        chatRef.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                handleNewMessage(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                handleNewMessage(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // A child has been removed
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // A child has been moved
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void handleNewMessage(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            String message = dataSnapshot.child("message").getValue(String.class);
            String sender = dataSnapshot.child("sender").getValue(String.class);

            if (sender != null && !sender.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > 1000) { // 1 second debounce time
                    lastUpdateTime = currentTime;
                    MsgObject obj = new MsgObject(sender, "user_id", message);
                    resultsMsgs.add(obj);
                    mMsgsAdapter.notifyItemInserted(resultsMsgs.size() - 1);
                    mMsgsRecyclerView.scrollToPosition(resultsMsgs.size() - 1);
                } else {
                    Log.d(TAG, "Duplicate update ignored.");
                }
            }
        }
    }

    private void checkUserAuthorization(String chatRoomId, AuthorizationCallback callback) {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("chats");

        userChatsRef.child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onAuthorized();
                } else {
                    callback.onUnauthorized();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUnauthorized();
            }
        });
    }

    private void showCodeOfConductDialog(final String chatRoomId) {
        new AlertDialog.Builder(this)
                .setTitle("Welcome to the Chat Room")
                .setMessage("As a new member of this chat room, we ask that you adhere to our community guidelines. Please be respectful and avoid offending others. Do you agree to follow these guidelines?")
                .setPositiveButton("I Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hasAcceptedCodeOfConduct = true;
                        // Grant access and save the acceptance
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("chats")
                                .child(chatRoomId)
                                .setValue(true);
                        dialog.dismiss();
                        // Proceed to fetch messages and setup the chat
                        FetchMsgs();
                        setupSendButton();
                        listenForNewMessages();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish(); // Close the activity
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private interface AuthorizationCallback {
        void onAuthorized();
        void onUnauthorized();
    }
}
