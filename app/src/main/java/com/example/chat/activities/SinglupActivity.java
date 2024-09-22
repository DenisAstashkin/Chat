package com.example.chat.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.chat.databinding.ActivitySinglupBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SinglupActivity extends AppCompatActivity {

    private ActivitySinglupBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySinglupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners()
    {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignDetails())
            {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String, String> us = new HashMap<>();
        us.put("name", binding.inputName.getText().toString());
        us.put("password", binding.inputPasswrod.getText().toString());
        us.put("email", binding.inputEmail.getText().toString());
        us.put("image", encodedImage);
        db.push().setValue(us)
                        .addOnSuccessListener( s-> {
                            loading(false);
                            preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                            Intent intent = new Intent(getApplicationContext(), SinglinActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            showToast("Complete");
                            startActivity(intent);
                        })
                        .addOnFailureListener( f -> {
                            loading(false);
                            showToast(f.getMessage());
                        });
        us.clear();
    }



    private String encodeImage(Bitmap map)
    {
        int previewWidth = 150;
        int previewHeight = map.getHeight() * previewWidth / map.getWidth();
        Bitmap previesBitmap = Bitmap.createScaledBitmap(map, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previesBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK)
                {
                    if (result.getData() != null)
                    {
                        Uri imageUri = result.getData().getData();
                        try
                        {
                            InputStream IS = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(IS);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }
                        catch (FileNotFoundException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignDetails()
    {
        if (encodedImage == null)
        {
            showToast("Select profile image");
            return false;
        }
        else if (binding.inputName.getText().toString().trim().isEmpty())
        {
            showToast("Enter name");
            return false;
        }
        else if (binding.inputEmail.toString().trim().isEmpty())
        {
            showToast("Enter email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast("Enter valid email");
            return false;
        }
        else if (binding.inputPasswrod.getText().toString().trim().isEmpty())
        {
            showToast("Enter password");
            return false;
        }
        else if (binding.inputConfirmPasswrod.getText().toString().trim().isEmpty())
        {
            showToast("Confirm your password");
            return false;
        }
        else if (!binding.inputPasswrod.getText().toString().equals(binding.inputConfirmPasswrod.getText().toString()))
        {
            showToast("Password and confirm password must be same");
            return false;
        }
        else
        {
            return true;
        }
    }


    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {

            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}