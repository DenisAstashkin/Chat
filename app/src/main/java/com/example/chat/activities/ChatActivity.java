package com.example.chat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.chat.adapters.ChatAdapter;
import com.example.chat.databinding.ActivityChatBinding;
import com.example.chat.models.ChatMessage;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private DatabaseReference db;
    private String conversionId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedSrting(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );

        binding.chatRecyclerView.setAdapter(chatAdapter);
        db = FirebaseDatabase.getInstance().getReference(Constants.KEY_COLLECTION_CHAT);
    }

    private void sendMessage() throws Exception {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, encryptMessage(binding.inputMessage.getText().toString().getBytes(), "asdfghjkl;'zxcvb".getBytes()));
        message.put(Constants.KEY_TIMESTAMP, new Date().toString());
        db.push().setValue(message);
        binding.inputMessage.setText(null);

    }

    private void listenMessages()

    {
        db.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = chatMessages.size();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Boolean add = true;
                    if(((HashMap<String, String>)ds.getValue()).get(Constants.KEY_SENDER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID)) &&
                            ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_RECEIVER_ID).equals(receiverUser.id))
                    {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_RECEIVER_ID);
                        try {
                            String[] message = (((HashMap<String, String>)ds.getValue()).get(Constants.KEY_MESSAGE)).split(";");
                            byte[] message_byte = new byte[message.length];
                            for(int i = 0; i < message.length; i++)
                            {
                                message_byte[i] = (byte)Integer.parseInt(message[i]);
                            }
                            chatMessage.message =  new String(decryptMessage(message_byte, "asdfghjkl;'zxcvb".getBytes()), "UTF-8");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        chatMessage.dataTime = ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_TIMESTAMP);
                        chatMessage.dateObject = new Date(((HashMap<String, String>)ds.getValue()).get(Constants.KEY_TIMESTAMP));

                        for(ChatMessage cm : chatMessages)
                        {
                            if (cm.dataTime.equals(chatMessage.dataTime))
                            {
                                add = false;
                                break;
                            }
                        }
                        if (add)
                        {
                            chatMessages.add(chatMessage);
                        }
                    }

                }
                chatMessages.sort((obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

                if (count == 0)
                {
                    chatAdapter.notifyDataSetChanged();
                }
                else
                {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                }
                binding.chatRecyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        db.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = chatMessages.size();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Boolean add = true;
                    if(((HashMap<String, String>)ds.getValue()).get(Constants.KEY_SENDER_ID).equals(receiverUser.id) &&
                            ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_RECEIVER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID)))
                    {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_RECEIVER_ID);
                        try {
                            String[] message = (((HashMap<String, String>)ds.getValue()).get(Constants.KEY_MESSAGE)).split(";");
                            byte[] message_byte = new byte[message.length];
                            for(int i = 0; i < message.length; i++)
                            {
                                message_byte[i] = (byte)Integer.parseInt(message[i]);
                            }
                            chatMessage.message =  new String(decryptMessage(message_byte, "asdfghjkl;'zxcvb".getBytes()), "UTF-8");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        chatMessage.dataTime = ((HashMap<String, String>)ds.getValue()).get(Constants.KEY_TIMESTAMP);
                        chatMessage.dateObject = new Date(((HashMap<String, String>)ds.getValue()).get(Constants.KEY_TIMESTAMP));

                        for(ChatMessage cm : chatMessages)
                        {
                            if (cm.dataTime.equals(chatMessage.dataTime))
                            {
                                add = false;
                                break;
                            }
                        }
                        if (add)
                        {
                            chatMessages.add(chatMessage);
                        }
                    }

                }
                chatMessages.sort((obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

                if (count == 0)
                {
                    chatAdapter.notifyDataSetChanged();
                }
                else
                {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                }
                binding.chatRecyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private Bitmap getBitmapFromEncodedSrting(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadReceiverDetails()
    {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        assert receiverUser != null;
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> {startActivity(new Intent(getApplicationContext(), UsersActivity.class));});
        binding.layoutSend.setOnClickListener(v -> {
            try {
                sendMessage();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String encryptMessage(byte[] message, byte[] keyBytes) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipher_message = cipher.doFinal(message);
        String res = new String();
        for(int i = 0; i < cipher_message.length; i++)
        {
            res += Byte.toString(cipher_message[i]);
            if (i + 1 == cipher_message.length)
                continue;
            res += ";";
        }
        return res;
    }

    private byte[] decryptMessage(byte[] encryptedMessage, byte[] keyBytes) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedMessage);
    }

}