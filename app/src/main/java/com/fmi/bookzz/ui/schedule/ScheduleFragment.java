package com.fmi.bookzz.ui.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.PlanAdapter;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.entity.Plan;
import com.fmi.bookzz.helper.PlanDecorator;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ScheduleFragment extends Fragment {

    TextView selectedDateTV;
    TextView selectedTimeTV;
    Button selectDateButton;
    Button selectTimeButton;
    Button addScheduleB;
    Spinner booksSpinner;
    MaterialCalendarView planCV;
    MainActivity activity;
    ExecutorService executorService;
    Handler mainHandler;
    String selectedBook; // Store the selected book
    RecyclerView planRV;
    Set<CalendarDay> planDates;
    List<Plan> plans;
    TextView planSelectedDateTV;
    PlanAdapter adapter;
    public ScheduleFragment() {
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
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        selectedDateTV = view.findViewById(R.id.selectedDateTV);
        selectDateButton = view.findViewById(R.id.selectDateButton);
        selectedTimeTV = view.findViewById(R.id.selectedTimeTV);
        selectTimeButton = view.findViewById(R.id.selectTimeButton);
        addScheduleB = view.findViewById(R.id.addScheduleB);
        booksSpinner = view.findViewById(R.id.booksSpinner);
        planCV = view.findViewById(R.id.planCV);
        planRV = view.findViewById(R.id.planRV);
        planSelectedDateTV = view.findViewById(R.id.planSelectedDateTV);
        adapter = new PlanAdapter(getActivity(), getContext());
        planRV.setLayoutManager(new LinearLayoutManager(getContext()));
        planRV.setAdapter(adapter);
        plans = new ArrayList<>();
        // Load books into the spinner
        planCV.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int year = date.getYear();
                int month = date.getMonth() + 1; // Months are 0-based in CalendarDay
                int day = date.getDay();

                planSelectedDateTV.setText("Selected Date: " + day + "." + month + "." + year);

                // Convert the selected date to your desired format and update the RecyclerView
                String formattedDate = "";
                if(day < 10){
                    formattedDate = "0" + day + "-";
                }else{
                    formattedDate = day + "-";
                }if(month < 10){
                    formattedDate += "0" + month + "-";
                }else{
                    formattedDate += month + "-";
                }
                formattedDate += year;
                updateRecyclerView(formattedDate);
            }
        });
        addSchedule();
        getBooks();

        // Handle button clicks
        handleDateAndTimePickers();
        handleAddScheduleButton();
        getPlans();
        return view;
    }

    private void getPlans() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_PLANS, RequestHelper.token));

        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    try {
                        JSONArray plansArray = new JSONArray(response);
                        planDates = new HashSet<>();
                        for (int i = 0; i < plansArray.length(); i++) {
                            JSONObject planJSON = plansArray.getJSONObject(i);
                            Plan newPlan = new Plan();

                            newPlan.setId(planJSON.getLong("id"));
                            newPlan.setBookTitle(planJSON.getString("bookTitle"));
                            newPlan.setDate(planJSON.getString("date"));
                            newPlan.setStartTime(planJSON.getString("startTime"));
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

                            try {
                                // Parse the string into a Date object
                                Date date = dateFormat.parse(newPlan.getDate());

                                // Create a Calendar instance and set the Date object to it
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);

                                // Extract the year, month, and day from the Calendar
                                int year = calendar.get(Calendar.YEAR);
                                int month = calendar.get(Calendar.MONTH);  // Months are 0-based
                                int day = calendar.get(Calendar.DAY_OF_MONTH);

                                // Create a CalendarDay instance
                                CalendarDay calendarDay = CalendarDay.from(year, month, day);

                                // Now you have a CalendarDay object
                                planDates.add(calendarDay);
                                plans.add(newPlan);
                            } catch (ParseException e) {
                                e.printStackTrace(); // Handle the exception if the date string is not in the expected format
                            }
                        }

                        // Update the spinner on the main thread
                        mainHandler.post(() -> {
                            planCV.addDecorator(new PlanDecorator(Color.RED, planDates));

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
                            booksSpinner.setAdapter(adapter);
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

    private void handleDateAndTimePickers() {
        // Set up Date Picker Dialog
        selectDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1;
                        selectedDateTV.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Set up Time Picker Dialog
        selectTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute1) -> {
                        String selectedTime;
                        if(minute1 == 0){
                            selectedTime = hourOfDay + ":00";
                        }else{
                            selectedTime = hourOfDay + ":" + minute1;
                        }

                        selectedTimeTV.setText(selectedTime);
                    },
                    hour, minute, true
            );
            timePickerDialog.show();
        });
    }

    private void handleAddScheduleButton() {
        // Handle Add Schedule button click
        addScheduleB.setOnClickListener(v -> {
            // Get the selected book from the spinner
            selectedBook = booksSpinner.getSelectedItem().toString();

            Log.d("Selected Book", selectedBook);

            // Now you can use the selectedBook string for further processing
            addPlan(selectedBook, selectedDateTV.getText().toString(),selectedTimeTV.getText().toString());
        });

        // Optional: Handle selection changes in the Spinner
        booksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Update the selectedBook variable when a new item is selected
                selectedBook = parentView.getItemAtPosition(position).toString();
                Log.d("Selected Book Changed", selectedBook);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case when no book is selected, if needed
            }
        });
    }

    private void addPlan(String selectedBook, String selectedDate, String selectedTime) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Create a JSON object with the data
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", selectedBook);
            jsonObject.put("date", selectedDate);
            jsonObject.put("startTime", selectedTime);

            // Convert the JSON object to a string
            String jsonString = jsonObject.toString();

            // Create the RequestBody with the JSON string
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);

            // Build the URL string
            String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                    String.format(RequestHelper.ADD_PLAN, RequestHelper.token));

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
                    Log.e("Add Schedule Error", e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject planJson = new JSONObject(response.body().string());
                            Plan plan = new Plan();
                            plan.setId(planJson.getLong("id"));
                            if(plan.getId() == -1l) {
                                Toast.makeText(activity, "this time is busy.please set another one", Toast.LENGTH_SHORT).show();
                            }else{
                                plan.setDate(planJson.getString("date"));
                                plan.setStartTime(planJson.getString("startTime"));
                                plan.setBookTitle(planJson.getString("bookTitle"));
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        selectedDateTV.setText("Selected Date");
                                        selectedTimeTV.setText("Selected Time");
                                    }
                                });

                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("Add Schedule Response", "Request failed with code: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Selected Book in Plan", selectedBook);
    }


    private void addSchedule() {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.ADD_SCHEDULE, RequestHelper.token));
        RequestHelper.requestService(urlString, "POST", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                // Handle the response from the server
                Log.d("Add Schedule Response", response);
            }

            @Override
            public void onError(String error) {
                Log.e("Add Schedule Error", error);
            }
        });
    }
    private void updateRecyclerView(String date) {
        List<Plan> currentPlans = new ArrayList<>();

        for (Plan p: plans) {
            String planDate = p.getDate();
            if (planDate.equals(date)) {
                currentPlans.add(p);
            }
        }

        adapter.changePlans(currentPlans);
    }
}
