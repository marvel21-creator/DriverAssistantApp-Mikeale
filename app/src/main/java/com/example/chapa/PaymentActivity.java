package com.example.chapa;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.Timestamp;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;//to create, read, update, and delete data in your Firestore database.
import com.google.firebase.firestore.DocumentSnapshot;//You’ll use it to access data retrieved from Firestore queries or document reads.

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAnalytics mFirebaseAnalytics;

    // UI Elements
    private TextView paymentStatusTextView;
    private TextView paymentMessageTextView;
    private TextView paymentAmountTextView;
    private TextView sourceTextView;
    private TextView destinationTextView;
    private TextView fareTextView;
    private ProgressBar progressBar;
    private Button findMyCarButton;
    private Dialog inputDialog;

    // Location variables
    private FusedLocationProviderClient fusedLocationClient;
    private List<Payment> paymentHistory = new ArrayList<>();
    private double fareAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Find the transaction history TextView
        TextView transactionHistoryTextView = findViewById(R.id.transactionHistory);
        transactionHistoryTextView.setOnClickListener(v -> showTransactionHistory());

        // Initialize Firebase instances
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        initializeUIElements();

        // Initialize Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        findMyCarButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // Fetch source, destination, and fare from Firestore
        fetchPaymentDetails();

        Button initiatePaymentButton = findViewById(R.id.initiatePaymentButton);
        initiatePaymentButton.setOnClickListener(v -> showPaymentInputDialog());
//home
        findViewById(R.id.home).setOnClickListener(view -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            startActivity(intent);
        });
        // Contact Me
        findViewById(R.id.contactMe).setOnClickListener(v -> showContactInfo());

        // Help
        findViewById(R.id.help).setOnClickListener(v -> showHelpDialog());
    }
    private void showContactInfo() {
        // Show a dialog with contact information
        new AlertDialog.Builder(this)
                .setTitle("Contact Me")
                .setMessage("You can contact us at: marvelethio2026@gmail.com or call:+251907492641")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHelpDialog() {
        // Show a dialog with help information
        new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage("For assistance, please visit our help center or call support.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showTransactionHistory() {
        Intent intent = new Intent(this, TransactionHistoryActivity.class);
        startActivity(intent);
    }

    private void initializeUIElements() {
        paymentStatusTextView = findViewById(R.id.paymentStatus);
        paymentMessageTextView = findViewById(R.id.paymentMessage);
        paymentAmountTextView = findViewById(R.id.paymentAmount);
        sourceTextView = findViewById(R.id.source_text);
        destinationTextView = findViewById(R.id.destination_text);
        fareTextView = findViewById(R.id.fare_text);
        progressBar = findViewById(R.id.progressBar);
        findMyCarButton = findViewById(R.id.findmycar);
    }

    private void fetchPaymentDetails() {
        db.collection("paymentDetail").document("detail")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String source = document.getString("source");
                        String destination = document.getString("destination");
                        fareAmount = Double.parseDouble(document.getString("fare"));
                        fareTextView.setText(fareAmount + " Birr");

                        sourceTextView.setText(source);
                        destinationTextView.setText(destination);
                    } else {
                        Toast.makeText(PaymentActivity.this, "Failed to fetch payment details!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPaymentInputDialog() {
        inputDialog = new Dialog(this);
        inputDialog.setContentView(R.layout.dialog_payment_input);

        EditText amountEditText = inputDialog.findViewById(R.id.amountInputPayment);
        EditText phoneNumberEditText = inputDialog.findViewById(R.id.phoneInput);
        Spinner paymentMethodSpinner = inputDialog.findViewById(R.id.paymentMethodSpinner);

        amountEditText.setText(String.valueOf((int) fareAmount)); // Cast to int if needed

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(adapter);

        Button confirmButton = inputDialog.findViewById(R.id.confirmPaymentButton);
        confirmButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String phoneNumber = phoneNumberEditText.getText().toString();
            String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();

            if (amountStr.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int amount = Integer.parseInt(amountStr);
            makePayment(amount, phoneNumber, paymentMethod);
            inputDialog.dismiss();
        });

        inputDialog.show();
    }

    private void makePayment(int amount, String phoneNumber, String paymentMethod) {
        paymentStatusTextView.setVisibility(View.GONE);
        paymentMessageTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Simulating payment processing
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            boolean paymentSuccess = simulatePayment(amount); // Implement your payment logic here

            if (paymentSuccess) {
                // Save phone number in shared preferences after a successful payment
                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("userPhoneNumber", phoneNumber)
                        .apply();

                paymentHistory.add(new Payment(amount, phoneNumber, paymentMethod, "Payment successful")); // Optional, keep for history
                displayPaymentMessage(paymentMethod, amount, true); // Display success message
                logTransaction(amount, phoneNumber, paymentMethod); // Log the transaction
            } else {
                displayPaymentMessage(paymentMethod, amount, false); // Display failure message
                logTransaction(amount, phoneNumber, paymentMethod); // Log failure transaction
            }
        }, 2000); // Simulate processing delay
    }

    // Simulate payment success or failure
    private boolean simulatePayment(int amount) {
        return Math.random() < 0.8; // 80% chance of success for simulation
    }

    private void displayPaymentMessage(String paymentMethod, int amount, boolean isSuccess) {
        String status = isSuccess ? "Success" : "Failed";
        String formattedMessage = String.format("Status: %s\nPayment Sent: %d Birr\nPayment Method: %s\nDate: %s",
                status, amount, paymentMethod, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        paymentMessageTextView.setText(formattedMessage);
        paymentMessageTextView.setVisibility(View.VISIBLE);

        if (isSuccess) {
            paymentMessageTextView.setTextColor(getResources().getColor(R.color.successColor)); // Ensure this resource exists
        } else {
            paymentMessageTextView.setTextColor(getResources().getColor(R.color.errorColor)); // Ensure this resource exists
        }
    }

    private void logTransaction(int amount, String phoneNumber, String paymentMethod) {
        String transactionId = "TX" + System.currentTimeMillis();
        Timestamp timestamp = Timestamp.now(); // Use Firestore's Timestamp
        String status = "Success";

        PaymentTransaction transaction = new PaymentTransaction(transactionId, amount, phoneNumber, paymentMethod, timestamp, status);
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    Bundle bundle = new Bundle();
                    bundle.putDouble("amount", amount);
                    bundle.putString("transaction_id", transactionId);
                    mFirebaseAnalytics.logEvent("transaction_success", bundle);
                    paymentStatusTextView.setText("Transaction recorded successfully!");
                })
                .addOnFailureListener(e -> {
                    paymentStatusTextView.setText("Error recording transaction: " + e.getMessage());
                });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d("CurrentLocation", "Latitude: " + currentLocation.latitude + ", Longitude: " + currentLocation.longitude);
                    } else {
                        Toast.makeText(this, "Unable to fetch location. Ensure location services are enabled.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}