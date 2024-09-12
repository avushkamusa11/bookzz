package com.fmi.bookzz.ui.statistic;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.helper.PlanDecorator;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticFragment extends Fragment {

    TextView selectedFromDateTV;
    Button selectFormDateButton;
    TextView selectedToDateTV;
    Button selectToDateButton;
    TextView statisticTV;
    Button getStatisticB;
    ExecutorService executorService;
    Handler mainHandler;


    public StatisticFragment() {
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
         View view = inflater.inflate(R.layout.fragment_statistic, container, false);
         selectedFromDateTV = view.findViewById(R.id.selectedFromDateTV);
         selectedToDateTV = view.findViewById(R.id.selectedToDateTV);
         selectFormDateButton = view.findViewById(R.id.selectFromDateButton);
         selectToDateButton = view.findViewById(R.id.selectDateToButton);
         statisticTV = view.findViewById(R.id.statisticTV);
         getStatisticB = view.findViewById(R.id.getStatisticB);
         selectedFromDateTV.setText("01-01-2024");
         selectedToDateTV.setText("01-09-2024");
         getStatisticB.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getStatistic();
             }
         });
        handleDatePickers();
        getStatistic();
         return view;
    }

    private void getStatistic() {
//        Date from = formatDate(selectedFromDateTV.getText().toString());
//        Date to = formatDate(selectedToDateTV.getText().toString());
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_STATISTIC,selectedFromDateTV.getText().toString(),selectedToDateTV.getText().toString(), RequestHelper.token));
        executorService.execute(() -> {
            RequestHelper.requestService(urlString, "GET", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                   // Integer.valueOf(response);
                    mainHandler.post(() -> {
                        statisticTV.setText("You read " + response + " pages from " + selectedFromDateTV.getText().toString() + " to " + selectedToDateTV.getText().toString());

                    });}

                @Override
                public void onError(String error) {
                    Log.e("Request Error", error);
                }
            });
        });
    }

    private void handleDatePickers() {
        // Set up Date Picker Dialog
        selectFormDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = "";
                        if(dayOfMonth < 10){
                            selectedDate += "0";
                        }
                              selectedDate +=  dayOfMonth + "-";
                        if((monthOfYear + 1) < 10){
                            selectedDate += "0";
                        }
                        selectedDate += (monthOfYear + 1) + "-" + year1;
                        selectedFromDateTV.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        selectToDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = "";
                        if(dayOfMonth < 10){
                            selectedDate += "0";
                        }
                        selectedDate +=  dayOfMonth + "-";
                        if((monthOfYear + 1) < 10){
                            selectedDate += "0";
                        }
                        selectedDate += (monthOfYear + 1) + "-" + year1;
                        selectedToDateTV.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }
    private Date formatDate(String stringDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date date = dateFormat.parse(stringDate);
            return  date;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}