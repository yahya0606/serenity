package com.yahya.shadow.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yahya.shadow.MyApp;
import com.yahya.shadow.R;
import com.yahya.shadow.chartRecyclerView.ChartData;
import com.yahya.shadow.chartRecyclerView.LineChartAdapter;
import com.yahya.shadow.dailyGoals.GoalItem;
import com.yahya.shadow.dailyGoals.GoalsAdapter;
import com.yahya.shadow.journals.Memo;
import com.yahya.shadow.services.StreakViewModel;
import com.yahya.shadow.services.TimestampUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;


public class HomeFragment extends Fragment implements GoalsAdapter.GoalUpdateListener{

    private LineChart moodChart;
    private List<GoalItem> dailyGoals;
    private TextView completionRateTextView,streakCountText,noGoals;
    private FloatingActionButton addGoalButton;
    private GoalsAdapter goalsAdapter;
    private RecyclerView lineChartRecyclerView,recyclerView;
    private LineChartAdapter lineChartAdapter;
    private StreakViewModel streakViewModel;
    private static final String FILE_NAME = "daily_goals.txt";
    private static final String PREFS_NAME = "DailyGoalsPrefs";
    private static final String LAST_UPDATE_KEY = "LastUpdate";
    private static final String STREAK_COUNT_KEY = "StreakCount";
    private static final int SPRINKLE_COUNT = 50; // Number of sprinkles to be animated
    private static final int SPRINKLE_DURATION = 3000; // Duration for each sprinkle animation
    private AdView mAdView;
    TextView noDataText;


    private int totalGoals = 0;
    private int doneGoals = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        addGoalButton = view.findViewById(R.id.add_goal_button);
        completionRateTextView = view.findViewById(R.id.completion_rate);
        noGoals = view.findViewById(R.id.noGoalText);
        noDataText = view.findViewById(R.id.noDataText);


        recyclerView = view.findViewById(R.id.recyclerView);
        lineChartRecyclerView = view.findViewById(R.id.line_chart_recycler_view);

        FrameLayout rootLayout = view.findViewById(R.id.rootLayout);
        streakViewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // Initialize dailyGoals list and adapter
        dailyGoals = new ArrayList<>();

        // Calculate initial completion rate
        updateCompletionRate();

        checkAndResetGoals(rootLayout);
        setUpLineChart();
        setUpRecyclerView();

        checkDailyGoalsSize();

        // Initialize the Mobile Ads SDK
        MobileAds.initialize(getContext(), initializationStatus -> {});

        // Find the AdView and load the ad
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Button checkupButton = view.findViewById(R.id.checkup_button);
        checkupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference hasAccepted = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("acceptedPolicy");
                hasAccepted.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            boolean hasAcceptedTerms = Boolean.TRUE.equals(snapshot.getValue(boolean.class));
                            if (hasAcceptedTerms){
                                startDailyCheckup();
                            }else{
                                askToAcceptTerms();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        // Add Goal Button Click Listener
        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle adding new goal
                addNewGoal("New Goal: Edit me");
            }
        });

        return view;
    }

    private void addSprinkles(FrameLayout rootLayout) {
        final Handler handler = new Handler();
        final Random random = new Random();

        for (int i = 0; i < SPRINKLE_COUNT; i++) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    createAndAnimateSprinkle(rootLayout, random);
                }
                }, i * 200); // Delay each sprinkle start time slightly
        }
    }
    private void createAndAnimateSprinkle(FrameLayout rootLayout, Random random) {
        if (getContext() == null) {
            // Ensure the context is available before proceeding
            return;
        }

        final View sprinkle = new View(getContext());
        sprinkle.setBackgroundResource(R.drawable.sprinkle);

        int sprinkleSize = random.nextInt(40) + 20; // Random size between 20 and 60
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sprinkleSize, sprinkleSize);
        params.leftMargin = random.nextInt(rootLayout.getWidth() - sprinkleSize);
        params.topMargin = rootLayout.getHeight(); // Start from the bottom
        sprinkle.setLayoutParams(params);

        rootLayout.addView(sprinkle);

        // Create the upward and downward animations
        int height = random.nextInt(3)+1;
        ObjectAnimator upAnimator = ObjectAnimator.ofFloat(sprinkle, "translationY", 0, -rootLayout.getHeight()*height / 4f);
        upAnimator.setDuration(SPRINKLE_DURATION / 2);
        upAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator downAnimator = ObjectAnimator.ofFloat(sprinkle, "translationY", -rootLayout.getHeight()*height / 4f, 0);
        downAnimator.setDuration(SPRINKLE_DURATION / 2);
        downAnimator.setInterpolator(new AccelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(upAnimator, downAnimator);
        animatorSet.start();

        animatorSet.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) { }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                rootLayout.removeView(sprinkle);
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                rootLayout.removeView(sprinkle);
            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) { }
        });
    }

    private void checkDailyGoalsSize() {
        if (dailyGoals.size()==0){
            noGoals.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            noGoals.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void checkAndResetGoals(FrameLayout rootLayout) {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastUpdate = prefs.getString(LAST_UPDATE_KEY, null);
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        int streakCount = prefs.getInt(STREAK_COUNT_KEY, 0);

        if (lastUpdate == null || !lastUpdate.equals(currentDate)) {
            if (lastUpdate != null && !isConsecutiveDate(lastUpdate, currentDate)) {
                streakCount = 0; // Reset streak count if not consecutive
            } else {
                streakCount++; // Increment streak count if consecutive
            }

            calculateAndSaveCompletionRate();
            //addSprinkles(rootLayout);
            dailyGoals.clear();
            saveGoals();

            prefs.edit().putString(LAST_UPDATE_KEY, currentDate).apply();
            prefs.edit().putInt(STREAK_COUNT_KEY, streakCount).apply();
        }
        loadGoals();
    }
    private boolean isConsecutiveDate(String lastUpdate, String currentDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date lastDate = sdf.parse(lastUpdate);
            Date current = sdf.parse(currentDate);
            long difference = current.getTime() - lastDate.getTime();
            long daysDifference = difference / (1000 * 60 * 60 * 24);
            return daysDifference == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateCompletionRate() {
        doneGoals = 0;
        for (int i = 0 ; i < dailyGoals.size(); i++){
            if (dailyGoals.get(i).isDone()){
                doneGoals++;
            }
        }
        totalGoals = dailyGoals.size();
        int completionPercentage = totalGoals == 0 ? 0 : (int) (((float) doneGoals / totalGoals) * 100);
        completionRateTextView.setText("Completion: " + doneGoals + " / " + totalGoals + " (" + completionPercentage + "%)");
    }
    private void calculateAndSaveCompletionRate() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("tracker")
                .child("dailydata")
                .child("activityTracker");
        if (!dailyGoals.isEmpty()) {
            doneGoals = 0;
            for (int i = 0 ; i < dailyGoals.size(); i++){
                if (dailyGoals.get(i).isDone()){
                    doneGoals++;
                }
            }
            totalGoals = dailyGoals.size();
            int completionPercentage = totalGoals == 0 ? 0 : (int) (((float) doneGoals / totalGoals) * 10);
            long timestamp = TimestampUtils.getTodayMidnightTimestamp(System.currentTimeMillis())/1000;
            mDatabase.child(String.valueOf(timestamp)).setValue(completionPercentage);
        }else{
            long timestamp = TimestampUtils.getTodayMidnightTimestamp(System.currentTimeMillis())/1000;
            mDatabase.child(String.valueOf(timestamp)).setValue(0);
        }
    }

    private void askToAcceptTerms() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Privacy Policy");
        builder.setMessage("Your privacy is important to us. We collect your email, mood status, productivity stats, and sleep schedule to provide you with personalized insights. We do not share your data with third parties without your consent. You have the right to access, update, and delete your data at any time.");
        builder.setPositiveButton("Accept", (dialog, which) -> {
            // User accepted the privacy policy
            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            current_user_db.child("acceptedPolicy").setValue(true);
        });
        builder.setNegativeButton("Decline", (dialog, which) -> {
            // User declined the privacy policy
            // Handle the decline (e.g., exit the app or restrict access)

        });
        builder.show();
    }

    private void setUpLineChart() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("tracker")
                .child("dailydata");

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ChartData> chartDataList = new ArrayList<>();

                for (DataSnapshot trackers : snapshot.getChildren()) {
                    ArrayList<Entry> entries = new ArrayList<>();

                    for (DataSnapshot trackerValues : trackers.getChildren()) {
                        long x = trackerValues.getChildrenCount();
                        long key = Long.parseLong(trackerValues.getKey());
                        long value = (x == 0) ? trackerValues.getValue(Double.class).longValue()
                                : trackerValues.child("rateSleep").getValue(Integer.class).longValue();
                        entries.add(new Entry(key, value));
                    }

                    String trackerKey = trackers.getKey().toLowerCase();
                    String chartLabel;
                    if ("moodtracker".equals(trackerKey)) {
                        chartLabel = "Your Mood Chart";
                    } else if ("sleeptracker".equals(trackerKey)) {
                        chartLabel = "Your Sleep Chart";
                    } else {
                        chartLabel = "Your Productivity Chart";
                    }

                    chartDataList.add(new ChartData(chartLabel, entries));
                }
                showChartData(chartDataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    private void showNoDataMessage(String message) {
        lineChartRecyclerView.setVisibility(View.GONE);
        noDataText.setText(message);
        noDataText.setVisibility(View.VISIBLE);
    }

    private void showChartData(List<ChartData> chartDataList) {
        lineChartAdapter = new LineChartAdapter(getContext(), chartDataList);
        lineChartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        lineChartRecyclerView.setAdapter(lineChartAdapter);
        noDataText.setVisibility(View.GONE);
        lineChartRecyclerView.setVisibility(View.VISIBLE);
    }


    private void setUpRecyclerView() {
        goalsAdapter = new GoalsAdapter(getContext(), dailyGoals, this); // Pass 'this' as GoalUpdateListener

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(goalsAdapter);

        // Set double click listener for editing
        goalsAdapter.setOnDoubleClickListener(this::editGoal);

        // Set long click listener for deletion
        goalsAdapter.setOnItemLongClickListener(position -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dailyGoals.remove(position);
                        goalsAdapter.notifyItemRemoved(position);
                        goalsAdapter.notifyItemRangeChanged(position, dailyGoals.size());
                        checkDailyGoalsSize();
                        updateCompletionRate();
                        saveGoals();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                if (dX < 0) { // Swiping left
                    itemView.setTranslationX(dX / 4); // Move itemView to the left
                } else {
                    itemView.setTranslationX(0);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }
    private void editGoal(int position) {
        String goal = dailyGoals.get(position).getDescription();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Goal");

        final EditText input = new EditText(getContext());
        input.setText(goal.replace(" (Done)", ""));
        builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
            String editedGoal = input.getText().toString();
            dailyGoals.get(position).setDescription(editedGoal);
            saveGoals();
            goalsAdapter.notifyItemChanged(position); // Notify adapter of change
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void addNewGoal(String goal) {
        askForDetails();
    }
    private void askForDetails() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Add a milestone");

        // Convert 48dp to pixels to ensure consistent height across different screen densities
        int heightInDp = 48;
        int heightInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());

        // Set height using layout parameters instead of setHeight
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);


        final EditText titleInput = new EditText(getContext());
        titleInput.setHint("Title");
        titleInput.setLayoutParams(params);
        titleInput.setHintTextColor(Color.WHITE);
        titleInput.setTextColor(Color.WHITE);


        final EditText bodyInput = new EditText(getContext());
        bodyInput.setHint("Body");
        bodyInput.setLayoutParams(params);
        bodyInput.setHintTextColor(Color.WHITE);
        bodyInput.setTextColor(Color.WHITE);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.BLACK);
        layout.addView(titleInput);
        layout.addView(bodyInput);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleInput.getText().toString();
            String body = bodyInput.getText().toString();
            if (!title.isEmpty() && !body.isEmpty()) {
                GoalItem newMemo = new GoalItem(title, body, false);
                dailyGoals.add(newMemo);
                goalsAdapter.notifyItemInserted(dailyGoals.size() - 1);
                recyclerView.scrollToPosition(dailyGoals.size()-1);
                updateCompletionRate();
                checkDailyGoalsSize();
                saveGoals();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void loadGoals() {
        try {
            FileInputStream fis = getContext().openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";;");
                if (parts.length == 3) {
                    String title = parts[0];
                    String description = parts[1];
                    boolean isDone = Boolean.parseBoolean(parts[2]);
                    dailyGoals.add(new GoalItem(title, description, isDone));
                }
            }
            goalsAdapter.notifyDataSetChanged();
            reader.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void saveGoals() {
        try {
            FileOutputStream fos = getContext().openFileOutput(FILE_NAME, MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            for (GoalItem goal : dailyGoals) {
                writer.write(goal.getTitle() + ";;" + goal.getDescription() + ";;" + goal.isDone());
                writer.newLine();
            }
            writer.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDailyCheckup() {
        // List of questions for the daily checkup
        List<String> questions = Arrays.asList(
                "How is your mood today?",
                "How was your sleep?",
                "When did you sleep?",
                "When did you wake up?"
        );

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_daily_checkup, null);
        TextView questionTextView = dialogView.findViewById(R.id.questionText);
        Button nextButton = dialogView.findViewById(R.id.Next);
        Button saveButton = dialogView.findViewById(R.id.Cancel);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        EditText answerInput = dialogView.findViewById(R.id.answerInput);

        // Initialize the index for the current question
        final int[] currentQuestionIndex = {0};
        questionTextView.setText(questions.get(currentQuestionIndex[0]));

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle the 'Next' button click
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex[0] < questions.size() - 1) {
                // Save the answer based on visibility
                if (answerInput.getVisibility() == View.VISIBLE) {
                    String userInput = answerInput.getText().toString();
                    int validatedInput = validateUserInput(userInput);
                    if (validatedInput>=0){
                        saveRate(questions.get(currentQuestionIndex[0]), validatedInput);
                        // Move to the next question
                        currentQuestionIndex[0]++;
                        updateUIForCurrentQuestion(questions, currentQuestionIndex[0], questionTextView, timePicker, answerInput);
                    }
                } else {
                    saveTime(questions.get(currentQuestionIndex[0]), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                    // Move to the next question
                    currentQuestionIndex[0]++;
                    updateUIForCurrentQuestion(questions, currentQuestionIndex[0], questionTextView, timePicker, answerInput);
                }

                // Change button text and action if it's the last question
                if (currentQuestionIndex[0] == questions.size() - 1) {
                    nextButton.setText("Save");
                    nextButton.setOnClickListener(view -> {
                        streakViewModel.checkDailyCheckup();
                        saveTime(questions.get(currentQuestionIndex[0]), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                        dialog.dismiss();
                    });
                }
            }
        });

        // Handle the 'Save' button click (currently does nothing)
        saveButton.setOnClickListener(v -> dialog.dismiss());
    }

    private int validateUserInput(String userInput) {
        int number;

        try {
            number = Integer.parseInt(userInput);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Enter a number please '0..10'", Toast.LENGTH_SHORT).show();
            return -1; // or any other value indicating invalid input
        }

        // Check if the number is lower than 0 or higher than 10
        if (number < 0) {
            number = 0;
        } else if (number > 10) {
            number = 10;
        }

        return number;
    }

    // Helper method to update UI based on the current question
    private void updateUIForCurrentQuestion(List<String> questions, int currentIndex, TextView questionTextView, TimePicker timePicker, EditText answerInput) {
        String currentQuestion = questions.get(currentIndex);
        if (currentQuestion.toLowerCase().contains("when")) {
            answerInput.setVisibility(View.GONE);
            timePicker.setVisibility(View.VISIBLE);
        } else {
            timePicker.setVisibility(View.GONE);
            answerInput.setVisibility(View.VISIBLE);
        }
        questionTextView.setText(currentQuestion);
    }
    private void saveSleepDuration() {
        DatabaseReference mDatabase = getDatabaseReference().child("sleepTracker");
        long timestamp = TimestampUtils.getTodayMidnightTimestamp(System.currentTimeMillis()) / 1000;

        mDatabase.child(String.valueOf(timestamp)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("sleepTime").exists() && snapshot.child("wakeUpTime").exists()) {
                        try {
                            int sleepTime = Integer.parseInt(Objects.requireNonNull(snapshot.child("sleepTime").getValue(String.class)));
                            int wakeUpTime = Integer.parseInt(Objects.requireNonNull(snapshot.child("wakeUpTime").getValue(String.class)));
                            int sleepDuration = Math.abs((((wakeUpTime/100)*60)+(wakeUpTime%100))-(((sleepTime/100)*60)+(sleepTime%100)));
                            int durationInSeconds = sleepDuration*60;
                            mDatabase.child(String.valueOf(timestamp)).child("sleepDuration").setValue(durationInSeconds);
                        } catch (NumberFormatException e) {
                            Log.e("saveSleepDuration", "Error parsing sleep time or wake up time", e);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("saveSleepDuration", "Database error: " + error.getMessage());
            }
        });
    }
    private void saveTime(String question, Integer currentHour, Integer currentMinute) {
        DatabaseReference mDatabase = getDatabaseReference().child("sleepTracker");
        long timestamp = TimestampUtils.getTodayMidnightTimestamp(System.currentTimeMillis()) / 1000;
        String time = String.format("%02d%02d", currentHour, currentMinute); // Ensure two-digit formatting

        mDatabase.child(String.valueOf(timestamp)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (question.toLowerCase().contains("sleep")) {
                    mDatabase.child(String.valueOf(timestamp)).child("sleepTime").setValue(time)
                            .addOnSuccessListener(aVoid -> Log.d("saveTime", "Sleep time saved: " + time))
                            .addOnFailureListener(e -> Log.e("saveTime", "Failed to save sleep time", e));
                } else {
                    mDatabase.child(String.valueOf(timestamp)).child("wakeUpTime").setValue(time)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("saveTime", "Wake up time saved: " + time);
                                saveSleepDuration();
                            })
                            .addOnFailureListener(e -> Log.e("saveTime", "Failed to save wake up time", e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("saveTime", "Database error: " + error.getMessage());
            }
        });
    }
    private void saveRate(String question, Integer value) {
        DatabaseReference mDatabase = getDatabaseReference();
        long timestamp = TimestampUtils.getTodayMidnightTimestamp(System.currentTimeMillis()) / 1000;
        if (question.toLowerCase().contains("mood")) {
            mDatabase.child("moodTracker").child(String.valueOf(timestamp)).setValue(value);
        } else {
            DatabaseReference sleepTrackerRef = mDatabase.child("sleepTracker").child(String.valueOf(timestamp));
            sleepTrackerRef.child("rateSleep").setValue(value);
            // Optional: Only update sleep tracker fields if needed
            sleepTrackerRef.child("sleepDuration").setValue(0);
            sleepTrackerRef.child("sleepTime").setValue("0");
            sleepTrackerRef.child("wakeUpTime").setValue("0");
        }
    }
    private DatabaseReference getDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("tracker")
                .child("dailydata");
    }


    @Override
    public void onGoalUpdate(int position, boolean isDone) {
        updateCompletionRate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

}
