package com.example.chat.activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.example.chat.databinding.ActivitySinglinBinding;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class SinglinActivity extends AppCompatActivity {

    private ActivitySinglinBinding binding;
    private PreferenceManager preferenceManager;
    private Boolean In = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySinglinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners()
    {
        binding.buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidSignInDetails())
                {
                    loading(true);
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot ds : snapshot.getChildren())
                            {
                                try
                                {
                                    if (GetDecryptInfo((((HashMap<String, String>)ds.getValue()).get("email")).split(";")).equals(binding.inputEmail.getText().toString()) &&
                                            GetDecryptInfo((((HashMap<String, String>)ds.getValue()).get("password")).split(";")).equals(binding.inputPasswrod.getText().toString()))
                                    {
                                        preferenceManager.putString(Constants.KEY_NAME, ((HashMap<String, String>)ds.getValue()).get("name"));
                                        preferenceManager.putString(Constants.KEY_EMAIL, ((HashMap<String, String>)ds.getValue()).get("email"));
                                        preferenceManager.putString(Constants.KEY_IMAGE, ((HashMap<String, String>)ds.getValue()).get("image"));
                                        preferenceManager.putString(Constants.KEY_USER_ID, ds.getKey().toString());
                                        In = true;
                                        break;
                                    }
                                }
                                catch (Exception ex)
                                {
                                    showToast(ex.getMessage());
                                }

                            }
                            signin();
                            loading(false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });
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

    private void signin()
    {
        if (In)
        {
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            In = false;
        }
        else
        {
            showToast("Invalid email or password");
        }
    }

    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails()
    {
        if (binding.inputEmail.getText().toString().trim().isEmpty())
        {
            showToast("Enter email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast("Enter valid email");
        }
        else if (binding.inputPasswrod.getText().toString().trim().isEmpty())
        {
            showToast("Enter password");
            return false;
        }
        return true;

    }

    private byte[] decryptMessage(byte[] encryptedMessage, byte[] keyBytes) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedMessage);
    }
}