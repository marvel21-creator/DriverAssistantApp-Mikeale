package com.example.chapa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverActivity extends AppCompatActivity {

    private EditText sourceInput, destinationInput, fareInput;
    private Button updateButton;
    private ListView transactionList;
    private TextView successfulPaymentCount;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        sourceInput = findViewById(R.id.source_input);
        destinationInput = findViewById(R.id.destination_input);
        fareInput = findViewById(R.id.fare_input);
        updateButton = findViewById(R.id.update_button);
        transactionList = findViewById(R.id.transaction_list);
        successfulPaymentCount = findViewById(R.id.successfulPaymentCount);

        // Fetch and display current details
        fetchCurrentDetails();

        // Set up update button click listener
        updateButton.setOnClickListener(v -> {
            String source = sourceInput.getText().toString();
            String destination = destinationInput.getText().toString();
            String fare = fareInput.getText().toString();

            if (validateInputs(source, destination, fare)) {
                updateDetails(source, destination, fare);
            }
        });

        // Fetch and display transaction history
        fetchTransactionHistory();
    }

    private void fetchCurrentDetails() {
        db.collection("paymentDetail").document("detail")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String source = documentSnapshot.getString("source");
                        String destination = documentSnapshot.getString("destination");
                        String fare = documentSnapshot.getString("fare");

                        sourceInput.setText(source);
                        destinationInput.setText(destination);
                        fareInput.setText(fare);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String source, String destination, String fare) {
        if (source.isEmpty()) {
            Toast.makeText(this, "Please enter a source", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fare.isEmpty()) {
            Toast.makeText(this, "Please enter a fare", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateDetails(String source, String destination, String fare) {
        Map<String, Object> details = new HashMap<>();
        details.put("source", source);
        details.put("destination", destination);
        details.put("fare", fare);

        db.collection("paymentDetail").document("detail")
                .set(details)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTransactionHistory() {
        db.collection("transactions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<PaymentTransaction> transactions = new ArrayList<>();
                        int successfulCount = 0; // Counter for successful payments
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String transactionId = document.getString("transactionId");
                            int amount = document.getLong("amount").intValue();
                            String paymentMethod = document.getString("paymentMethod");
                            String phoneNumber = document.getString("phoneNumber");
                            String status = document.getString("status");
                            Timestamp timestamp = document.getTimestamp("timestamp");

                            transactions.add(new PaymentTransaction(transactionId, amount, phoneNumber, paymentMethod, timestamp, status));

                            // Count successful payments
                            if ("Success".equalsIgnoreCase(status)) {
                                successfulCount++;
                            }
                        }

                        // Update the successful payment count TextView
                        successfulPaymentCount.setText("Successful Payments: " + successfulCount);

                        // Display transactions in a ListView
                        TransactionAdapter adapter = new TransactionAdapter(this, transactions);
                        transactionList.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "Failed to fetch transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}