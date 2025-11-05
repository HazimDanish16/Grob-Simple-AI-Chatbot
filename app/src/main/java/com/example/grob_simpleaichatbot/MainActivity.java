package com.example.grob_simpleaichatbot;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private EditText editMessage;
    private ImageView btnSend;
    private ImageView btnProfile;
    private ImageView btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);

        btnSend.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please type a message first!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Message sent: " + message, Toast.LENGTH_SHORT).show();
                editMessage.setText(""); // clear input
            }
        });


        btnProfile.setOnClickListener(v ->
                Toast.makeText(this, "Profile screen will open here", Toast.LENGTH_SHORT).show()
        );

        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings screen will open here", Toast.LENGTH_SHORT).show()
        );
    }
}
