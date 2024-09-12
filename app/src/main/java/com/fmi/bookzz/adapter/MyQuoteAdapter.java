package com.fmi.bookzz.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.Quote;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MyQuoteAdapter extends RecyclerView.Adapter<MyQuoteAdapter.MyQuoteViewHolder> {
    Context context;
    private List<Quote> quotes;
    MainActivity activity;
    Handler mainHandler;

    public MyQuoteAdapter(Activity activity, Context context, List<Quote> quotes){
        this.context = context;
        this.activity = (MainActivity) activity;
        this.quotes =quotes;
        mainHandler = new Handler(Looper.getMainLooper());
    }
    @NonNull
    @Override
    public MyQuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_quote_list_row,parent,false);
        return new MyQuoteViewHolder(view,context,this);
    }

    @Override
    public void onBindViewHolder(@NonNull MyQuoteViewHolder holder, int position) {
        Quote quote = quotes.get(position);
        holder.bind(quote);
    }

    @Override
    public int getItemCount() {
        return quotes.size();
    }

    public static class MyQuoteViewHolder extends RecyclerView.ViewHolder{
        Context context;
        private TextView quoteTV;
        private TextView bookTitleTV;
        private CheckBox isPrivateCB;
        private MyQuoteAdapter adapter;
        private Quote quote;

        public MyQuoteViewHolder(@NonNull View itemView, Context context,MyQuoteAdapter adapter) {
            super(itemView);
            this.context = context;
            this.adapter=adapter;

            quoteTV = itemView.findViewById(R.id.quoteTV);
            bookTitleTV = itemView.findViewById(R.id.quoteBookTitleTV);
            isPrivateCB = itemView.findViewById(R.id.isPrivateQuoteCB);
            isPrivateCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateQuote(isChecked,quote.getId());
                }
            });
        }

        private void updateQuote(boolean isChecked,long id) {
            String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                    String.format(RequestHelper.UPDATE_QUOTE,id,isChecked));
            RequestHelper.requestService(urlString, "PUT", new ResponseListener() {
                @Override
                public void onResponse(String response) throws JSONException {
                    //  JSONArray usersArray = new JSONArray(response);
                    Log.wtf("response", response);
                }

                @Override
                public void onError(String error) {

                }
            });
            adapter.mainHandler.post(() -> {
                // Assuming `getAdapterPosition()` provides the correct position
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    adapter.notifyItemChanged(position);  // Only update the affected item
                }
            });

        }

        public void bind(Quote quote){
            this.quote = quote;
            quoteTV.setText(quote.getQuoteText());
            bookTitleTV.setText(quote.getBookTitle());
            isPrivateCB.setChecked(quote.isPrivate());
        }
    }

}
