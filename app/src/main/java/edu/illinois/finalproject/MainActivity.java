package edu.illinois.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageButton cameraButton, uploadImageButton;
    private ImageView imagePreview;
    private TextView uploadTextView, welcomeTextView;
    private ProgressDialog progressDialog;
    private Button recentSearchesButton, signOutButton;
    final private int REQUEST_IMAGE_CAPTURE = 1;
    final private int RESULT_LOAD_IMAGE = 2;

    private ArrayList<String> imagePathArray = new ArrayList<>();
    private String filename;

    private FirebaseDatabase database;
    private FirebaseAuth databaseAuth;
    private FirebaseAuth.AuthStateListener databaseAuthListener;
    private DatabaseReference userRef;
    private UserProfile signedInUser;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        uploadImageButton = (ImageButton) findViewById(R.id.galleryImageButton);
        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        uploadTextView = (TextView) findViewById(R.id.uploadImageTextView);
        welcomeTextView = (TextView) findViewById(R.id.welcomeMessage);
        recentSearchesButton = (Button) findViewById(R.id.recentSearches);
        signOutButton = (Button) findViewById(R.id.signOutButton);

        databaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference();
        FirebaseUser user = databaseAuth.getCurrentUser();
        userID = user.getUid();

        recentSearchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent());
            }
        });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });


        databaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = databaseAuth.getCurrentUser();
                if (user != null) {
                    makeToastText("Signed in as " + user.getEmail());
                } else {
                    // User is signed out
                    startActivity(new Intent(MainActivity.this, LogInActivity.class));
                }
            }
        };

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    signedInUser = new UserProfile();
                    signedInUser.setName(ds.child(userID).getValue(UserProfile.class).getName());
                    signedInUser.setUserName(ds.child(userID).getValue(UserProfile.class).getUserName());
                    signedInUser.setEmail(ds.child(userID).getValue(UserProfile.class).getEmail());

                    welcomeTextView.setText("Welcome " + signedInUser.getUserName() + "!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseAuth.signOut();
            }
        });
    }

    /**
     * Source: https://www.youtube.com/watch?v=OPnusBmMQTw and
     * Intent to open the album and choose only images
     */
    private void getImageFromAlbum() {
        try {
            // make sure that the intent only allows choosing images.
            Intent pickPhotoIntent =
                    new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            makeToastText("Gallery load failed");
        }
    }

    /**
     * Source: https://www.youtube.com/watch?v=itAOotYB5Ns and
     * https://developer.android.com/training/camera/photobasics.html
     * Intent to be sent to open the camera
     */
    private void dispatchTakePictureIntent() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (Exception e) {
            makeToastText("Camera load failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageSourceHandler(requestCode, data);
        }
        imageProcessing(resultCode);
    }

    /**
     * Handles the request made by the user, opens the camera or upload from the gallery
     *
     * @param requestCode request code to identify the command
     * @param data        intent result
     */
    private void imageSourceHandler(int requestCode, Intent data) {
        switch (requestCode) {
            case (REQUEST_IMAGE_CAPTURE):
                // gets the resulting data
                Bundle extras = data.getExtras();
                Bitmap image = (Bitmap) extras.get("data");

                // offsets the image orientation
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap imageNew = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
                        image.getHeight(), matrix, true);

                //add to the array, take the name and sets the image into the imagePreview imageview
                filename = saveImage(imageNew);
                imagePathArray.add(filename);
                imagePreview.setImageBitmap(imageNew);
                imageProcessing(requestCode);
                break;
            case (RESULT_LOAD_IMAGE):
                // if the request is to open the gallery and choose picture to upload
                Uri imageUri = data.getData();
                filename = imageUri.getPath();
                imagePathArray.add(filename);
                imagePreview.setImageURI(imageUri);
                imageProcessing(requestCode);
                break;
        }
    }

    /**
     * Uploads the image and do stuff according to the way the image is uploaded
     *
     * @param resultCode the code that differs the image treatment
     */
    private void imageProcessing(final int resultCode) {
        uploadTextView.setText("Click here to search using this image!");
        uploadTextView.setTextSize(15);
        uploadTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Uploading Image...");
                progressDialog.show();

                //get the signed in user
                FirebaseUser user = databaseAuth.getCurrentUser();
                String userId = user.getUid();

                Uri uri = Uri.fromFile(new File(filename));
            }
        });
    }

    /**
     * Save image into a local file
     *
     * @param finalBitmap bitmap whose file is being created
     * @return the path to the saved file
     */
    private String saveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return root + "/saved_images/" + fname;
    }

    @Override
    public void onStart() {
        super.onStart();
        databaseAuth.addAuthStateListener(databaseAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (databaseAuthListener != null) {
            databaseAuth.removeAuthStateListener(databaseAuthListener);
        }
    }

    /**
     * Make a toast for the given message
     *
     * @param message value of string to make the toast with
     */
    public void makeToastText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}


