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
import com.fmi.bookzz.entity.User;
import com.fmi.bookzz.helper.Helper;
import com.fmi.bookzz.helper.RequestHelper;
import com.fmi.bookzz.helper.ResponseListener;
import com.fmi.bookzz.ui.MainActivity;
import com.fmi.bookzz.ui.books.BooksFragment;
import com.fmi.bookzz.ui.chat.UserChatFragment;
import com.fmi.bookzz.ui.user.FriendsFragment;
import com.fmi.bookzz.ui.user.ProfileFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    private List<User> users;
    private MainActivity activity;
    private String buttonText;

    public UsersAdapter(Activity activity,Context context,List<User> users, String buttonText){
        this.context = context;
        this.activity = (MainActivity) activity;
        this.buttonText = buttonText;
        this.users = users;
    }
    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_row,parent,false);

        return new UsersViewHolder(view,context,this, activity, buttonText);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addAndUpdate(ArrayList<User> allUsers, boolean updateList) {
        if(updateList){
            users.addAll(allUsers);
        }
        notifyDataSetChanged();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        Context context;
        private ImageView userIV;
        private TextView fullNameTV;
        private TextView usernameTV;
        private Button addToFriendB;
        private User user;
        private MainActivity activity;
        private List<User> users;
        private UsersAdapter adapter;
        public UsersViewHolder(@NonNull View itemView,Context context, UsersAdapter adapter, MainActivity activity, String buttonText) {
            super(itemView);
            this.context = context;
            this.adapter = adapter;
            this.activity = activity;

            userIV = itemView.findViewById(R.id.userPPIV);
            fullNameTV = itemView.findViewById(R.id.fullNameTV);
            usernameTV = itemView.findViewById(R.id.userUsernameTV);
            addToFriendB = itemView.findViewById(R.id.addToFriendB);
            addToFriendB.setText(buttonText);
            if(buttonText.equals("ADD TO FRIEND")){
                addToFriendB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = String.format("%s:%s/%s", RequestHelper.ADDRESS, RequestHelper.PORT,
                                String.format(RequestHelper.ADD_TO_FRIEND,user.getId(),RequestHelper.token));
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
                        adapter.removeUser(user);
                    }
                });
            }else {
                addToFriendB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

                        transaction.replace(R.id.content_main, new UserChatFragment(user));
                        transaction.commit();
                    }
                });
            }

userIV.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.content_main, new ProfileFragment(user));
        transaction.commit();
    }
});
        }

        public void bind(User user){
            this.user = user;
            fullNameTV.setText(user.getfName() + " " + user.getlName());
            usernameTV.setText(user.getUsername());
            if(user.getProfilePicture() != null ){
                Bitmap bitmap = Helper.decodeBase64ToBitmap(user.getProfilePicture());
                userIV.setImageBitmap(bitmap);
            }
        }
    }

    private void removeUser(User user) {
        int position = users.indexOf(user);
        if(position >= 0){
            users.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,users.size());
        }
    }
}
