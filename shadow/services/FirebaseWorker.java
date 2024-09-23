package com.yahya.shadow.services;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.*;
import com.google.firebase.auth.FirebaseAuth;
import com.yahya.shadow.TextingActivity;

import com.yahya.shadow.R;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FirebaseWorker extends Worker {

    private DatabaseReference mDatabase;
    private Set<String> activeChatRooms = new HashSet<>();
    private Map<String, String> lastNotifiedMessageIds = new HashMap<>();

    public FirebaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mDatabase = FirebaseDatabase.getInstance().getReference("chats");
    }

    @NonNull
    @Override
    public Result doWork() {
        fetchAndAttachListenersToChatRooms();
        addChatRoomListener();
        return Result.success();
    }

    private void fetchAndAttachListenersToChatRooms() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatRoomSnapshot : dataSnapshot.getChildren()) {
                    String chatRoomId = chatRoomSnapshot.getKey();
                    if (chatRoomId != null && !activeChatRooms.contains(chatRoomId)) {
                        activeChatRooms.add(chatRoomId);
                        addMessageListenerForChatRoom(chatRoomId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void addChatRoomListener() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String chatRoomId = dataSnapshot.getKey();
                if (chatRoomId != null && !activeChatRooms.contains(chatRoomId)) {
                    activeChatRooms.add(chatRoomId);
                    addMessageListenerForChatRoom(chatRoomId);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String chatRoomId = dataSnapshot.getKey();
                if (chatRoomId != null) {
                    activeChatRooms.remove(chatRoomId);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addMessageListenerForChatRoom(String chatRoomId) {
        DatabaseReference messagesRef = mDatabase.child(chatRoomId).child("messages");
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                //handleNewMessage(dataSnapshot, chatRoomId);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                //handleNewMessage(dataSnapshot, chatRoomId);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void handleNewMessage(@NonNull DataSnapshot dataSnapshot, String chatRoomId) {
        final String MESSAGE_KEY = "message";
        final String SENDER_KEY = "sender";

        String messageId = dataSnapshot.getKey();
        String newMessage = dataSnapshot.child(MESSAGE_KEY).getValue(String.class);
        String user = dataSnapshot.child(SENDER_KEY).getValue(String.class);

        if (user == null) {
            return;
        }

        String lastNotifiedMessageId = lastNotifiedMessageIds.get(chatRoomId);
        if (messageId.equals(lastNotifiedMessageId)) {
            return;
        }

        getUserName(user, sender -> {
            if (Objects.equals(dataSnapshot.child("notified").getValue(String.class), "no")) {
                if (!user.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    sendNotification(chatRoomId, sender, newMessage);
                    lastNotifiedMessageIds.put(chatRoomId, messageId);
                }
            }
        });
    }

    private void sendNotification(String chatRoom, String title, String message) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String notificationId = String.valueOf(System.currentTimeMillis() / 1000);

        Intent intent = new Intent(context, TextingActivity.class);
        intent.putExtra("chatRoom", chatRoom);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationId, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationId)
                .setSmallIcon(R.mipmap.yingyang_round) // Updated reference
                .setContentTitle(chatRoom)
                .setContentText(title + ": " + message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSilent(false)
                .setAutoCancel(true);

        notificationManager.notify(Integer.parseInt(notificationId), builder.build());
    }

    public static void getUserName(String user, UserNameCallback callback) {
        DatabaseReference userNameRef = FirebaseDatabase.getInstance().getReference().child("users").child(user).child("user");
        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.getValue(String.class);
                callback.onCallback(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        });
    }

    public interface UserNameCallback {
        void onCallback(String userName);
    }
}