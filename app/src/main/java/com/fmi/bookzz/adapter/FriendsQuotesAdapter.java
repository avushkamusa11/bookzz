package com.fmi.bookzz.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.Quote;
import com.fmi.bookzz.ui.MainActivity;

import java.util.List;

public class FriendsQuotesAdapter extends RecyclerView.Adapter<FriendsQuotesAdapter.FriendsQuotesViewHolder> {
    Context context;
    List<Quote> quotes;
    MainActivity activity;
    Handler mainHandler;

    public FriendsQuotesAdapter(Activity activity,Context context, List<Quote> quotes){
        this.context = context;
        this.activity = (MainActivity) activity;
        this.quotes =quotes;
        mainHandler = new Handler(Looper.getMainLooper());

    }
    @NonNull
    @Override
    public FriendsQuotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_quotes_list_row,parent,false);

        return new FriendsQuotesViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsQuotesViewHolder holder, int position) {
        Quote quote = quotes.get(position);
        holder.bind(quote);
    }

    @Override
    public int getItemCount() {
        return quotes.size();
    }

    public static class FriendsQuotesViewHolder extends  RecyclerView.ViewHolder {

        Context context;
        private TextView usernameTV;
        private ImageView profilePictureIV;
        private TextView quoteTV;
        private TextView bookTitleTV;
        public FriendsQuotesViewHolder(@NonNull View itemView, Context context)
        {
            super(itemView);
            this.context = context;

            usernameTV = itemView.findViewById(R.id.quotesFrendUsernameTV);
            profilePictureIV = itemView.findViewById(R.id.profilePictureIV);
            quoteTV = itemView.findViewById(R.id.friendQuoteTV);
            bookTitleTV = itemView.findViewById(R.id.friendQuoteTitleTV);
        }

        public void bind(Quote quote){
            quoteTV.setText(quote.getQuoteText());
            bookTitleTV.setText(quote.getBookTitle());
            usernameTV.setText(quote.getUsername());

            //to do profile picture
        }
    }
}
