package com.fmi.bookzz.ui.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView profilePictureIV;
    TextView profileNameTV;
    TextView usernameTV;
    Button changePictureB;
    RecyclerView booksRV;
    User user;
    MyLibraryAdapter adapter;
    MainActivity activity;
    ExecutorService executorService;
    Handler mainHandler;
    Bitmap selectedBitmap;

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
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        activity = (MainActivity) getActivity();
        profilePictureIV = view.findViewById(R.id.profileImageIV);
        profileNameTV = view.findViewById(R.id.ProfileNameTV);
        usernameTV = view.findViewById(R.id.username);
        changePictureB = view.findViewById(R.id.changeProfilePictureB);
        booksRV = view.findViewById(R.id.books_recycler_view);
        user = RequestHelper.currentUser;

        Bitmap profilePicture = Helper.decodeBase64ToBitmap(user.getProfilePicture());
        profilePictureIV.setImageBitmap(profilePicture);
        profileNameTV.setText(user.getfName() + " " + user.getlName());
        usernameTV.setText(user.getUsername());

        ArrayList<MyBook> books = new ArrayList<>();
        adapter = new MyLibraryAdapter(activity, getContext(), books);
        booksRV.setLayoutManager(new GridLayoutManager(getContext(), 2));
        booksRV.setAdapter(adapter);
        getBooks();

        changePictureB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profilePictureIV.setImageBitmap(selectedBitmap);
                uploadImageToServer(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToServer(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();


        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        sendImageToServer(encodedImage);
    }

    private void sendImageToServer(String encodedImage) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "profile.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), Base64.decode(encodedImage, Base64.DEFAULT)))
                .build();

        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                String.format(RequestHelper.UPLOAD_PICTURE, RequestHelper.token));

        Request request = new Request.Builder()
                .url(urlString)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
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
