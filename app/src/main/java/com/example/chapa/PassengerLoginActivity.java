package com.example.chapa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class PassengerLoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView signupLink;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passenger_activity_login);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Initialize UI elements
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            authenticatePassenger(username, password);
        });

        // Set up signup link click listener
        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(PassengerLoginActivity.this, PassengerSignupActivity.class);
            startActivity(intent);
        });
    }

    private void authenticatePassenger(String username, String password) {
        db.collection("passengers")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password) // In production, use hashed passwords!
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Save login state
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        Toast.makeText(this, "Logged in as Passenger", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, PassengerActivity.class); // Redirect to passenger dashboard
                        startActivity(intent);
                        finish(); // Close the login activity
                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error during authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}