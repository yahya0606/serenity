package com.yahya.shadow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

import javax.xml.transform.Source;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail,mPass,mUser;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmail = findViewById(R.id.email);
        mUser = findViewById(R.id.username);
        Button mLogin = findViewById(R.id.login);
        mPass = findViewById(R.id.pass);
        TextView mSignUp = findViewById(R.id.signup);

        LinearLayout mLinear = findViewById(R.id.linear);
        LinearLayout mForgot = findViewById(R.id.forgot);

        mForgot.setVisibility(View.GONE);

        mLinear.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("current", 1);
                    startActivity(intent);
                }
            }
        };
        mPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPass.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    // hide password
                    mPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.show, 0);
                } else {
                    // show password
                    mPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.hide1, 0);
                }
            }
        });

        mLogin.setClickable(true);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(mEmail.getText().toString().trim().isEmpty() && mPass.getText().toString().trim().isEmpty())){
                    final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this,"Please wait","Logging In",true);
                    mAuth.signInWithEmailAndPassword("tester@tester.com","tester")
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("position", 2);
                                        startActivity(intent);

                                    }else{
                                        Log.e("ERROR", Objects.requireNonNull(task.getException()).toString());
                                        Toast.makeText(LoginActivity.this,"Forgot your password ?",Toast.LENGTH_LONG).show();
                                        mForgot.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                }else{
                    Toast.makeText(LoginActivity.this, "Please make sure you entered all the data", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(mEmail.getText().toString().trim().isEmpty() && mPass.getText().toString().trim().isEmpty())){
                    acceptPrivacyPolicy();
                }else{
                    Toast.makeText(LoginActivity.this, "Please make sure you entered all the data", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    mEmail.setError("Please enter your email");
                    mEmail.requestFocus();
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,
                                        "Password reset link sent to your email", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Failed to send reset email", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }

    private void acceptPrivacyPolicy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy");
        builder.setMessage("Your privacy is important to us. We collect your email, mood status, productivity stats, and sleep schedule to provide you with personalized insights. We do not share your data with third parties without your consent. You have the right to access, update, and delete your data at any time.");
        builder.setPositiveButton("Accept", (dialog, which) -> {
            // User accepted the privacy policy
            final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this,"Please wait","Setting your account up",true);
            mAuth.createUserWithEmailAndPassword("tester@tester.com", "tester")
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Oops! Try again", Toast.LENGTH_SHORT).show();
                            } else {
                                //saving data
                                //email
                                String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
                                current_user_db.child("email").setValue(mEmail.getText().toString());
                                //user
                                current_user_db.child("user").setValue(mUser.getText().toString());
                                //points
                                current_user_db.child("points").setValue(0);
                                //
                                current_user_db.child("acceptedPolicy").setValue(true);
                                //username
                                DatabaseReference current_user_db6 = FirebaseDatabase.getInstance().getReference().child("userIds");
                                current_user_db6.child(user_id).setValue(mUser.getText().toString());
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                //moving on
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("position", 2);
                                startActivity(intent);
                            }
                        }
                    });
        });
        builder.setNegativeButton("Decline", (dialog, which) -> {
            // User declined the privacy policy
            // Handle the decline (e.g., exit the app or restrict access)
            final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this,"Please wait","Setting your account up",true);
            //this part  mEmail.getText().toString(), mPass.getText().toString()
            mAuth.createUserWithEmailAndPassword("tester@tester.com", "tester")
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Oops! Try again later", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                //saving data
                                //email
                                String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
                                current_user_db.child("email").setValue(mEmail.getText().toString());
                                //user
                                current_user_db.child("user").setValue(mUser.getText().toString());
                                //points
                                current_user_db.child("points").setValue(0);
                                //
                                current_user_db.child("acceptedPolicy").setValue(false);
                                //username
                                DatabaseReference current_user_db6 = FirebaseDatabase.getInstance().getReference().child("userIds");
                                current_user_db6.child(user_id).setValue(mUser.getText().toString());
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                //moving on
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("position", 2);
                                startActivity(intent);
                            }
                        }
                    });
        });
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}