package com.yahya.shadow.dailyGoals;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yahya.shadow.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    private List<GoalItem> goals;
    private Context context;
    private OnItemLongClickListener longClickListener;
    private OnDoubleClickListener doubleClickListener;
    public List<Boolean> goalsCheckedState;

    private GoalUpdateListener updateListener;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // milliseconds

    public GoalsAdapter(Context context, List<GoalItem> goals, GoalUpdateListener updateListener) {
        this.context = context;
        this.goals = goals;
        this.updateListener = updateListener;
        this.goalsCheckedState = new ArrayList<>(Collections.nCopies(goals.size(), false));
    }

    public interface OnDoubleClickListener {
        void onDoubleClick(int position);
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.daily_goal_item, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        holder.goalTextView.setText(goals.get(position).getTitle()+" : "+goals.get(position).getDescription());
        GoalItem goal = goals.get(position);
        holder.isDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.isDone.isChecked()){
                    goal.setDone(true);
                }else{
                    goal.setDone(false);
                }
                if (updateListener != null) {
                    updateListener.onGoalUpdate(position, goal.isDone()); // Notify listener of goal update
                }

            }
        });
        // Set long press listener
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
            }
            return true;
        });

        // Set the checked state

        // Handle double-click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            private boolean isDoubleClick = false;

            @Override
            public void onClick(View v) {
                if (isDoubleClick) {
                    if (doubleClickListener != null) {
                        doubleClickListener.onDoubleClick(position);
                    }
                    isDoubleClick = false;
                } else {
                    isDoubleClick = true;
                    new Handler().postDelayed(() -> isDoubleClick = false, DOUBLE_CLICK_TIME_DELTA);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalTextView;
        CheckBox isDone;

        public GoalViewHolder(View itemView) {
            super(itemView);
            goalTextView = itemView.findViewById(R.id.goalTextView);
            isDone = itemView.findViewById(R.id.goalDone);

        }
    }

    // Interface for long click listener
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnDoubleClickListener(OnDoubleClickListener listener) {
        this.doubleClickListener = listener;
    }

    public interface GoalUpdateListener {
        void onGoalUpdate(int position, boolean isDone);
    }
}
