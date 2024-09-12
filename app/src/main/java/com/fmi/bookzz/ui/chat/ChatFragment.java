package com.fmi.bookzz.ui.chat;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fmi.bookzz.R;
import com.fmi.bookzz.adapter.ChatAdapter;
import com.fmi.bookzz.entity.Chat;
import com.fmi.bookzz.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    RecyclerView chatRV;
    ChatAdapter adapter;
    MainActivity activity;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);;
        chatRV = view.findViewById(R.id.chatRV);
        activity = (MainActivity) getActivity();
        List<Chat> chats = new ArrayList<>();
        adapter = new ChatAdapter(activity,getContext(),chats);
        chatRV.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRV.setAdapter(adapter);
        getChats();
        return view;
    }

    private void getChats() {
    }

}