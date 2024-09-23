package com.yahya.shadow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.yahya.shadow.fragments.ProfileFragment;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileMakingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference mDatabase,mDatabase1,mDatabase2,mDatabase4,mDatabase3,mDatabase5,mDatabase6,mDatabase7,mProfileDatabase;

    private CircleImageView profileImage;
    private Uri imageUri;
    private String myUri;
    private StorageTask uploadTask;
    private StorageReference storageProfilePicRef;
    private  EditText mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_making);
        Button mDone = (Button) findViewById(R.id.Done);
        mUser = (EditText) findViewById(R.id.usernameDown);
        EditText mEmail = (EditText) findViewById(R.id.email);
        TextView mUsername = (TextView) findViewById(R.id.usertxt);
        TextView mPoints = (TextView) findViewById(R.id.points);
        TextView mReputation = (TextView) findViewById(R.id.thanks);




        mAuth = FirebaseAuth.getInstance();

        mEmail.setEnabled(false);
        mUser.setEnabled(true);

        String user_id = mAuth.getCurrentUser().getUid();

        //Profile
        mProfileDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Pics");

        profileImage = findViewById(R.id.profileImg);

        //user
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("user");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user = dataSnapshot.getValue(String.class);
                mUsername.setText(user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileMakingActivity.this,"Error username !",Toast.LENGTH_SHORT).show();
            }
        });

        //email
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("email");
        mDatabase2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);
                mEmail.setText(email);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileMakingActivity.this,"Error email !",Toast.LENGTH_SHORT).show();
            }
        });
        //user
        mDatabase1 = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("user");
        mDatabase1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user = dataSnapshot.getValue(String.class);
                mUser.setText(user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileMakingActivity.this,"Error country !",Toast.LENGTH_SHORT).show();
            }
        });
        //reputation
        mDatabase4 = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("reputation");
        mDatabase4.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer reputation = dataSnapshot.getValue(Integer.class);
                String repu = Integer.toString(reputation);
                mReputation.setText(repu);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileMakingActivity.this,"Error country !",Toast.LENGTH_SHORT).show();
            }
        });
        //points
        mDatabase3 = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("points");
        mDatabase3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer points = dataSnapshot.getValue(Integer.class);
                String poi = Integer.toString(points);
                mPoints.setText(poi);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileMakingActivity.this,"Error country !",Toast.LENGTH_SHORT).show();
            }
        });

        getUserProfilePic();

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = ProgressDialog.show(ProfileMakingActivity.this,"Please wait","Saving Data",true);

                String user_id = mAuth.getCurrentUser().getUid().toString();

                //reset username
                DatabaseReference current_user_db6 = FirebaseDatabase.getInstance().getReference().child("userIds");
                current_user_db6.child(user_id).setValue(mUser.getText().toString());
                DatabaseReference current_user_db7 = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
                current_user_db7.child("user").setValue(mUser.getText().toString());
                ChangePostUser();

                //saving profile
                uploadProfilePic();
                progressDialog.dismiss();

                Intent intent = new Intent(v.getContext(), MainActivity.class);
                Bundle b = new Bundle();
                b.putInt("current",2);
                intent.putExtras(b);
                v.getContext().startActivity(intent);
            }
        });
        //profileImage.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        CropImage.activity().setAspectRatio(1,1).start(ProfileMakingActivity.this);
        //    }
        //});

    }

    private void ChangePostUser() {
        DatabaseReference userIdDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        userIdDatabase.child("postsUser").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot posts : snapshot.getChildren()){
                        EditPostsInformations(posts.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void EditPostsInformations(String post_id) {
        DatabaseReference postDatabase = FirebaseDatabase.getInstance().getReference().child("posts").child(post_id);
        postDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    DatabaseReference current_user_db8 = FirebaseDatabase.getInstance().getReference().child("posts").child(post_id);
                    current_user_db8.child("posterUserId").setValue(mUser.getText().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserProfilePic() {
        mProfileDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() >0){
                    if (snapshot.hasChild("profile")){
                        String Image = snapshot.child("profile").getValue().toString();
                        Picasso.get().load(Image).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void uploadProfilePic() {
        if (imageUri!=null){
            final StorageReference fileRef = storageProfilePicRef.child(mAuth.getCurrentUser().getUid());

            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task <Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri =task.getResult();
                        myUri = downloadUri.toString();

                        HashMap<String , Object> userMap = new HashMap<>();
                        userMap.put("profile",myUri);

                        mProfileDatabase.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                        Toast.makeText(ProfileMakingActivity.this, "Profile SAVED!", Toast.LENGTH_LONG).show();

                    }
                }
            });
        }else{
            Toast.makeText(ProfileMakingActivity.this,"No Image Selected",Toast.LENGTH_LONG).show();
        }
    }
}