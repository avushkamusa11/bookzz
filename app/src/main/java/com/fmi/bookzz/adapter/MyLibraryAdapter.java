package com.fmi.bookzz.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.Book;
import com.fmi.bookzz.entity.MyBook;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;
import com.fmi.bookzz.ui.book.BookItemFragment;
import com.fmi.bookzz.ui.myLibrary.ChangeStatusFragment;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MyLibraryAdapter extends RecyclerView.Adapter<MyLibraryAdapter.MyLibraryViewHolder> {
    Context context;
    private List<MyBook> bookList;
    private MainActivity activity;

    public MyLibraryAdapter(Activity activity, Context context, List<MyBook> bookList){
        this.bookList = bookList;
        this.context = context;
        this.activity = (MainActivity) activity;
    }

    @NonNull
    @Override
    public MyLibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_one_item,parent,false);
        return  new MyLibraryViewHolder(view,context,this);
    }

    @Override
    public void onBindViewHolder(@NonNull MyLibraryViewHolder holder, int position) {
        MyBook book = bookList.get(position);
        holder.bind(book);
        holder.bookIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.content_main, new BookItemFragment(bookList.get(holder.getAdapterPosition()).getId()));
                transaction.commit();
            }
        });
        holder.changeStatusB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.content_main, new ChangeStatusFragment(bookList.get(holder.getAdapterPosition())));
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void addAndUpdate(ArrayList<MyBook> allBooks, boolean updateList) {
        if(updateList){
            bookList.addAll(allBooks);
        }
        notifyDataSetChanged();
    }

    public static class MyLibraryViewHolder extends  RecyclerView.ViewHolder{

        Context context;
        private ImageView bookIV;
        private TextView bookTitleTV;
        private TextView bookAuthorsTV;
        private TextView statusTV;
        private Button changeStatusB;
        private MyBook book;
        private List<MyBook> books;
        MyLibraryAdapter adapter;
        public MyLibraryViewHolder(@NonNull View itemView, Context context, MyLibraryAdapter adapter) {
            super(itemView);
            this.context = context;
            this.adapter = adapter;


            bookIV = itemView.findViewById(R.id.booksBookIV);
            bookTitleTV = itemView.findViewById(R.id.booksBookTitleTV);
            bookAuthorsTV = itemView.findViewById(R.id.booksBookAuthorTV);
            statusTV = itemView.findViewById(R.id.booksStatusTV);
            changeStatusB = itemView.findViewById(R.id.booksChangeStatus);
        }

        public void bind(MyBook book){
            String authors = "";
            for(String name  : book.getAuthors()){
                authors = authors + " " + name;
            }
            bookAuthorsTV.setText(authors);
            statusTV.setText(book.getStatus());
            bookTitleTV.setText(book.getTitle());

            if(book.getBookImage() != null ){
                Bitmap bitmap = Helper.decodeBase64ToBitmap(book.getBookImage());
                bookIV.setImageBitmap(bitmap);
            }
        }
    }

}
