package com.example.grob_simpleaichatbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "profile_prefs";
    private static final String KEY_NAME  = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BIO   = "bio";

    private EditText etName, etEmail, etBio;
    private Button btnSave;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Uses your provided layout file:
        setContentView(R.layout.profile_pages);

        etName  = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etBio   = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved values; if empty, keep whatever default text the XML shows
        String savedName  = prefs.getString(KEY_NAME,  null);
        String savedEmail = prefs.getString(KEY_EMAIL, null);
        String savedBio   = prefs.getString(KEY_BIO,   null);

        if (!TextUtils.isEmpty(savedName))  etName.setText(savedName);
        if (!TextUtils.isEmpty(savedEmail)) etEmail.setText(savedEmail);
        if (!TextUtils.isEmpty(savedBio))   etBio.setText(savedBio);

        btnSave.setOnClickListener(v -> {
            final String name  = etName.getText().toString().trim();
            final String email = etEmail.getText().toString().trim();
            final String bio   = etBio.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Please enter your name");
                etName.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
                return;
            }

            prefs.edit()
                    .putString(KEY_NAME,  name)
                    .putString(KEY_EMAIL, email)
                    .putString(KEY_BIO,   bio)
                    .apply();

            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        });
    }
}
