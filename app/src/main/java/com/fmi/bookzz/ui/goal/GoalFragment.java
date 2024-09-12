package com.fmi.bookzz.ui.goal;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.MyLibraryAdapter;
import com.fmi.bookzz.adapter.UserBooksAdapter;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.entity.User;
import com.fmi.bookzz.entity.YearGoal;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;
import com.fmi.bookzz.ui.user.ProfileFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalFragment extends Fragment {
TextView addGoalTV;
EditText addGoalET;
Button addGoalB;
TextView goalTV;
TextView goalBooksTV;
//View horizontalLine;
//ImageView fantasyBookImage;
RecyclerView goalBooksRV;
    MainActivity activity;
    ExecutorService executorService;
    Handler mainHandler;
    MyLibraryAdapter adapter;
    Integer year;

    public GoalFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_goal, container, false);
       addGoalTV = view.findViewById(R.id.addGoalTV);
       addGoalET = view.findViewById(R.id.addGoalET);
       addGoalB = view.findViewById(R.id.addGoalB);
       goalTV = view.findViewById(R.id.goalTV);
       goalBooksTV = view.findViewById(R.id.goalBooksTV);
       goalBooksRV = view.findViewById(R.id.goalBooksRV);
       getGoal();
        ArrayList<MyBook> books = new ArrayList<>();
        adapter = new MyLibraryAdapter(activity, getContext(), books);
        goalBooksRV.setLayoutManager(new GridLayoutManager(getContext(), 2));
        goalBooksRV.setAdapter(adapter);
        getBooks();
       return view;
    }

    public void getGoal(){
        year = LocalDate.now().getYear();
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_YEAR_GOAL,year, RequestHelper.token));

        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    try {
                            JSONObject goalJson = new JSONObject(response);
                            YearGoal goal = new YearGoal();

                            goal.setId(goalJson.getLong("id"));
                            goal.setCount(goalJson.getInt("count"));
                            goal.setYear(goalJson.getInt("year"));
                        bindGoal(goal, year);
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

    private void bindGoal(YearGoal goal, Integer year) {
        if(goal.getId() == -1){
            goalTV.setVisibility(View.INVISIBLE);
            goalBooksTV.setVisibility(View.INVISIBLE);
            goalBooksRV.setVisibility(View.INVISIBLE);
            addGoalTV.setText("Add your goal (" + year + ")" );
            addGoalB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String count = addGoalET.getText().toString();
                    addGoal(count);
                }
            });
        }
        else {
            addGoalET.setVisibility(View.INVISIBLE);
            addGoalTV.setVisibility(View.INVISIBLE);
            addGoalB.setVisibility(View.INVISIBLE);
            goalTV.setText("Your goal this year (" + goal.getYear() + ") is " + goal.getCount() + " books");
            goalBooksTV.setText(year + " in books");
        }
    }

    private void addGoal(String count) {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.ADD_GOAL,count,RequestHelper.token));
        RequestHelper.requestService(urlString, "POST", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                try {
                    JSONObject goalJson = new JSONObject(response);
                    YearGoal goal = new YearGoal();

                    goal.setId(goalJson.getLong("id"));
                    goal.setCount(goalJson.getInt("count"));
                    goal.setYear(goalJson.getInt("year"));
                    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_main, new GoalFragment());
                    transaction.commit();
                    bindGoal(goal, goal.getYear());
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
    public void getBooks() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_READ_BOOKS,year, RequestHelper.token));

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