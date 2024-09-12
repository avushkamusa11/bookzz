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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.ui.MainActivity;
import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.Book;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.book.BookItemFragment;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    Context context;
    public List<Book> bookList;
    private List<Book> fullBookList;
    private MainActivity activity;



    public BookAdapter(Activity activity, Context context, List<Book> bookList) {
        this.bookList = bookList;
        fullBookList = new ArrayList<>(bookList);
        this.context = context;
        this.activity = (MainActivity) activity;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_list_row,parent,false);

        return new BookViewHolder(view,context,this);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book);
        holder.bookIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.content_main, new BookItemFragment(bookList.get(holder.getAdapterPosition()).getId()));
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void addAndUpdate(ArrayList<Book> allBooks, boolean updateList) {
        if(updateList){
            bookList.addAll(allBooks);
            fullBookList.addAll(allBooks);
        }
        notifyDataSetChanged();
    }
    public void removeBook(Book book) {
        int position = bookList.indexOf(book);
        if (position >= 0) {
            bookList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, bookList.size());
        }
    }


    public static class BookViewHolder extends  RecyclerView.ViewHolder{

        Context context;
        private LinearLayout rootLayout;
        private ImageView bookIV;
        private TextView bookTitleTV;
        private TextView bookAuthorsTV;
        private TextView rateTV;
        private Button addToReadB;
        private RatingBar ratingBar;
        private Book book;
        private List<Book> books;
        BookAdapter adapter;
        public BookViewHolder(@NonNull View itemView, Context context, BookAdapter adapter) {
            super(itemView);
            this.context = context;
            this.adapter = adapter;


        rootLayout = itemView.findViewById(R.id.bookListRootLayout);
            bookIV = itemView.findViewById(R.id.bookImage);
            bookTitleTV = itemView.findViewById(R.id.booksTitleTV);
            bookAuthorsTV = itemView.findViewById(R.id.booksAuthorTV);
            rateTV = itemView.findViewById(R.id.booksStatusTV);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ratingBar.setNumStars(5);
            addToReadB = itemView.findViewById(R.id.addToReadB);
            addToReadB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                            String.format(RequestHelper.ADD_TO_READ,book.getId(),"To Read",RequestHelper.token));
                    RequestHelper.requestService(urlString, "POST", new ResponseListener() {
                        @Override
                        public void onResponse(String response) throws JSONException {
                            Long id = Long.valueOf(response);
                            Log.wtf("response", response);
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
                    adapter.removeBook(book);
                }
            });

//            bookIV.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
//
//                    transaction.replace(R.id.fragment_book_item, new EventFragment(books.get(holder.getAdapterPosition()).getId()));
//                    transaction.commit();
//                }
//            });
        }

        public void bind(Book book){
            this.book = book;
            String authors = "";
            for(String name  : book.getAuthors()){
                authors = authors + " " + name;
            }bookTitleTV.setText(book.getTitle());

            bookAuthorsTV.setText(authors);
            rateTV.setText(book.getRate() + "");
            ratingBar.setRating((float) book.getRate());
            if(book.getFile() != null ){
                Bitmap bitmap = Helper.decodeBase64ToBitmap(book.getFile());
                bookIV.setImageBitmap(bitmap);
            }
        }
    }
}
