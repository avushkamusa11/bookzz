package com.fmi.bookzz.ui.quote;

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

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.FriendsQuotesAdapter;
import com.fmi.bookzz.adapter.MyQuoteAdapter;
import com.fmi.bookzz.entity.Quote;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendsQuotesFragment extends Fragment {


    RecyclerView quotesRV;
    ExecutorService executorService;
    Handler mainHandler;
    FriendsQuotesAdapter adapter;
    List<Quote> quotes;

    public FriendsQuotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        quotes = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends_quotes, container, false);
        quotesRV = view.findViewById(R.id.friendsQuotesRV);
        getQuotes();
        return view;
    }

    private void getQuotes() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_QUOTES));

        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    try {
                        JSONArray quotesArray = new JSONArray(response);

                        for (int i = 0; i < quotesArray.length(); i++) {
                            JSONObject quoteJSON = quotesArray.getJSONObject(i);
                            Quote quote  = new Quote();
                            quote.setQuoteText(quoteJSON.getString("quoteText"));
                            quote.setId(quoteJSON.getLong("id"));
                            quote.setBookTitle(quoteJSON.getString("bookTitle"));
                            quote.setUsername(quoteJSON.getString("username"));

                            quotes.add(quote);
                        }

                        // Update the spinner on the main thread
                        mainHandler.post(() -> {
                            if (adapter == null) {
                                adapter = new FriendsQuotesAdapter(getActivity(), getContext(), quotes);
                                quotesRV.setLayoutManager(new LinearLayoutManager(getContext()));
                                quotesRV.setAdapter(adapter);
                            } else {
                                adapter.notifyDataSetChanged();  // Update existing adapter
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("JSON Parsing Error", "Error parsing JSON response", e);
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("Request Error", error);
                }
            });
        });
    }
}