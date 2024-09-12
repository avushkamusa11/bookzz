package com.fmi.bookzz.ui.books;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.ui.MainActivity;
import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.BookAdapter;
import com.fmi.bookzz.entity.Book;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class BooksFragment extends Fragment {

    private BooksFragment binding;
    RecyclerView booksRV;
    BookAdapter adapter;
    MainActivity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books,container,false);
        booksRV = view.findViewById(R.id.booksRV);

        activity = (MainActivity) getActivity();
        ArrayList<Book> books = new ArrayList<>();
        adapter = new BookAdapter(activity,getContext(), books);
        booksRV.setLayoutManager(new LinearLayoutManager(getContext()));
        booksRV.setAdapter(adapter);
        getBooks();
        return  view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void getBooks(){
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_ALL_BOOKS,RequestHelper.token));

        RequestHelper.requestService(urlString, "GET", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                //JSONObject jsonResponse = new JSONObject(response);
                try {
                    JSONArray booksArray = new JSONArray(response);
                    ArrayList<Book> allBooks = new ArrayList<>();

                    for (int i = 0; i < booksArray.length(); i++) {
                        JSONObject bookJson = booksArray.getJSONObject(i);
                        Book newBook = new Book();

                        newBook.setId(bookJson.getLong("id"));
                        newBook.setTitle(bookJson.getString("title"));
                        newBook.setRate(bookJson.getDouble("rate"));
                        newBook.setFile(bookJson.getString("file"));

                        JSONArray authorsArray = bookJson.getJSONArray("authors");
                        List<String> authorsList = new ArrayList<>();

                        for (int j = 0; j < authorsArray.length(); j++) {
                            authorsList.add(authorsArray.getString(j));
                        }

                        newBook.setAuthors(authorsList); // Ensure setAuthors accepts List<String>

                        allBooks.add(newBook);
                    }

                    adapter.addAndUpdate(allBooks, true);

                    Log.wtf("response", response);
                } catch (JSONException e) {
                    Log.e("JSON Parsing Error", "Error parsing JSON response", e);
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }
}