package com.fmi.bookzz.ui.chat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.FriendsQuotesAdapter;
import com.fmi.bookzz.adapter.SingleChatAdapter;
import com.fmi.bookzz.entity.ChatMessage;
import com.fmi.bookzz.entity.Quote;
import com.fmi.bookzz.entity.User;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UserChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private SingleChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private List<ChatMessage> messageList = new ArrayList<>();
    ExecutorService executorService;
    Handler mainHandler;
    User user;



    public UserChatFragment(User user) {
       this.user = user;
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);
        recyclerView = view.findViewById(R.id.chat_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        chatAdapter = new SingleChatAdapter(messageList);  // Create the adapter with the empty list
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(chatAdapter);

        getMessages();


        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString();
            if (!messageText.isEmpty()) {
                messageInput.setText("");
                sendMessage(messageText);

                messageInput.setText("");
                getMessages();
                }
        });
        return view;
    }

    private void getMessages() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_MESSAGES,user.getId(), RequestHelper.token));

        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    try {
                        JSONArray chatArray = new JSONArray(response);
                        ArrayList<ChatMessage> messages = new ArrayList<>();
                        messageList.clear();
                        for (int i = 0; i < chatArray.length(); i++) {
                            JSONObject messageJson = chatArray.getJSONObject(i);
                            ChatMessage chat = new ChatMessage();

                            chat.setText(messageJson.getString("messageText"));
                            chat.setSender(messageJson.getBoolean("isSender"));

                            messages.add(chat);
                        }
                        messageList.addAll(messages);

                        mainHandler.post(() -> {
//                            if (chatAdapter == null) {
//                                chatAdapter = new SingleChatAdapter(messageList);
//                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//                                recyclerView.setAdapter(chatAdapter);
//                                chatAdapter.notifyDataSetChanged();
//                            } else {
//                                chatAdapter.notifyDataSetChanged();  // Update existing adapter
//                            }
                            chatAdapter.notifyDataSetChanged();  // Notify adapter about the new data
                            recyclerView.smoothScrollToPosition(messageList.size() - 1);  // Scroll to bottom

                        });  Log.wtf("response", response);
                    } catch (JSONException e) {
                        Log.e("JSON Parsing Error", "Error parsing JSON response", e);
                    }
                }

                @Override
                public void onError(String error) {
                    // Handle the error on the main thread if necessary
                    mainHandler.post(() -> {
                        // Show error message or perform some UI action
                    });
                }
            });
        });
    }

    private void sendMessage(String messageText) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Create a JSON object with the data
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("reciever", user.getId());
            jsonObject.put("message", messageText);

            // Convert the JSON object to a string
            String jsonString = jsonObject.toString();

            // Log the JSON string to verify its content
            Log.d("JSON Request", jsonString);
            // Create the RequestBody with the JSON string
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);

            // Build the URL string
            String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                    String.format(RequestHelper.SEND_MESSAGE, RequestHelper.token));

            // Build the request
            Request request = new Request.Builder()
                    .url(urlString)
                    .post(requestBody)  // Use POST method with the JSON body
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    // Handle the error
                    Log.e("Send message Error", e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                    } else {
                        Log.e("Send message Response", "Request failed with code: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}