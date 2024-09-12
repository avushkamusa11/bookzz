package com.fmi.bookzz.ui.user;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.MyLibraryAdapter;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.entity.User;
import com.fmi.bookzz.helper.Helper;
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

public class ProfileFragment extends Fragment {

    ImageView profilePictureIV;
    TextView profileNameTV;
    TextView usernameTV;
    Button sendMessageB;
    RecyclerView booksRV;
    User user;
    MyLibraryAdapter adapter;
    MainActivity activity;
    ExecutorService executorService;
    Handler mainHandler;
    public ProfileFragment(User user) {
        this.user = user;
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        activity = (MainActivity) getActivity();
        profilePictureIV = view.findViewById(R.id.profileImageIV);
        profileNameTV = view.findViewById(R.id.profileNameTV);
        usernameTV = view.findViewById(R.id.usernameTV);
        sendMessageB = view.findViewById(R.id.sendMessageB);
        booksRV = view.findViewById(R.id.books_recycler_view);
        profileNameTV.setText(user.getfName() + " " + user.getlName());
        usernameTV.setText(user.getUsername());
        if(user.getProfilePicture() != null ){
            Bitmap bitmap = Helper.decodeBase64ToBitmap(user.getProfilePicture());
            profilePictureIV.setImageBitmap(bitmap);
        }
        ArrayList<MyBook> books = new ArrayList<>();
        adapter = new MyLibraryAdapter(activity, getContext(), books);
        booksRV.setLayoutManager(new GridLayoutManager(getContext(), 2));
        booksRV.setAdapter(adapter);
        getBooks();
        return view;
    }

    public void getBooks() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_USERS_BOOKS, user.getId()));

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

                            newBook.setAuthors(authorsList);

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
                    mainHandler.post(() -> {
                    });
                }
            });
        });
    }
}
