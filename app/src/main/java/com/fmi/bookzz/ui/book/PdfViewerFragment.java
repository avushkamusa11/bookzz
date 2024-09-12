package com.fmi.bookzz.ui.book;
// PdfViewerFragment.java

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fmi.bookzz.R;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.ui.MainActivity;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PdfViewerFragment extends Fragment {

    private PDFView pdfView;
    MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        pdfView = view.findViewById(R.id.pdfView);
        activity = (MainActivity) getActivity();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            long bookId = args.getLong("bookId");
            //String pdfUrl = "http://192.168.174.35:8081/book/pdf/" + bookId;
            String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                    String.format(RequestHelper.GET_PDF, bookId));

            new DownloadPdfTask().execute(urlString);
        }
    }

    private class DownloadPdfTask extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... strings) {
            String pdfUrl = strings[0];
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(pdfUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String base64Pdf = response.body().string();
                    return Base64.decode(base64Pdf, Base64.DEFAULT);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(byte[] pdfBytes) {
            if (pdfBytes != null) {
                pdfView.fromBytes(pdfBytes)
                        .load();
            } else {
                Toast.makeText(getContext(), "Failed to download PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
