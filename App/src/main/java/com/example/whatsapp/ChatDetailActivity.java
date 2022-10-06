package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.whatsapp.Adapter.ChatAdapter;
import com.example.whatsapp.Models.MessageModel;
import com.example.whatsapp.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.util.ArrayList;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        final String senderId = auth.getUid();
        String receiveId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");
        binding.userName.setText(userName);
      //  setContentView(R.layout.activity_chat_detail);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);
        binding.backArrow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void  onClick(View v){
                Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter=new ChatAdapter(messageModels,this,receiveId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom=senderId + receiveId;
        final String receiverRoom=receiveId + senderId;

        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                            MessageModel model=snapshot1.getValue(MessageModel.class);
                            model.setMessageId(snapshot1.getKey());
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=binding.enterMessage.getText().toString();
                final MessageModel model=new MessageModel(senderId,message);
                //model.setTimestamp(Date.getTime());
                binding.enterMessage.setText("");

                database.getReference().child("chats")
                        .child(senderRoom)
                        .push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .push()
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });

                            }
                        });
            }
        });


    }


}