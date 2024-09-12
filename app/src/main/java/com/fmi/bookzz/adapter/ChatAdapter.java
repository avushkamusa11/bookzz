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
import com.fmi.bookzz.entity.Chat;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.ui.MainActivity;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
   Context context;
   private List<Chat> chatsList;
   private MainActivity activity;

   public ChatAdapter(Activity activity,Context context,List<Chat> chatsList){
       this.chatsList= chatsList;
       this.context= context;
       this.activity = (MainActivity) activity;
   }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_row,parent,false);

       return new ChatViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
       Chat chat = chatsList.get(position);
       holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        Context context;
        private ImageView userIV;
        private TextView usernameTV;
        private TextView messageTV;

        public ChatViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            this.context = context;

            userIV = itemView.findViewById(R.id.userIV);
            usernameTV = itemView.findViewById(R.id.chatsUsernameTV);
            messageTV = itemView.findViewById(R.id.chatsMessageTV);
        }

        public void bind(Chat chat) {
            usernameTV.setText(chat.getUsername());
            if(chat.getUserProfilePicture() != null ){
                Bitmap bitmap = Helper.decodeBase64ToBitmap(chat.getUserProfilePicture());
                userIV.setImageBitmap(bitmap);
            }
            messageTV.setText(chat.getLastMessage());
        }
    }
}
