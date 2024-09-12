package com.fmi.bookzz.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fmi.bookzz.R;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;

import org.json.JSONException;

public class LogoutFragment extends Fragment {
    Handler mainHandler;
    ProgressDialog dialog;

    public LogoutFragment() {
        // Required empty public constructor
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler(Looper.getMainLooper());
        dialog = new ProgressDialog(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logout, container, false);
        dialog.setTitle("Loging out...");
        dialog.show();
        logout();
        return view;
    }

    private void logout() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.LOGOUT,RequestHelper.token));
        RequestHelper.requestService(urlString, "POST", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                //  JSONArray usersArray = new JSONArray(response);
                Log.wtf("response", response);
            }

            @Override
            public void onError(String error) {

            }
        });
        mainHandler.post(() -> {
            // Assuming `getAdapterPosition()` provides the correct position
            startActivity(new Intent(getActivity(), LoginActivity.class));

        });

    }
}