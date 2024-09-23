package com.yahya.shadow.chartRecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yahya.shadow.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LineChartAdapter extends RecyclerView.Adapter<LineChartAdapter.ViewHolder> {

    private final Context context;
    private final List<ChartData> chartDataList;

    public LineChartAdapter(Context context, List<ChartData> chartDataList) {
        this.context = context;
        this.chartDataList = chartDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.linechart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChartData chartData = chartDataList.get(position);
        holder.titleTextView.setText(chartData.getTitle());

        // Check if there are less than 3 entries
        if (chartData.getEntries().size() < 3) {
            holder.noData.setVisibility(View.VISIBLE);
            holder.lineChart.setVisibility(View.GONE);
        } else {
            holder.noData.setVisibility(View.GONE);
            holder.lineChart.setVisibility(View.VISIBLE);

            // Apply settings and data
            LineDataSet lineDataSet = new LineDataSet(chartData.getEntries(), "");
            lineDataSet.setColor(Color.WHITE);
            lineDataSet.setCircleColor(Color.WHITE);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            LineData lineData = new LineData(lineDataSet);
            lineData.setDrawValues(false);
            lineDataSet.setCircleRadius(5);
            holder.lineChart.getDescription().setEnabled(false);

            // Customize X-axis
            XAxis xAxis = holder.lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawLabels(false);

            // Customize Y-axis
            YAxis yAxisLeft = holder.lineChart.getAxisLeft();
            yAxisLeft.setDrawGridLines(false);
            yAxisLeft.setDrawLabels(false);
            yAxisLeft.setAxisMinimum(-1f); // Minimum value
            yAxisLeft.setAxisMaximum(11f); // Maximum value

            YAxis yAxisRight = holder.lineChart.getAxisRight();
            yAxisRight.setEnabled(false);  // Disable right Y-axis

            // Add horizontal line y=5
            LimitLine limitLine = new LimitLine(5f, "");
            limitLine.setLineWidth(3f);
            limitLine.enableDashedLine(10f, 10f, 0f);
            limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            limitLine.setTextSize(10f);
            yAxisLeft.addLimitLine(limitLine);

            holder.lineChart.setData(lineData);
            holder.lineChart.invalidate(); // Refresh chart

            holder.lineChart.setOnChartValueSelectedListener(holder);
        }
    }

    @Override
    public int getItemCount() {
        return chartDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OnChartValueSelectedListener {
        TextView titleTextView,noData;
        LineChart lineChart;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.chart_title);
            lineChart = itemView.findViewById(R.id.lineChart);
            cardView = itemView.findViewById(R.id.card_view);
            noData = itemView.findViewById(R.id.noData);
            GestureDetector gestureDetector;

            // Initialize GestureDetector
            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Double-tap detected, handle accordingly
                    handleLongPress(e);
                    return true;
                }
            });
            // Set the OnChartGestureListener to detect long press
            lineChart.setOnChartGestureListener(new OnChartGestureListener() {
                @Override
                public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                    // Do nothing
                }

                @Override
                public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                    // Do nothing
                }

                @Override
                public void onChartLongPressed(MotionEvent me) {
                    // Long press detected, handle accordingly
                    handleLongPress(me);
                }

                @Override
                public void onChartDoubleTapped(MotionEvent me) {
                    // Do nothing
                }

                @Override
                public void onChartSingleTapped(MotionEvent me) {
                    // Do nothing
                }

                @Override
                public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                    // Do nothing
                }

                @Override
                public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                    // Do nothing
                }

                @Override
                public void onChartTranslate(MotionEvent me, float dX, float dY) {
                    // Do nothing
                }
            });
            // Forward touch events to GestureDetector
            lineChart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            //showDetailsPopup(e);
        }

        @Override
        public void onNothingSelected() {

        }

        private void handleLongPress(MotionEvent e) {
            // Implement logic to show popup for long press event
            // Find the Entry corresponding to the long-pressed point
            Highlight highlight = lineChart.getHighlightByTouchPoint(e.getX(), e.getY());
            // Show popup with details of the selected entry
            if (highlight != null) {
                float xVal = highlight.getX(); // This is the index of the entry in the dataset

                // Get the corresponding Entry from the LineData
                List<ILineDataSet> dataSets = lineChart.getLineData().getDataSets();
                for (ILineDataSet dataSet : dataSets) {
                    LineDataSet lineDataSet = (LineDataSet) dataSet; // Cast to LineDataSet

                    // Find the entry closest to the touched position
                    Entry closestEntry = getClosestEntry(lineDataSet, xVal);
                    if (closestEntry != null) {
                        showDetailsPopup(closestEntry);
                        break; // Stop after finding the first valid entry
                    }
                }
            }
        }

        private Entry getClosestEntry(LineDataSet lineDataSet, float  timestampToFind) {
            List<Entry> entries = lineDataSet.getEntriesForXValue(timestampToFind);
            if (entries.isEmpty()) {
                return null;
            } else {
                // Find the closest entry to the timestamp
                Entry closestEntry = entries.get(0);
                for (Entry entry : entries) {
                    if (Math.abs(entry.getX() - timestampToFind) < Math.abs(closestEntry.getX() - timestampToFind)) {
                        closestEntry = entry;
                    }
                }
                return closestEntry;
            }
        }

        private void showDetailsPopup(Entry e) {
            // Implement your popup logic here
            // Example: AlertDialog to show details
            Dialog customDialog = new Dialog(itemView.getContext());
            customDialog.setContentView(R.layout.dialog_entry_details);

            // Set values in custom dialog
            TextView xValueTextView = customDialog.findViewById(R.id.x_value);
            TextView yValueTextView = customDialog.findViewById(R.id.y_value);

            String formattedDate = convertTimestampToDate(e.getX()*1000);

            xValueTextView.setText(formattedDate+":");
            yValueTextView.setText(String.valueOf(e.getY()));

            // Show dialog
            customDialog.show();
        }

        private String convertTimestampToDate(float x) {
            Date date = new Date((long) x);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }
}