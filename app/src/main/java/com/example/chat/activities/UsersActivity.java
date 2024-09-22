package com.example.chat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chat.adapters.UsersAdapter;
import com.example.chat.databinding.ActivityUsersBinding;
import com.example.chat.listeners.UserListener;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UsersActivity extends AppCompatActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> {startActivity(new Intent(getApplicationContext(), SinglinActivity.class));});
    }

    private void getUsers()
    {
        loading(true);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<User> users = new ArrayList<>();
                String user_id = preferenceManager.getString(Constants.KEY_USER_ID);
                try
                {

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (user_id.equals(ds.getKey().toString())) {
                            continue;
                        }
                        User user = new User();
                        user.name = ((HashMap<String, String>) ds.getValue()).get("name");
                        user.email = GetDecryptInfo(((HashMap<String, String>) ds.getValue()).get("email").split(";"));
                        user.image = ((HashMap<String, String>) ds.getValue()).get("image");
                        user.id = ds.getKey();
                        users.add(user);
                    }
                    if (users.size() > 0) {
                        UsersAdapter usersAdapter = new UsersAdapter(users, UsersActivity.this);
                        binding.usersRecyclerView.setAdapter(usersAdapter);
                        binding.usersRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        showErrorMassage();
                    }
                }
                catch (Exception ex)
                {
                    showErrorMassage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        loading(false);
    }

    private String GetDecryptInfo(String[] Bytes)
    {
        String Info = "";
        try
        {
            byte[] info_byte = new byte[Bytes.length];
            for(int i = 0; i < Bytes.length; i++)
            {
                info_byte[i] = (byte)Integer.parseInt(Bytes[i]);
            }
            Info =  new String(decryptMessage(info_byte, "asdfghjkl;'zxcvb".getBytes()), "UTF-8");
        }
        catch (Exception e)
        {

        }
        return Info;
    }

    private byte[] decryptMessage(byte[] encryptedMessage, byte[] keyBytes) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedMessage);
    }

    private void showErrorMassage()
    {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        preferenceManager.putString(Constants.KEY_RECEIVER_ID, user.id);
        startActivity(intent);
        finish();
    }

}