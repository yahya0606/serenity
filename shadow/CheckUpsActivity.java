package com.yahya.shadow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yahya.shadow.checkups.DailyCheckup;
import com.yahya.shadow.checkups.DailyCheckupAdapter;
import com.yahya.shadow.services.TimestampUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckUpsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DailyCheckupAdapter adapter;
    private List<DailyCheckup> checkupList;
    private DatabaseReference databaseReference;
    private CardView deleteData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_ups);

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.checkUps);
        deleteData = findViewById(R.id.deleteData);
        checkupList = new ArrayList<>(); // Initialize your data source

        // Add mock data (replace with actual data fetching logic as needed)
        fillInTheData();

        adapter = new DailyCheckupAdapter(this, checkupList, databaseReference);

        // Set layout manager and adapter for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        deleteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });
    }
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Checkup History")
                .setMessage("Are you sure you want to delete all your checkups? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCheckup();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void deleteCheckup() {
        databaseReference.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CheckUpsActivity.this, "Checkup deleted", Toast.LENGTH_SHORT).show();
                        checkupList.clear();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CheckUpsActivity.this, "Failed to delete checkup", Toast.LENGTH_SHORT).show();
                        Log.e("DailyCheckupAdapter", "Error deleting checkup", e);
                    }
                });
    }

    private void fillInTheData() {
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("tracker")
                .child("dailydata");

        // Query to fetch data
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnapshot : dataSnapshot.child("moodTracker").getChildren()) {
                    String activityRate = "0";
                    String timestamp = dateSnapshot.getKey();
                    String date = convertTimestampToDate(Long.parseLong(timestamp) * 1000);
                    String moodRate = String.valueOf(dataSnapshot.child("moodTracker").child(timestamp).getValue(Integer.class));
                    if (dataSnapshot.child("activityTracker").child(timestamp).exists()) {
                        activityRate = String.valueOf(dataSnapshot.child("activityTracker").child(timestamp).getValue(Integer.class));
                    }

                    String sleepRate = "0";
                    String sleepDuration = "0";
                    DataSnapshot sleepSnapshot = dataSnapshot.child("sleepTracker").child(timestamp);
                    if (sleepSnapshot.child("rateSleep").exists()) {
                        sleepRate = String.valueOf(sleepSnapshot.child("rateSleep").getValue(Integer.class));
                    }
                    if (sleepSnapshot.child("sleepDuration").exists()) {
                        sleepDuration = String.valueOf(sleepSnapshot.child("sleepDuration").getValue(Integer.class));
                    }
                    Integer Average = (Integer.parseInt(moodRate) + Integer.parseInt(sleepRate)) / 2;
                    Integer sleep = Integer.parseInt(sleepDuration);
                    DailyCheckup dailyCheckup = new DailyCheckup(timestamp, String.valueOf(Average) + "/10", sleepRate, moodRate, activityRate, sleepDuration, "Keep it up !!");
                    checkupList.add(dailyCheckup);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DailyCheckupsActivity", "Error fetching data", databaseError.toException());
            }
        });
    }
    private String convertTimestampToDate(float x) {
        Date date = new Date((long) x);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
    private long convertDateToTimestamp(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            Date parsedDate = sdf.parse(date);
            return parsedDate.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}