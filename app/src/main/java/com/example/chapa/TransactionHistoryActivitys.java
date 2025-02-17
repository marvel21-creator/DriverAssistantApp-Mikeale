package com.example.chapa;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TransactionHistoryActivitys extends AppCompatActivity {
    private ListView transactionListView;
    private TransactionAdapter adapter;
    private FirebaseFirestore db;
    private List<PaymentTransaction> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history_activitys);

        transactionListView = findViewById(R.id.transactionListView);
        db = FirebaseFirestore.getInstance();
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList);
        transactionListView.setAdapter(adapter);

        setupFilterButtons();
        fetchTransactions(null, null);  // Fetch all by default
    }

    private void setupFilterButtons() {
        findViewById(R.id.filterDay).setOnClickListener(v -> fetchTransactions(getStartOfDay(), getEndOfDay()));
        findViewById(R.id.filterWeek).setOnClickListener(v -> fetchTransactions(getStartOfWeek(), getEndOfWeek()));
        findViewById(R.id.filterMonth).setOnClickListener(v -> fetchTransactions(getStartOfMonth(), getEndOfMonth()));
        findViewById(R.id.filterYear).setOnClickListener(v -> fetchTransactions(getStartOfYear(), getEndOfYear()));
    }

    private void fetchTransactions(Long startTime, Long endTime) {
        db.collection("transactions")
                .whereGreaterThanOrEqualTo("timestamp", startTime != null ? new Timestamp(new Date(startTime)) : Timestamp.now())
                .whereLessThanOrEqualTo("timestamp", endTime != null ? new Timestamp(new Date(endTime)) : Timestamp.now())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        transactionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String transactionId = document.getString("transactionId");
                            int amount = document.getLong("amount").intValue();
                            String phoneNumber = document.getString("phoneNumber");
                            String paymentMethod = document.getString("paymentMethod");
                            Timestamp timestamp = document.getTimestamp("timestamp");
                            String status = document.getString("status");

                            PaymentTransaction transaction = new PaymentTransaction(transactionId, amount, phoneNumber, paymentMethod, timestamp, status);
                            transactionList.add(transaction);
                        }

                        // Sort transactions by date (newest first)
                        Collections.sort(transactionList, Comparator.comparing(PaymentTransaction::getTimestamp).reversed());

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    private long getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    private long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    private long getStartOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }
}
