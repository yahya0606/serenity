package com.yahya.shadow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yahya.shadow.dailyGoals.GoalItem;

public class SettingsActivity extends AppCompatActivity {

    private TextView usernameEditText;
    private ImageView profileImageView;
    private CardView privacyPolicyButton;
    private CardView creditsButton;
    private CardView deleteAccount;
    private DatabaseReference mDatabase,profileReference;
    private StorageReference mStorage;
    private String mAuth;
    private ImageView infoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        usernameEditText = findViewById(R.id.username);
        profileImageView = findViewById(R.id.profilePic);
        privacyPolicyButton = findViewById(R.id.policy);
        creditsButton = findViewById(R.id.credit);
        mAuth = FirebaseAuth.getInstance().getCurrentUser().getUid();
        infoButton = findViewById(R.id.infoButton);
        deleteAccount = findViewById(R.id.deleteAccount);

        mStorage = FirebaseStorage.getInstance().getReference().child("Profile Pics");

        // Bring the profile image view to the top
        profileImageView.bringToFront();
        profileReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth);

        //profile
        profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() >0){
                    if (snapshot.hasChild("profile")){
                        String Image = snapshot.child("profile").getValue().toString();
                        Picasso.get().load(Image).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle profile picture change
                selectProfilePicture();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user = dataSnapshot.child("user").getValue(String.class);
                usernameEditText.setText(user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Importing username","Failed!");
            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle profile picture change
                selectProfilePicture();
            }
        });

        usernameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show dialog to edit username
                editUserName();
            }
        });
        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open privacy policy
                openPrivacyPolicy();
            }
        });
        creditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open credits
                openCredits();
            }
        });
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show tooltip
                showInfoBubble(v);
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAccountDeletion();
            }
        });

    }
    private void confirmAccountDeletion() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Prompt user to re-authenticate
            reAuthenticateUser();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void reAuthenticateUser() {
        // Get current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Show dialog to get email and password
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Re-authenticate");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            // Convert 48dp to pixels to ensure consistent height across different screen densities
            int heightInDp = 48;
            int heightInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());


            // Set height using layout parameters instead of setHeight
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);

            final EditText emailInput = new EditText(this);
            emailInput.setHint("Email");
            emailInput.setLayoutParams(params);
            emailInput.setHintTextColor(Color.WHITE);
            layout.addView(emailInput);

            final EditText passwordInput = new EditText(this);
            passwordInput.setHint("Password");
            passwordInput.setLayoutParams(params);
            passwordInput.setHintTextColor(Color.WHITE);
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.setBackgroundColor(Color.BLACK);
            layout.addView(passwordInput);

            builder.setView(layout);

            builder.setPositiveButton("Submit", (dialog, which) -> {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Re-authentication successful, proceed to delete account
                            deleteAccountAndData();
                        } else {
                            // Handle re-authentication failure
                            Log.e("Re-authentication", "Failed to re-authenticate", task.getException());
                            Toast.makeText(SettingsActivity.this, "Failed to re-authenticate. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(SettingsActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.show();
        }
    }

    private void deleteAccountAndData() {
        // Get current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Reference to user's data in Firebase Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth);

            // Reference to user's profile picture in Firebase Storage
            StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Pics").child(mAuth);

            // Delete profile picture from Firebase Storage
            profileImageRef.delete().addOnSuccessListener(aVoid -> {
                Log.d("Delete Account", "Profile picture deleted successfully");
            }).addOnFailureListener(e -> {
                Log.e("Delete Account", "Failed to delete profile picture", e);
            });

            // Delete user data from Firebase Database
            userRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Delete Account", "User data deleted successfully");

                    // Delete user from Firebase Authentication
                    user.delete().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.d("Delete Account", "User account deleted successfully");
                            Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            // Redirect to login or main activity
                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Log.e("Delete Account", "Failed to delete user account", task1.getException());
                            Toast.makeText(SettingsActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Delete Account", "Failed to delete user account", e);
                        Toast.makeText(SettingsActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e("Delete Account", "Failed to delete user data", task.getException());
                    Toast.makeText(SettingsActivity.this, "Failed to delete account data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void showInfoBubble(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View bubbleView = inflater.inflate(R.layout.bubble_layout, null);

        final PopupWindow popupWindow = new PopupWindow(bubbleView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // Show the popup window near the info button
        popupWindow.showAsDropDown(anchorView, -50, 0, Gravity.END);

        // Close the popup window when touched
        bubbleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void editUserName(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Edit your Username");

        // Convert 48dp to pixels to ensure consistent height across different screen densities
        int heightInDp = 48;
        int heightInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());


        // Set height using layout parameters instead of setHeight
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);

        final EditText UsernameInput = new EditText(this);
        UsernameInput.setHint("New username");
        UsernameInput.setHintTextColor(Color.WHITE);
        UsernameInput.setTextColor(Color.WHITE);

        UsernameInput.setLayoutParams(params);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(UsernameInput);
        layout.setBackgroundColor(Color.BLACK);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newUsername = UsernameInput.getText().toString();
            if (!newUsername.isEmpty()) {
                mDatabase.child("user").setValue(newUsername);
                usernameEditText.setText(newUsername);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void selectProfilePicture() {

    }

    private void saveProfilePicture(Uri imageUri) {
        // Implement logic to save the image URI to Firebase
        final StorageReference profileImageRef = mStorage.child(mAuth);
        UploadTask uploadTask = profileImageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            DatabaseReference userRef = mDatabase.child("profile");
            userRef.setValue(uri.toString());
            Toast.makeText(SettingsActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
        })).addOnFailureListener(e -> {
            Toast.makeText(SettingsActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
            Log.e("Upload Error", "Failed to upload profile picture", e);
        });
    }

    private void openPrivacyPolicy() {
        Intent intent = new Intent(SettingsActivity.this, PrivacyPolicyActivity.class);
        startActivity(intent);

    }

    private void openCredits() {
        Intent intent = new Intent(SettingsActivity.this, CreditsActivity.class);
        startActivity(intent);

    }
}
