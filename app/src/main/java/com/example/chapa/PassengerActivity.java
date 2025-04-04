package com.example.chapa;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PassengerActivity extends AppCompatActivity {
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
    private double fareAmount;

    // Stripe Payment Variables
    private PaymentSheet paymentSheet;
    private String customerId, ephemeralKey, clientSecret;
    String SECRET_KEY = "sk_test_51QvRIL09YM7VAtiKdPc5YMNPW3Lnpy7tZNSJA06vgtjAdFynXlPrSE17OMnaMyZE1JGW7VQRLF0tabr0F1mECItw00nHQ4b3JQ";
    String PUBLISH_KEY = "pk_test_51QvRIL09YM7VAtiKrYR3yfSKfRRzG6GdyZE237adc0nvhg9r7IgmHmuxQtt9C6hjMwY6M45YgONvhmCgMMHN83sq00H2nqXAdI";
    private Button loginButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Initialize UI elements
        loginButton = findViewById(R.id.loginButton);

        // Check login state
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        updateLoginButton(isLoggedIn);

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            if (isLoggedIn) {
                // Logout logic
                logout();
            } else {
                // Redirect to login activity
                Intent intent = new Intent(this, PassengerLoginActivity.class);
                startActivity(intent);
            }
        });

        TextView transactionHistoryTextView = findViewById(R.id.transactionHistory);
        transactionHistoryTextView.setOnClickListener(v -> showTransactionHistory());

        // Initialize Firebase and Stripe
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        db = FirebaseFirestore.getInstance();
        PaymentConfiguration.init(this, PUBLISH_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Initialize UI elements
        initializeUIElements();

        // Initialize Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        findMyCarButton.setOnClickListener(v -> {
            Intent intent = new Intent(PassengerActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        Button initiatePaymentButton = findViewById(R.id.initiatePaymentButton);
        initiatePaymentButton.setOnClickListener(v -> showPaymentInputDialog());

        findViewById(R.id.home).setOnClickListener(view -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.contactMe).setOnClickListener(v -> showContactInfo());
        findViewById(R.id.help).setOnClickListener(v -> showHelpDialog());

        // Fetch payment details from Firestore
        fetchPaymentDetails();
    }

    private void updateLoginButton(boolean isLoggedIn) {
        if (isLoggedIn) {
            loginButton.setText("Logout");
        } else {
            loginButton.setText("Login");
        }
    }

    private void logout() {
        // Update login state
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Redirect to login activity
        Intent intent = new Intent(PassengerActivity.this, PassengerLoginActivity.class);
        startActivity(intent);
        //finish(); // Close the current activity

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showTransactionHistory() {
        Intent intent = new Intent(this, TransactionHistoryActivitys.class);
        startActivity(intent);
    }

    private void showRoleSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Role");

        String[] roles = {"Passenger", "Driver"};
        builder.setItems(roles, (dialog, which) -> {
            if (which == 0) {
                // Passenger selected
                Toast.makeText(this, "Logged in as Passenger", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, PassengerLoginActivity.class);
                startActivity(intent);
            } else {
                // Driver selected
                showDriverLoginDialog();
            }
        });

        builder.show();
    }

    private void showDriverLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Driver Login");

        final EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(usernameInput);
        layout.addView(passwordInput);

        builder.setView(layout);
        builder.setPositiveButton("Login", (dialog, which) -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            authenticateDriver(username, password);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void authenticateDriver(String username, String password) {
        db.collection("driver")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password) // In production, use hashed passwords!
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Save login state
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        Toast.makeText(this, "Logged in as Driver", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, DriverActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error during authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showContactInfo() {
        new AlertDialog.Builder(this)
                .setTitle("Contact Me")
                .setMessage("You can contact us at: support@example.com")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage("For assistance, please visit our help center or call support.")
                .setPositiveButton("OK", null)
                .show();
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
                        Toast.makeText(PassengerActivity.this, "Failed to fetch payment details!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPaymentInputDialog() {
        inputDialog = new Dialog(this);
        inputDialog.setContentView(R.layout.dialog_payment_input);

        EditText amountEditText = inputDialog.findViewById(R.id.amountInputPayment);
        EditText phoneNumberEditText = inputDialog.findViewById(R.id.phoneInput);

        // Set the fare amount and make it non-editable
        amountEditText.setText(String.valueOf((int) fareAmount));
        amountEditText.setFocusable(false);
        amountEditText.setClickable(false);
        amountEditText.setCursorVisible(false);
        amountEditText.setLongClickable(false);

        Button confirmButton = inputDialog.findViewById(R.id.confirmPaymentButton);
        confirmButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String phoneNumber = phoneNumberEditText.getText().toString();

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            int amount = Integer.parseInt(amountStr);
            createCustomer(amount, phoneNumber); // Start Stripe payment flow
            inputDialog.dismiss();
        });

        inputDialog.show();
    }

    private void createCustomer(int amount, String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/customers",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        customerId = object.getString("id");
                        getEphemeralKey(customerId, amount, phoneNumber);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PassengerActivity.this, "Failed to create customer", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SECRET_KEY);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getEphemeralKey(String customerId, int amount, String phoneNumber) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        ephemeralKey = object.getString("secret");
                        getClientSecret(customerId, ephemeralKey, amount, phoneNumber);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PassengerActivity.this, "Failed to get ephemeral key", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SECRET_KEY);
                headers.put("Stripe-Version", "2023-10-16");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customerId, String ephemeralKey, int amount, String phoneNumber) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/payment_intents",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        clientSecret = object.getString("client_secret");
                        progressBar.setVisibility(View.GONE);
                        PaymentFlow(amount, phoneNumber); // Start payment flow
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PassengerActivity.this, "Failed to get client secret", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SECRET_KEY);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("amount", String.valueOf(amount * 100)); // Amount in cents
                params.put("currency", "usd");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void PaymentFlow(int amount, String phoneNumber) {
        if (clientSecret == null || clientSecret.isEmpty()) {
            Toast.makeText(this, "Client Secret is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        paymentSheet.presentWithPaymentIntent(
                clientSecret, new PaymentSheet.Configuration("Birr Ride APP",
                        new PaymentSheet.CustomerConfiguration(customerId, ephemeralKey))
        );
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            logTransaction((int) fareAmount, "User Phone Number"); // Log successful transaction
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void logTransaction(int amount, String phoneNumber) {
        String transactionId = "TX" + System.currentTimeMillis();
        Timestamp timestamp = Timestamp.now();
        String status = "Success";

        PaymentTransaction transaction = new PaymentTransaction(transactionId, amount, phoneNumber, "Stripe", timestamp, status);
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
}