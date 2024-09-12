package com.fmi.bookzz.ui.myLibrary;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.BookAdapter;
import com.fmi.bookzz.adapter.MyLibraryAdapter;
import com.fmi.bookzz.entity.Book;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyLibraryFragment extends Fragment {

    RecyclerView booksRV;
    MyLibraryAdapter adapter;
    MainActivity activity;
    ExecutorService executorService;
    Handler mainHandler;

    public MyLibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_library,container,false);
        booksRV = view.findViewById(R.id.myLibraryRV);
        activity = (MainActivity) getActivity();
        ArrayList<MyBook> books = new ArrayList<>();
        adapter = new MyLibraryAdapter(activity,getContext(), books);
        booksRV.setLayoutManager(new GridLayoutManager(getContext(), 2));
        booksRV.setAdapter(adapter);
        getBooks();
        return view;
    }

    public void getBooks() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_MY_BOOKS, RequestHelper.token));

        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    try {
                        JSONArray booksArray = new JSONArray(response);
                        ArrayList<MyBook> allBooks = new ArrayList<>();

                        for (int i = 0; i < booksArray.length(); i++) {
                            JSONObject bookJson = booksArray.getJSONObject(i);
                            MyBook newBook = new MyBook();

                            newBook.setId(bookJson.getLong("id"));
                            newBook.setTitle(bookJson.getString("title"));
                            newBook.setStatus(bookJson.getString("status"));
                            newBook.setBookImage(bookJson.getString("bookImage"));
                            newBook.setBookId(bookJson.getLong("bookId"));

                            JSONArray authorsArray = bookJson.getJSONArray("authors");
                            List<String> authorsList = new ArrayList<>();

                            for (int j = 0; j < authorsArray.length(); j++) {
                                authorsList.add(authorsArray.getString(j));
                            }

                            newBook.setAuthors(authorsList); // Ensure setAuthors accepts List<String>

                            allBooks.add(newBook);
                        }

                        mainHandler.post(() -> adapter.addAndUpdate(allBooks, true));
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