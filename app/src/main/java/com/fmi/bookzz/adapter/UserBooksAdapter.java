package com.fmi.bookzz.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.Book;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;

import org.json.JSONException;

import java.util.List;

public class UserBooksAdapter extends RecyclerView.Adapter<UserBooksAdapter.UserBooksViewHolder> {
    Context context;
    private List<Book> bookList;
    private MainActivity activity;

    @NonNull
    @Override
    public UserBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull UserBooksViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class UserBooksViewHolder extends  RecyclerView.ViewHolder{

        Context context;
        private ImageView bookIV;
        private TextView bookTitleTV;

        private Book book;
        private List<Book> books;
        BookAdapter adapter;
        public UserBooksViewHolder(@NonNull View itemView, Context context, BookAdapter adapter) {
            super(itemView);
            this.context = context;
            this.adapter = adapter;


           bookIV = itemView.findViewById(R.id.bookImage);
            bookTitleTV = itemView.findViewById(R.id.booksTitleTV);

        }


        public void bind(Book book){
            this.book = book;
            bookTitleTV.setText(book.getTitle());

            if(book.getFile() != null ){
                Bitmap bitmap = Helper.decodeBase64ToBitmap(book.getFile());
                bookIV.setImageBitmap(bitmap);
            }
        }
    }
}
