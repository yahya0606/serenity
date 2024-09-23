package com.yahya.shadow.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yahya.shadow.R;
import com.yahya.shadow.journals.MemoAdapter;
import com.yahya.shadow.journals.Memo;
import com.yahya.shadow.journals.Memo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private List<Memo> memos;
    private MemoAdapter memoAdapter;
    private static final String MEMO_FILE_NAME = "memos.txt";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.memos);
        FloatingActionButton fabAddMemo = view.findViewById(R.id.addMemo);

        memos = loadMemos();
        memoAdapter = new MemoAdapter(getContext(), memos);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(memoAdapter);

        fabAddMemo.setOnClickListener(v -> showAddMemoDialog());
        memoAdapter.setOnItemClickListener(this::showMemoDetails);

        return view;
    }

    private List<Memo> loadMemos() {
        List<Memo> memos = new ArrayList<>();
        try (FileInputStream fis = getContext().openFileInput(MEMO_FILE_NAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 3) {
                    String title = parts[0];
                    String body = parts[1];
                    long timestamp = Long.parseLong(parts[2]);
                    memos.add(new Memo(title, body, timestamp));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memos;
    }

    private void showMemoDetails(Memo memo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_memo_details, null);
        builder.setView(dialogView);

        TextView titleTextView = dialogView.findViewById(R.id.dialogTitleTextView);
        TextView bodyTextView = dialogView.findViewById(R.id.dialogBodyTextView);
        TextView timestampTextView = dialogView.findViewById(R.id.dialogTimestampTextView);

        titleTextView.setText(memo.getTitle());
        bodyTextView.setText(memo.getBody());
        timestampTextView.setText(formatTimestamp(memo.getTimestamp()));

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


    private void saveMemos() {
        try (FileOutputStream fos = getContext().openFileOutput(MEMO_FILE_NAME, Context.MODE_PRIVATE);
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter bw = new BufferedWriter(osw)) {
            for (Memo memo : memos) {
                bw.write(memo.getTitle() + "\t" + memo.getBody() + "\t" + memo.getTimestamp());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddMemoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Memo");

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
                Memo newMemo = new Memo(title, body, System.currentTimeMillis());
                memos.add(newMemo);
                memoAdapter.notifyItemInserted(memos.size() - 1);
                saveMemos();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
