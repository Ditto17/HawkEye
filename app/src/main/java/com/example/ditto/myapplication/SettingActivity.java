package com.example.ditto.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    //Android layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusbtn;
    private Button mImagebtn;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PICK = 1;

    //storage Firebase
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //Finding widget with their uniqe ID's
        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mStatusbtn = (Button) findViewById(R.id.settings_status_btn);
        mImagebtn = (Button) findViewById(R.id.settings_image_btn);

        //Getting the Instance of current User
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Getting the storage ref from the firebaseStorage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Getting the UID of the current user
        String current_uid = mCurrentUser.getUid();

        //poplating the realtimedatabase with USER --> UID
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //setting up an valueEventlistener
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                //if(!image.equals("default")){
                    Picasso.get().load(image).into(mDisplayImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //setting up an OnClicklistener on change status button
        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Getting the status from the realtime database and storing it in a string.
                String status_value = mStatus.getText().toString();

                //Making an Intent to Move the user to SettingsActivity --> StatusActivity.
                Intent status_intent = new Intent(SettingActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status_value);

                startActivity(status_intent);

            }
        });

        //Setting up an OnClickListener on an ImageButton.
        mImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Using an Intent to get all the images from the gallery.
                Intent galleryIntent = new Intent();

                //Setting up the path of the image source.
                galleryIntent.setType("image/*");

                //Getting the content(images) from the above path.
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                //Returning the Activity with a result(selected Image).
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

                // start picker to get image for cropping and then use the image in cropping activity
               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingActivity.this);*/

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            // start cropping activity for pre-acquired image saved on the device
            //Instantiating the cropImage feature and setting the ratio in 1:1.
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(SettingActivity.this);
        }

        //Checking if the image is cropped or not.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //loading prompt
                mProgressDialog = new ProgressDialog(SettingActivity.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait!");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final String current_user_id = mCurrentUser.getUid();

                final StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");

                if(resultUri != null) {

                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {


                                //@SuppressWarnings("VisibleForTests") String download_url = task.getResult().getStorage().getDownloadUrl().toString();
                                mImageStorage.child("profile_images").child(current_user_id + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String download_url = uri.toString();

                                        //check again .child("image")
                                        mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {

                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingActivity.this, "uploaded.", Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        });

                                    }
                                });

                            } else {

                                Toast.makeText(SettingActivity.this, "Error in uploading.", Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();

                            }

                        }
                    });
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
