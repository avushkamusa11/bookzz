package com.fmi.bookzz.ui.myLibrary;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;
import com.fmi.bookzz.ui.book.PdfViewerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChangeStatusFragment extends Fragment {

    Long myBookId;
    MyBook myBook;
    ImageView bookIV;
    TextView titleTV;
    TextView authorsTV;
    Button readPdfB;
    Spinner changeStatusS;
    RatingBar ratingBar;
    Button changeStatusB;
    TextView statusTV;
    Button ratingB;

    MainActivity activity;
    String token;
    String selectedItem;
    int rate;

    public ChangeStatusFragment(MyBook myBook) {
        this.myBook = myBook;
        this.token = RequestHelper.token;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_status, container, false);
        bookIV = view.findViewById(R.id.csBookIV);
        titleTV = view.findViewById(R.id.csBookTitleTV);
        authorsTV = view.findViewById(R.id.csAuthorsTV);
        readPdfB  =view.findViewById(R.id.csReadPdfB);
        changeStatusS = view.findViewById(R.id.csChangeStatusS);
        ratingBar = view.findViewById(R.id.ratingBar);
        changeStatusB = view.findViewById(R.id.csChangeStatusB);
        statusTV = view.findViewById(R.id.csStatusTV);
        ratingB = view.findViewById(R.id.csRateB);

        setView(myBook);
        activity = (MainActivity) getActivity();
        readPdfB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdfViewer();
            }
        });
        changeStatusS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        changeStatusB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(selectedItem);
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
               rate = (int) rating;
            }
        });
        ratingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateBook(rate);
            }
        });
        return view;
    }

    private void rateBook(int rate) {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.RATE_BOOK, myBook.getBookId(),rate));

        RequestHelper.requestService(urlString, "PUT", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                JSONObject bookJson = new JSONObject(response);

                if(!bookJson.getString("title").isEmpty()){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Successfully rated", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {

            }
        });

    }

    private void changeStatus(String selectedItem) {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.UPDATE_STATUS, myBook.getBookId(),selectedItem,token));

        RequestHelper.requestService(urlString, "PUT", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                JSONObject bookJson = new JSONObject(response);
                MyBook newBook = new MyBook();

                newBook.setId(bookJson.getLong("id"));
                newBook.setTitle(bookJson.getString("title"));
                newBook.setStatus(bookJson.getString("status"));
                newBook.setBookImage(bookJson.getString("bookImage"));

                JSONArray authorsArray = bookJson.getJSONArray("authors");
                List<String> authorsList = new ArrayList<>();

                for (int j = 0; j < authorsArray.length(); j++) {
                    authorsList.add(authorsArray.getString(j));
                }

                newBook.setAuthors(authorsList);
                setView(newBook);
            }

            @Override
            public void onError(String error) {

            }
        });

    }

    private void setView(MyBook myBook) {
        StringBuilder authors = new StringBuilder();
        for (String name : myBook.getAuthors()) {
            authors.append(" ").append(name);
        }
        titleTV.setText(myBook.getTitle());
        statusTV.setText(myBook.getStatus());
        authorsTV.setText(authors.toString());
       if (myBook.getBookImage() != null) {
           Bitmap bitmap = Helper.decodeBase64ToBitmap(myBook.getBookImage());
           bookIV.setImageBitmap(bitmap);
       }
    }

    private void openPdfViewer() {
        Bundle bundle = new Bundle();
        bundle.putLong("bookId", myBook.getBookId());

        PdfViewerFragment pdfViewerFragment = new PdfViewerFragment();
        pdfViewerFragment.setArguments(bundle);

        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, pdfViewerFragment)
                    .addToBackStack(null)
                    .commit();
        }
//        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
//        navController.navigate(R.id.nav_pdf_viewer, bundle);
    }
}