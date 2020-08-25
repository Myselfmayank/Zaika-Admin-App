package com.example.zaikaadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    TextInputLayout RestName,RestDesc,Rating;
    TextView progress;
    ProgressBar progressBar;
    ImageView restImage;
    int REQUEST_CODE=101;

    Uri imageUri;
    boolean isImageSelected;

    StorageReference storageReference;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RestName=findViewById(R.id.restName);
        RestDesc=findViewById(R.id.restDesc);
        Rating=findViewById(R.id.rating);
        progress=findViewById(R.id.textViewProgress);
        progressBar=findViewById(R.id.progressBar);
        restImage=findViewById(R.id.profile_image);

        progress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);



        databaseReference= FirebaseDatabase.getInstance().getReference("restaurantDetails");
        storageReference= FirebaseStorage.getInstance().getReference("restaurantPic");

        restImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE && data!=null){
            imageUri=data.getData();
            isImageSelected=true;
            restImage.setImageURI(imageUri);
        }
    }

    public void submitBtn(View view)  {
        if(isImageSelected){
            uploadImage();
        }
    }

    private void uploadImage() {
        progress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);



        final String key = RestName.getEditText().getText().toString();
        //uploading image to firebaseStorage
        storageReference.child(key+".jpg").putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.child(key+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        HashMap hashMap = new HashMap();
                        hashMap.put("restaurantName",RestName.getEditText().getText().toString());
                        hashMap.put("description",RestDesc.getEditText().getText().toString());
                        hashMap.put("rating",Rating.getEditText().getText().toString());
                        hashMap.put("imageUrl",uri.toString());
                        //Store downloadable uri in database
                        databaseReference.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double Progress=(taskSnapshot.getBytesTransferred()*100/taskSnapshot.getTotalByteCount());
                progressBar.setProgress((int) Progress);
                progress.setText(Progress+"%");
            }
        });
    }
}


