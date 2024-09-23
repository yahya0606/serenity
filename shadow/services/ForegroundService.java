package com.yahya.shadow.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yahya.shadow.R;
import com.yahya.shadow.TextingActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ForegroundService extends Service {
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = "ForegroundService";
    private DatabaseReference mDatabase;
    private Set<String> activeChatRooms = new HashSet<>();
    private Map<String, String> lastNotifiedMessageIds = new HashMap<>();  // Track last notified message per chat room

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");
        mDatabase = FirebaseDatabase.getInstance().getReference("chats");
        // Start by attaching listeners to existing chat rooms
        fetchAndAttachListenersToChatRooms();
        // Also listen for new chat rooms
        addChatRoomListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        String input = (intent != null) ? intent.getStringExtra("inputExtra") : "Foreground";
        int notificationId = (int) System.currentTimeMillis(); // Unique notification ID

        // Create the notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.mipmap.yingyang_round)
                .setPriority(NotificationCompat.PRIORITY_LOW)  // Set priority to low
                .setSilent(true)  // Make the notification silent
                .build();

        // Create a notification channel
        createNotificationChannel();

        // Call startForeground() to put the service in the foreground
        //startForeground(notificationId, notification);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Checkup Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void fetchAndAttachListenersToChatRooms() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatRoomSnapshot : dataSnapshot.getChildren()) {
                    String chatRoomId = chatRoomSnapshot.getKey();
                    if (chatRoomId != null && !activeChatRooms.contains(chatRoomId)) {
                        Log.d(TAG, "New room added: " + dataSnapshot.getKey());
                        activeChatRooms.add(chatRoomId);
                        addMessageListenerForChatRoom(chatRoomId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
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
                // Handle changes if needed
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String chatRoomId = dataSnapshot.getKey();
                if (chatRoomId != null) {
                    activeChatRooms.remove(chatRoomId);
                    // Optionally, remove message listeners if no longer needed
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle movements if needed
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
            }
        });
    }

    private void addMessageListenerForChatRoom(String chatRoomId) {
        DatabaseReference messagesRef = mDatabase.child(chatRoomId).child("messages");
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                //handleNewMessage(dataSnapshot, chatRoomId);
                Log.d(TAG, "New message added: " + dataSnapshot.child("message").getValue(String.class));

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle changes if needed
                //handleNewMessage(dataSnapshot, chatRoomId);
                Log.d(TAG, "New message changed: " + dataSnapshot.child("message").getValue(String.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle removals if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle movements if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

    private void handleNewMessage(@NonNull DataSnapshot dataSnapshot, String chatRoomId) {
        final String MESSAGE_KEY = "message";
        final String SENDER_KEY = "sender";

        // Retrieve the new message
        String messageId = dataSnapshot.getKey();
        String newMessage = dataSnapshot.child(MESSAGE_KEY).getValue(String.class);
        String user = dataSnapshot.child(SENDER_KEY).getValue(String.class);

        // Add null check for user
        if (user == null) {
            Log.e(TAG, "User ID is null, cannot fetch username.");
            return;
        }

        // Ensure this message has not already been notified
        String lastNotifiedMessageId = lastNotifiedMessageIds.get(chatRoomId);
        if (messageId.equals(lastNotifiedMessageId)) {
            Log.d(TAG, "Duplicate message detected, skipping notification.");
            return;
        }

        getUserName(user, sender -> {
            //if (Objects.equals(dataSnapshot.child("notified").getValue(String.class), "no")){
                if (!user.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    sendNotification(chatRoomId, sender, newMessage);
                    lastNotifiedMessageIds.put(chatRoomId, messageId);  // Update last notified message ID for this chat room
                }
            //}
        });
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

    private void sendNotification(String chatRoom, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String notificationId = String.valueOf(System.currentTimeMillis() / 1000);

        // Create an intent to open the app when the notification is clicked
        Intent intent = new Intent(this, TextingActivity.class);
        intent.putExtra("chatRoom", chatRoom);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationId, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationId)
                .setSmallIcon(R.mipmap.yingyang_round)
                .setContentTitle(chatRoom)
                .setContentText(title + ": " + message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSilent(false)
                .setAutoCancel(true); // Auto-cancel the notification when clicked

        notificationManager.notify(Integer.parseInt(notificationId), builder.build());
    }

    public interface UserNameCallback {
        void onCallback(String userName);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
