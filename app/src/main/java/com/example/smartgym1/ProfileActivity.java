package com.example.smartgym1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

import static com.example.smartgym1.FBref.refAuth;
import static com.example.smartgym1.FBref.refImages;
import static com.example.smartgym1.FBref.refUsers;

public class ProfileActivity extends AppCompatActivity {

    ImageView iV;
    int Gallery=1;

    TextView tv_user;

    User user = new User();

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser firebaseUser = refAuth.getCurrentUser();

        iV=(ImageView)findViewById(R.id.iV);

        tv_user = (TextView)findViewById(R.id.tv_user);

        email = firebaseUser.getEmail().replace("."," ");

        refUsers.child(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user.copyUser(dataSnapshot.getValue(User.class));
                        tv_user.setText(user.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        try {
            download();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Selecting image file to upload to Firebase Storage
     * <p>
     *
     * @param view
     */
    public void upload(View view) {
        Intent si = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(si, Gallery);
    }

    /**
     * Uploading selected image file to Firebase Storage
     * <p>
     *
     * @param requestCode   The call sign of the intent that requested the result
     * @param resultCode    A code that symbols the status of the result of the activity
     * @param data          The data returned
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Gallery) {
                Uri file = data.getData();
                if (file != null) {
                    final ProgressDialog pd=ProgressDialog.show(this,"Upload image","Uploading...",true);
                    StorageReference refImg = refImages.child(email+".jpg");
                    refImg.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    pd.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                                    try {
                                        download();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    pd.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "No Image was selected", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void download() throws IOException{

        StorageReference refImg = refImages.child(email+".jpg");

        final File localFile = File.createTempFile(email,"jpg");
        refImg.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                String filePath = localFile.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                iV.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ProfileActivity.this, "Image download failed", Toast.LENGTH_LONG).show();
            }
        });
    }
}
