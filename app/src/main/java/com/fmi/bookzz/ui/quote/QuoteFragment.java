package com.fmi.bookzz.ui.quote;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.MyQuoteAdapter;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.entity.Plan;
import com.fmi.bookzz.entity.Quote;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.books.BooksFragment;

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

public class QuoteFragment extends Fragment {

    EditText addQuoteET;
    Spinner selectBookSpinner;
    CheckBox addQuoteCB;
    Button  addQuoteB;
    Button viewOtherQuotesB;
    RecyclerView myQuotesRV;
    ExecutorService executorService;
    Handler mainHandler;
    private boolean isPrivateChecked;
    private MyQuoteAdapter adapter;
    List<Quote> quotes;

    public QuoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_quote, container, false);
        addQuoteET = view.findViewById(R.id.addQuoteET);
        selectBookSpinner = view.findViewById(R.id.selectBookSpinner);
        addQuoteCB = view.findViewById(R.id.addQuoteCB);
        addQuoteB = view.findViewById(R.id.addQuoteB);
        myQuotesRV = view.findViewById(R.id.myQuotesRV);
        quotes = new ArrayList<>();
        getQuotes();
//        adapter = new MyQuoteAdapter(getActivity(),getContext(),quotes);
//        myQuotesRV.setLayoutManager(new LinearLayoutManager(getContext()));
//        myQuotesRV.setAdapter(adapter);
    viewOtherQuotesB = view.findViewById(R.id.viewOtherQuotesB);
        getBooks();
        addQuoteCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isPrivateChecked =isChecked;
            }
        });

        addQuoteB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuote();
            }
        });
        viewOtherQuotesB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.content_main, new FriendsQuotesFragment());
                transaction.commit();
            }
        });
        return view;
    }

    private void getQuotes() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_MY_QUOTES, RequestHelper.token));

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
                            quote.setPrivate(quoteJSON.getBoolean("isPrivate"));

                            quotes.add(quote);
                             }

                        // Update the spinner on the main thread
                        mainHandler.post(() -> {
                            if (adapter == null) {
                                adapter = new MyQuoteAdapter(getActivity(), getContext(), quotes);
                                myQuotesRV.setLayoutManager(new LinearLayoutManager(getContext()));
                                myQuotesRV.setAdapter(adapter);
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

    private void getBooks() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_MY_BOOKS, RequestHelper.token));

        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    try {
                        JSONArray booksArray = new JSONArray(response);
                        List<String> allBooks = new ArrayList<>();

                        for (int i = 0; i < booksArray.length(); i++) {
                            JSONObject bookJson = booksArray.getJSONObject(i);
                            MyBook newBook = new MyBook();

                            newBook.setId(bookJson.getLong("id"));
                            newBook.setTitle(bookJson.getString("title"));

                            allBooks.add(newBook.getTitle());
                        }

                        // Update the spinner on the main thread
                        mainHandler.post(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, allBooks);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            selectBookSpinner.setAdapter(adapter);
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

    private void addQuote() {
        OkHttpClient client = new OkHttpClient();

        try {
            // Create a JSON object with the data
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("quoteText", addQuoteET.getText().toString());
            jsonObject.put("bookTitle", selectBookSpinner.getSelectedItem().toString());
            jsonObject.put("isPrivate", isPrivateChecked);

            // Convert the JSON object to a string
            String jsonString = jsonObject.toString();

            // Log the JSON string to verify its content
            Log.d("JSON Request", jsonString);
            // Create the RequestBody with the JSON string
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);

            // Build the URL string
            String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                    String.format(RequestHelper.ADD_QUOTE, RequestHelper.token));

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
                    Log.e("Add Quote Error", e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject planJson = new JSONObject(response.body().string());
                            Quote quote = new Quote();



                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("Add Quote Response", "Request failed with code: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}