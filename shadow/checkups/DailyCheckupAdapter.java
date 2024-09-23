package com.yahya.shadow.checkups;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.yahya.shadow.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyCheckupAdapter extends RecyclerView.Adapter<DailyCheckupAdapter.ViewHolder> {

    private List<DailyCheckup> checkupList;
    private SparseBooleanArray expandState; // Declaration of expandState
    private Context context;
    private DatabaseReference databaseReference;

    public DailyCheckupAdapter(Context context, List<DailyCheckup> checkupList, DatabaseReference databaseReference) {
        this.context = context;
        this.checkupList = checkupList;
        this.databaseReference = databaseReference;

        // Initialize expandState with false (collapsed) for each item
        expandState = new SparseBooleanArray();
        for (int i = 0; i < checkupList.size(); i++) {
            expandState.append(i, false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.check_up_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        DailyCheckup checkup = checkupList.get(position);

        String date = convertTimestampToDate(Float.parseFloat(checkup.getDay())*1000);
        holder.textDay.setText(date);
        holder.textRating.setText("Rating: " + checkup.getRating());

        holder.layoutSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpanded(position); // Toggle expanded state
            }
        });

        if (expandState.get(position)) {
            holder.layoutExpanded.setVisibility(View.VISIBLE);
        } else {
            holder.layoutExpanded.setVisibility(View.GONE);
        }

        Integer sleep = Integer.parseInt(checkup.getSleepDuration());
        holder.textSleepDuration.setText("You slept "+sleep/3600+" hours and "+(sleep%3600)/60+" minutes");
        holder.moodRate.setText("Mood Rate: "+checkup.getMoodRate()+"/10");
        holder.sleepRate.setText("Sleep Rate: "+checkup.getSleepRate()+"/10");
        Integer productivity = Integer.parseInt(checkup.getProductivity())*10;
        holder.productivity.setText("Productivity: "+productivity+"%");
        holder.textNotes.setVisibility(View.GONE);

        // Long press to delete
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteConfirmationDialog(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkupList.size();
    }

    private void toggleExpanded(int position) {
        boolean expanded = expandState.get(position);
        expandState.put(position, !expanded);
        notifyItemChanged(position);
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Checkup")
                .setMessage("Are you sure you want to delete this checkup?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCheckup(position);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteCheckup(int position) {
        DailyCheckup checkup = checkupList.get(position);
        // Assuming you have a field to identify checkups, e.g., a timestamp or ID
        String timestamp = checkup.getDay(); // Update based on actual data field

        databaseReference.child("moodTracker").child(timestamp).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Checkup deleted", Toast.LENGTH_SHORT).show();
                        checkupList.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete checkup", Toast.LENGTH_SHORT).show();
                        Log.e("DailyCheckupAdapter", "Error deleting checkup", e);
                    }
                });
        databaseReference.child("sleepTracker").child(timestamp).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Checkup deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete checkup", Toast.LENGTH_SHORT).show();
                        Log.e("DailyCheckupAdapter", "Error deleting checkup", e);
                    }
                });
        databaseReference.child("activityTracker").child(timestamp).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Checkup deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete checkup", Toast.LENGTH_SHORT).show();
                        Log.e("DailyCheckupAdapter", "Error deleting checkup", e);
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDay, textRating, textSleepDuration, textNotes, moodRate, sleepRate, productivity;
        LinearLayout layoutSummary, layoutExpanded;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.text_day);
            textRating = itemView.findViewById(R.id.text_rating);
            textSleepDuration = itemView.findViewById(R.id.text_sleep_duration);
            textNotes = itemView.findViewById(R.id.text_notes);
            moodRate = itemView.findViewById(R.id.text_mood_rate);
            sleepRate = itemView.findViewById(R.id.text_sleep_rate);
            productivity = itemView.findViewById(R.id.text_activity_rate);
            layoutSummary = itemView.findViewById(R.id.layout_summary);
            layoutExpanded = itemView.findViewById(R.id.layout_expanded);
        }
    }
    private String convertTimestampToDate(float x) {
        Date date = new Date((long) x);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}
