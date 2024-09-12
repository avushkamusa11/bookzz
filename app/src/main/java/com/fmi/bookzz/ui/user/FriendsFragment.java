package com.fmi.bookzz.ui.user;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.UsersAdapter;
import com.fmi.bookzz.entity.User;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendsFragment extends Fragment {
    RecyclerView friendsRV;
    EditText searchFriendET;
    ImageButton searchB;
    Button viewUsersB;
    UsersAdapter adapter;
    MainActivity activity;
    ExecutorService executorService;
    Handler mainHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsRV = view.findViewById(R.id.allFriendsRV);
        searchFriendET = view.findViewById(R.id.searchFriendET);
        searchB = view.findViewById(R.id.searchFriendB);
        viewUsersB = view.findViewById(R.id.usersB);
        List<User> users = new ArrayList<>();
        activity= (MainActivity) getActivity();
        adapter = new UsersAdapter(activity,getContext(),users, "MESSAGE");
        friendsRV.setLayoutManager(new GridLayoutManager(getContext(),2));
        friendsRV.setAdapter(adapter);
        getUsers();
        viewUsersB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.content_main, new UsersFragment());
                transaction.commit();
            }
        });
        return  view;
    }

    private void getUsers() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_ALL_FRIENDS, RequestHelper.token));

        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    try {
                        JSONArray usersArray = new JSONArray(response);
                        ArrayList<User> allUsers = new ArrayList<>();

                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject bookJson = usersArray.getJSONObject(i);
                            User user = new User();

                            user.setId(bookJson.getLong("id"));
                            user.setfName(bookJson.getString("fName"));
                            user.setlName(bookJson.getString("lName"));
                            user.setUsername(bookJson.getString("username"));
                            user.setProfilePicture(bookJson.getString("profilePicture"));

                            allUsers.add(user);
                        }

                        mainHandler.post(() -> adapter.addAndUpdate(allUsers, true));
                        Log.wtf("response", response);
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
}