package com.fmi.bookzz.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.Book;
import com.fmi.bookzz.entity.Comment;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    Context context;
    private List<Comment> comments;
    private MainActivity activity;

    public CommentsAdapter(Activity activity, Context context, List<Comment> comments){
        this.activity = (MainActivity) activity;
        this.context=context;
        this.comments = comments;
    }
    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_row,parent,false);

        return new CommentsViewHolder(view,context,this);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
    public void addAndUpdate(ArrayList<Comment> allComents, boolean updateList) {
        if(updateList){
            comments.addAll(allComents);
        }
        notifyDataSetChanged();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{

        Context context;
        private ImageView userPictureIV;
        private TextView usernameTV;
        private TextView commentTV;
        private TextView dateTV;
        private CommentsAdapter adapter;

        public CommentsViewHolder(@NonNull View itemView,Context context,CommentsAdapter adapter) {
            super(itemView);
            this.context = context;
            this.adapter = adapter;

            userPictureIV = itemView.findViewById(R.id.commentsUserIV);
            usernameTV = itemView.findViewById(R.id.commentsUserTV);
            commentTV = itemView.findViewById(R.id.commentTV);
            dateTV = itemView.findViewById(R.id.commentDateTV);
        }

        public void bind(Comment comment){
            if (comment.getUserProfilePicture() != null) {
                Bitmap bitmap = Helper.decodeBase64ToBitmap(comment.getUserProfilePicture());
                userPictureIV.setImageBitmap(bitmap);
            }
            usernameTV.setText(comment.getUsername());
            commentTV.setText(comment.getComment());
            dateTV.setText(comment.getPublishDate().toString());
        }
    }
}
