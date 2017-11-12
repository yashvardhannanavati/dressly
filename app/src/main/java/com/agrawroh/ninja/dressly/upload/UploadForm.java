package com.agrawroh.ninja.dressly.upload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawroh.ninja.dressly.R;
import com.agrawroh.ninja.dressly.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UploadForm extends AppCompatActivity {

    private static List<String> keys;
    String[] SIZE = {"Small", "Medium", "Large", "XL", "XXL"};
    String[] SEX = {"1", "2", "3", "4", "5", "6"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        /* Initialize Keys */
        keys = new ArrayList<>();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, SIZE);
        MaterialBetterSpinner materialDesignSpinner =
                findViewById(R.id.android_material_design_size);
        materialDesignSpinner.setAdapter(arrayAdapter);

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, SEX);
        MaterialBetterSpinner materialDesignSpinner2 =
                findViewById(R.id.android_material_design_gender);
        materialDesignSpinner2.setAdapter(arrayAdapter2);

        findViewById(R.id.imgImages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 965);
            }
        });
        findViewById(R.id.imgImages1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 966);
            }
        });
        findViewById(R.id.imgImages2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 967);
            }
        });
        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item it = new Item();
                it.setItemId(new Date().getTime());
                it.setItemName(((TextView) findViewById(R.id.input_name)).getText().toString());
                it.setItemDescription(((TextView) findViewById(R.id.input_description)).getText().toString());
                it.setItemDetails(((TextView) findViewById(R.id.input_details)).getText().toString());
                it.setItemImageURI(new ArrayList<>(keys));
                it.setItemRating("$200");
                it.setItemLocation(((TextView) findViewById(R.id.input_location)).getText().toString());
                it.setItemSize(((com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner) findViewById(R.id.android_material_design_size)).getText().toString());
                it.setItemPrice(((TextView) findViewById(R.id.input_price)).getText().toString());
                FirebaseDatabase.getInstance()
                        .getReference().child("items").child(((com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner) findViewById(R.id.android_material_design_gender)).getText().toString())
                        .push()
                        .setValue(it);
                Toast.makeText(getApplicationContext(), "Item Upload Successful!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 965 || requestCode == 966 || requestCode == 967) && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                final String key = "images/" + String.valueOf(new Date().getTime()) + ".jpg";
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                if (bitmap.getWidth() >= 2048 || bitmap.getWidth() >= 2048) {
                    bitmap = scaleDown(bitmap);
                }
                uploadFile(key, bitmap);
                if (requestCode == 965) {
                    ((ImageView) findViewById(R.id.imgImages)).setImageBitmap(bitmap);
                } else if (requestCode == 966) {
                    ((ImageView) findViewById(R.id.imgImages1)).setImageBitmap(bitmap);
                } else if (requestCode == 967) {
                    ((ImageView) findViewById(R.id.imgImages2)).setImageBitmap(bitmap);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Upload File
     *
     * @param key
     * @param bitmap
     */
    private void uploadFile(final String key, final Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child(key).putBytes(data, new StorageMetadata.Builder().setContentType("image/jpeg").build());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                keys.add(downloadUrl.toString());
            }
        });
    }

    private Bitmap scaleDown(Bitmap realImage) {
        if (realImage.getWidth() >= 2048) {
            return Bitmap.createScaledBitmap(realImage, 2048,
                    (int) (2048d / realImage.getWidth() * realImage.getHeight()), true);
        } else {
            return Bitmap.createScaledBitmap(realImage, (int) (2048d / realImage.getHeight()) * realImage.getWidth(),
                    2048, true);
        }
    }
}
