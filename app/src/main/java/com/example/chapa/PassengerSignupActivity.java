package com.example.chapa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PassengerSignupActivity extends AppCompatActivity {

    private EditText nameInput, usernameInput, contactInput, passwordInput;
    private Button signupButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_signup);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameInput = findViewById(R.id.nameInput);
        usernameInput = findViewById(R.id.usernameInput);
        contactInput = findViewById(R.id.contactInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);

        // Set up signup button click listener
        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String username = usernameInput.getText().toString();
            String contact = contactInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (validateInputs(name, username, contact, password)) {
                registerPassenger(name, username, contact, password);
            }
        });
    }

    private boolean validateInputs(String name, String username, String contact, String password) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (contact.isEmpty()) {
            Toast.makeText(this, "Please enter your contact info", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerPassenger(String name, String username, String contact, String password) {
        // Create a new passenger object
        Map<String, Object> passenger = new HashMap<>();
        passenger.put("name", name);
        passenger.put("username", username);
        passenger.put("contactInfo", contact);
        passenger.put("password", password); // In production, hash the password!

        // Add the passenger to Firestore
        db.collection("passengers")
                .add(passenger)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this,PassengerLoginActivity.class);
                    startActivity(intent);
                    finish(); // Close the signup activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error during registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}