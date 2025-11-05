package com.example.grob_simpleaichatbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_NOTIF = "notifications";
    private static final String KEY_DARK = "dark_mode";
    private static final String KEY_TEXT_SIZE = "chat_text_size";   // int step 0..8
    private static final String KEY_LANG = "language";              // string code

    private SwitchCompat swNotifications, swDarkMode;
    private SeekBar seekTextSize;
    private Spinner spLanguage;
    private Button btnSave;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        swNotifications = findViewById(R.id.swNotifications);
        swDarkMode = findViewById(R.id.swDarkMode);
        seekTextSize = findViewById(R.id.seekTextSize);
        spLanguage = findViewById(R.id.spLanguage);
        btnSave = findViewById(R.id.btnSaveSettings);

        // Language spinner items (add or change as you like)
        String[] langs = new String[] { "System default", "English", "Malay" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, langs);
        spLanguage.setAdapter(adapter);

        // Load saved values
        boolean notif = prefs.getBoolean(KEY_NOTIF, true);
        boolean dark = prefs.getBoolean(KEY_DARK, false);
        int textSizeStep = prefs.getInt(KEY_TEXT_SIZE, 2);
        String lang = prefs.getString(KEY_LANG, "System default");

        swNotifications.setChecked(notif);
        swDarkMode.setChecked(dark);
        seekTextSize.setProgress(textSizeStep);
        int langIndex = 0;
        for (int i = 0; i < langs.length; i++) {
            if (langs[i].equals(lang)) { langIndex = i; break; }
        }
        spLanguage.setSelection(langIndex);

        btnSave.setOnClickListener(v -> {
            String selectedLang = (String) spLanguage.getSelectedItem();
            prefs.edit()
                    .putBoolean(KEY_NOTIF, swNotifications.isChecked())
                    .putBoolean(KEY_DARK, swDarkMode.isChecked())
                    .putInt(KEY_TEXT_SIZE, seekTextSize.getProgress())
                    .putString(KEY_LANG, selectedLang)
                    .apply();

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();

            // If you later implement dark mode or language switching at runtime,
            // you can recreate() or send a result back to the caller here.
        });
    }
}
