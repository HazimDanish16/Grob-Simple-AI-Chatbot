package com.example.grob_simpleaichatbot;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    private static final String OPENROUTER_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private ChatAdapter chatAdapter;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private CircularProgressIndicator loadingIndicator;
    private RecyclerView chatRecyclerView;
    private OkHttpClient httpClient;
    private boolean isRequestInFlight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initHttpClient();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> attemptSendMessage());

        if (messageInput != null) {
            messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        attemptSendMessage();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void initHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    private void attemptSendMessage() {
        if (isRequestInFlight || messageInput == null) {
            return;
        }

        CharSequence input = messageInput.getText();
        String userMessage = input != null ? input.toString().trim() : "";
        if (TextUtils.isEmpty(userMessage)) {
            return;
        }

        messageInput.setText("");
        hideKeyboard();
        chatAdapter.addMessage(new Message(userMessage, true));
        scrollToBottom();
        sendMessageToOpenRouter(userMessage);
    }

    private void sendMessageToOpenRouter(String prompt) {
        final String apiKey = BuildConfig.OPENROUTER_API_KEY;
        if (TextUtils.isEmpty(apiKey)) {
            chatAdapter.addMessage(new Message(getString(R.string.message_api_key_missing), false));
            scrollToBottom();
            return;
        }

        setLoading(true);
        final int placeholderPosition = chatAdapter.addMessage(new Message(getString(R.string.message_thinking), false));
        scrollToBottom();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("model", "openrouter/auto");
            JSONArray messages = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant for the Grob Simple AI Chatbot Android app.");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            requestBody.put("messages", messages);
        } catch (JSONException e) {
            updateBotMessage(placeholderPosition, getString(R.string.message_error_generic));
            setLoading(false);
            return;
        }

        RequestBody body = RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE);

        Request request = new Request.Builder()
                .url(OPENROUTER_ENDPOINT)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://github.com/")
                .addHeader("X-Title", getString(R.string.app_name))
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                updateBotMessage(placeholderPosition, getString(R.string.message_error_generic));
                setLoading(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resultMessage;
                if (!response.isSuccessful() || response.body() == null) {
                    resultMessage = getString(R.string.message_error_generic);
                } else {
                    String responseBody = response.body().string();
                    resultMessage = parseAssistantReply(responseBody);
                    if (resultMessage != null) {
                        resultMessage = resultMessage.trim();
                    }
                }

                if (TextUtils.isEmpty(resultMessage)) {
                    resultMessage = getString(R.string.message_empty_response);
                }

                updateBotMessage(placeholderPosition, resultMessage);
                setLoading(false);
            }
        });
    }

    private String parseAssistantReply(String responseBody) {
        try {
            JSONObject root = new JSONObject(responseBody);
            JSONArray choices = root.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                return null;
            }
            JSONObject firstChoice = choices.optJSONObject(0);
            if (firstChoice == null) {
                return null;
            }
            JSONObject message = firstChoice.optJSONObject("message");
            if (message == null) {
                return null;
            }
            return message.optString("content");
        } catch (JSONException e) {
            return null;
        }
    }

    private void updateBotMessage(int position, String message) {
        runOnUiThread(() -> {
            chatAdapter.updateMessage(position, new Message(message, false));
            scrollToBottom();
        });
    }

    private void setLoading(boolean loading) {
        runOnUiThread(() -> {
            isRequestInFlight = loading;
            if (sendButton != null) {
                sendButton.setEnabled(!loading);
            }
            if (loadingIndicator != null) {
                loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void scrollToBottom() {
        runOnUiThread(() -> {
            if (chatRecyclerView != null && chatAdapter.getItemCount() > 0) {
                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void hideKeyboard() {
        if (messageInput == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
        }
    }
}