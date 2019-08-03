package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AdminActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private static final int PICTURE_RESULT = 42;
    EditText edTitle;
    EditText edPrice;
    EditText edDescription;
    ImageView imgDisplay;
    HolidayDeal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        edTitle = findViewById(R.id.edTitle);
        edPrice = findViewById(R.id.edPrice);
        edDescription = findViewById(R.id.edDescription);
        imgDisplay = findViewById(R.id.imgDisplay);

        Intent intent = getIntent();
        HolidayDeal deal = (HolidayDeal) intent.getSerializableExtra("Deal");
        if(deal == null) {
            deal = new HolidayDeal();
        }
        this.deal = deal;
        edTitle.setText(deal.getTitle());
        edPrice.setText(deal.getPrice());
        edDescription.setText(deal.getDescription());
        showImage(deal.getImageUrl());
        Button selectImgBtn = findViewById(R.id.selectImgBtn);
        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this,"Saved Successfully",
                        Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this,"Deal Deleted",
                        Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enabledEditTexts(true);
            findViewById(R.id.selectImgBtn).setEnabled(true);
        }else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enabledEditTexts(false);
            findViewById(R.id.selectImgBtn).setEnabled(false);

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.storageReference.child(imageUri.getLastPathSegment());
            UploadTask uploadTask = ref.putFile(imageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        String url = task.getResult().toString();
                        String pictureName = task.getResult().getPath();
                        deal.setImageUrl(url);
                        deal.setImageName(pictureName);
                        showImage(url);
                    }else{
                        //Log.d(TAG, "onComplete: Failed to fetch url");
                    }
                }
            });
        }
    }

    private void saveDeal() {
        deal.setTitle(edTitle.getText().toString());
        deal.setPrice(edPrice.getText().toString());
        deal.setDescription(edDescription.getText().toString());

        if(deal.getId() == null) {
            databaseReference.push().setValue(deal);
        }else{
            databaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void clean() {
        edTitle.setText("");
        edPrice.setText("");
        edDescription.setText("");
        edTitle.requestFocus();
    }

    private void backToList() {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }

    private void deleteDeal() {
        if(deal == null) {
            Toast.makeText(this, "Please save before deleting",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        databaseReference.child(deal.getId()).removeValue();

        if(deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtil.storageReference.child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Log.d("Delete Image", "Image Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Log.d("Delete Image", e.getMessage());
                }
            });
        }
    }

    private void enabledEditTexts(boolean isEnabled) {
        edTitle.setEnabled(isEnabled);
        edPrice.setEnabled(isEnabled);
        edDescription.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if(url != null && url.isEmpty() == false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(imgDisplay);
        }
    }


}
