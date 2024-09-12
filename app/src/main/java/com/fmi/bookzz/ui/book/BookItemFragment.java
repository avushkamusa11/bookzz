package com.fmi.bookzz.ui.book;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.CommentsAdapter;
import com.fmi.bookzz.databinding.ActivityMainBinding;
import com.fmi.bookzz.entity.Book;
import com.fmi.bookzz.entity.Comment;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.helper.IOnBackPressed;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;
import com.fmi.bookzz.ui.books.BooksFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BookItemFragment extends Fragment implements IOnBackPressed {
    ImageView bookItemIV;
    Button readPdfB;
    TextView titleTV;
    TextView authorTV;
    TextView genreTV;
    TextView rateTV;
    TextView pagesTV;
    TextView descriptionTV;
    TextView commentsTV;
    RecyclerView commentsRV;
    EditText addCommentET;
    ImageButton addCommentIB;
    CommentsAdapter adapter;
    MainActivity activity;

    long bookId;
    private NavController navController; // Declare the NavController

//

    public BookItemFragment(long bookId){
        this.bookId= bookId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_item, container, false);

        bookItemIV = view.findViewById(R.id.bookItemIV);
        titleTV = view.findViewById(R.id.bookItemTitleTV);
        readPdfB = view.findViewById(R.id.readPdfB);
        authorTV = view.findViewById(R.id.bookItemAuthorTV);
        pagesTV = view.findViewById(R.id.bookItemPagesTV);
        genreTV = view.findViewById(R.id.bookItemGenreTV);
        descriptionTV = view.findViewById(R.id.bookItemDescriptionTV);
        rateTV = view.findViewById(R.id.bookItemRateTV);
        commentsTV = view.findViewById(R.id.bookItemCommentsTV);
        commentsRV = view.findViewById(R.id.commentsRV);
        addCommentET = view.findViewById(R.id.addCommentET);
        addCommentIB = view.findViewById(R.id.addCommentB);

        activity = (MainActivity) getActivity();
        ArrayList<Comment> comments = new ArrayList<>();
        adapter = new CommentsAdapter(activity, getContext(), comments);
        commentsRV.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRV.setAdapter(adapter);
        getComments(bookId);
        getBook(bookId);
        addCommentIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment(v);
            }
        });
        readPdfB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdfViewer();
            }
        });
        return view;
    }

    private void openPdfViewer() {
        Bundle bundle = new Bundle();
        bundle.putLong("bookId", bookId); // Pass any other data if needed

        PdfViewerFragment pdfViewerFragment = new PdfViewerFragment();
        pdfViewerFragment.setArguments(bundle);

        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, pdfViewerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void getComments(long bookId) {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_COMMENTS, bookId));

        RequestHelper.requestService(urlString, "GET", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                try {
                    JSONArray commentsArray = new JSONArray(response);
                    ArrayList<Comment> comments = new ArrayList<>();

                    for (int i = 0; i < commentsArray.length(); i++) {
                        JSONObject commentJson = commentsArray.getJSONObject(i);
                        Comment comment = new Comment();

                        comment.setId(commentJson.getLong("id"));
                        comment.setComment(commentJson.getString("comment"));
                        comment.setPublishDate(commentJson.getString("publishDate"));
                        JSONObject user = commentJson.getJSONObject("user");
                        comment.setUsername(user.getString("username"));
                        comments.add(comment);
                    }
                    adapter.addAndUpdate(comments, true);

                } catch (JSONException e) {
                    Log.wtf("JSON parsing error", e);
                }
            }

            @Override
            public void onError(final String error) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Failed to get comments", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getBook(long bookId) {
        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.GET_BOOK, bookId));

        RequestHelper.requestService(urlString, "GET", new ResponseListener() {
            @Override
            public void onResponse(String response) throws JSONException {
                JSONObject jsonObject = new JSONObject(response);
                Book newBook = new Book();

                newBook.setId(jsonObject.getLong("id"));
                newBook.setTitle(jsonObject.getString("title"));
                newBook.setIsbn(jsonObject.getString("isbn"));
                newBook.setPages(jsonObject.getInt("pages")); // Assuming pages should be set too
                newBook.setDescription(jsonObject.getString("description")); // Assuming description should be set too
                newBook.setRate(jsonObject.getDouble("rate"));
                newBook.setFile(jsonObject.getString("file"));
                newBook.setGenre((jsonObject.getString("genre")));
                JSONArray authorsArray = jsonObject.getJSONArray("authors");
                List<String> authorsList = new ArrayList<>();
                for (int j = 0; j < authorsArray.length(); j++) {
                    authorsList.add(authorsArray.getString(j));
                }
                newBook.setAuthors(authorsList);
                setView(newBook);
            }

            @Override
            public void onError(final String error) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Failed to get book", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setView(Book book) {
        StringBuilder authors = new StringBuilder();
        for (String name : book.getAuthors()) {
            authors.append(" ").append(name);
        }
        titleTV.setText(book.getTitle());

        authorTV.setText(authors.toString());
        rateTV.setText(String.valueOf(book.getRate()));
        if (book.getFile() != null) {
            Bitmap bitmap = Helper.decodeBase64ToBitmap(book.getFile());
            bookItemIV.setImageBitmap(bitmap);
        }
        pagesTV.setText(String.valueOf(book.getPages()));
        genreTV.setText(book.getGenre());
        descriptionTV.setText(book.getDescription());
    }

    public void addComment(View view) {
        String commentText = addCommentET.getText().toString();
        if (!commentText.isEmpty()) {
            new CommentAsyncTask(commentText, bookId, activity).execute();
        } else {
            Toast.makeText(activity, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onBackPressed() {
        BooksFragment nextFrag= new BooksFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, nextFrag, "findThisFragment")
                .addToBackStack(null)
                .commit();

        return true;
    }

    private class CommentAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean isSuccessful;
        String token;
        String errorMessage;
        String commentText;
        Long bookId;

        ProgressDialog dialog;

        CommentAsyncTask(String commentText, Long bookId, MainActivity activity) {
            this.commentText = commentText;
            this.bookId = bookId;
            this.token = RequestHelper.token;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Adding comment...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String urlString = String.format("%s:%s/comments/%s/%s",
                    RequestHelper.ADDRESS, RequestHelper.PORT, bookId, token);

            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = commentText.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                String result = reader.readLine();

                if (result != null) {
                    JSONObject resultOb = new JSONObject(result);
                    if (resultOb.getLong("id") > 0) {
                        isSuccessful = true;
                    }
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            dialog.dismiss();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isSuccessful) {
                        Toast.makeText(activity, "Comment added successfully", Toast.LENGTH_SHORT).show();
                        addCommentET.setText("");
                        getComments(bookId);
                    } else {
                        Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
