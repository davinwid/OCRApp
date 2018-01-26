package edu.illinois.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    final private static int REQUEST_IMAGE_CAPTURE = 1;
    final private static int RESULT_LOAD_IMAGE = 2;
    final private static int REQUEST_CAMERA_PERMISSION_GRANTED = 3;
    final private static int REQUEST_STORAGE_PERMISSION_GRANTED = 4;

    private ImageButton cameraButton, uploadImageButton;
    private ImageView imagePreview;
    private TextView uploadTextView, welcomeTextView, ocrResultText, noResultText;
    private Button signOutButton, scanImage, copyToClipBoard, resultPage;
    private Toolbar toolbar;

    private FirebaseDatabase database;
    private FirebaseAuth databaseAuth;
    private FirebaseAuth.AuthStateListener databaseAuthListener;
    private DatabaseReference userRef;
    private UserProfile signedInUser;
    private String userID;
    private Bitmap image;
    private static String imageFilePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        uploadImageButton = (ImageButton) findViewById(R.id.galleryImageButton);
        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        uploadTextView = (TextView) findViewById(R.id.uploadImageTextView);
        welcomeTextView = (TextView) findViewById(R.id.welcomeMessage);
        ocrResultText = (TextView) findViewById(R.id.ocrResultText);
        signOutButton = (Button) findViewById(R.id.signOutButton);
        copyToClipBoard = (Button) findViewById(R.id.copyToClipboard);
        scanImage = (Button) findViewById(R.id.scanImage);
        noResultText = (TextView) findViewById(R.id.noResultText);
        resultPage = (Button) findViewById(R.id.resultPage);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        databaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference();
        FirebaseUser user = databaseAuth.getCurrentUser();

        userID = user.getUid();
        setSupportActionBar(toolbar);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask for permission once again to make sure everything works
                askPermission();
                dispatchTakePictureIntent();
            }
        });


        databaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = databaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    startActivity(new Intent(MainActivity.this, LogInActivity.class));
                }
            }
        };

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnaphotCurrent : dataSnapshot.getChildren()) {
                    getUserDetails(dataSnaphotCurrent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //source https://stackoverflow.com/questions/42851810/how-to-add-copy-to-clipboard-button-in-android
        copyToClipBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilityMethods.copyToClipboard(ocrResultText, MainActivity.this);
            }
        });

        resultPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailedPage = new Intent(MainActivity.this, DetailedViewPage.class);
                detailedPage.putExtra("result" , ocrResultText.getText().toString());
                detailedPage.putExtra("memory location", imageFilePath);
                startActivity(detailedPage);
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
     * Gets the user credentials from firebase and puts it in the activity
     *
     * @param ds data snapshot of the user credential source
     */
    private void getUserDetails(DataSnapshot ds) {
        signedInUser = new UserProfile();
        signedInUser.setName(ds.child(userID).getValue(UserProfile.class).getName());
        signedInUser.setUserName(ds.child(userID).getValue(UserProfile.class).getUserName());
        signedInUser.setEmail(ds.child(userID).getValue(UserProfile.class).getEmail());

        makeToastText("Signed in as " + signedInUser.getEmail());
        welcomeTextView.setText("Welcome " + signedInUser.getUserName() + "!");
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
     * Intent to be sent to open the camera
     * source: https://www.youtube.com/watch?v=itAOotYB5Ns and
     * https://developer.android.com/training/camera/photobasics.html
     */
    private void dispatchTakePictureIntent() {
        try {
            // initialize the intent and photo file to be created
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;

            // try to create the image file
            try {
                photoFile = UtilityMethods.createImageFile();
                imageFilePath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // set up authorities that has been set in the manifest and make a uri
            String authorities = getApplicationContext().getPackageName() + ".provider";
            Uri cameraIntentUri = FileProvider.getUriForFile(MainActivity.this, authorities, photoFile);

            // attach the Uri object to the camera
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraIntentUri);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (Exception e) {
            makeToastText("Camera load failed");
        }
    }

    /**
     * Asks the user for the permission to run the apps full functionality
     * source: https://www.youtube.com/watch?v=Nt5GMaFUvog
     */
    private void askPermission() {
        // asks the program whether the write external storage permission is granted,
        // if not ask the user
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION_GRANTED);
        }
        // asks the program whether the camera permission is granted,
        // if not ask the user
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MainActivity.REQUEST_CAMERA_PERMISSION_GRANTED);
        }
    }


    /**
     * Method that handles the intent result
     *
     * @param requestCode code that checks its state (RESULT_OK or not)
     * @param resultCode  code that differs with different intent request
     * @param data        the intent that was just ran
     */
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_IMAGE_CAPTURE):
                    // if the request is capturing the image, create the required bitmap
                    image = UtilityMethods.getCorrectOrientedImage(imageFilePath);
                    break;
                case (RESULT_LOAD_IMAGE):
                    // if the request is to open the gallery and choose picture to upload
                    Uri imageUri = data.getData();
                    imageFilePath = UtilityMethods.getRealPathFromURI(this, imageUri);

                    // create a bitmap of the image
                    image = UtilityMethods.getCorrectOrientedImage(imageFilePath);
                    break;
            }
            // sets the image preview and sets the layout
            layoutProcess(image);
        }
    }


    /**
     * Uploads the image and sets the layout according to the way the image is uploaded
     *
     * @param image image the OCR is going to scan with
     */
    private void layoutProcess(final Bitmap image) {
        imagePreview.setImageBitmap(image);
        uploadTextView.setVisibility(View.GONE);
        scanImage.setVisibility(View.VISIBLE);
        copyToClipBoard.setVisibility(View.GONE);
        ocrResultText.setVisibility(View.GONE);
        noResultText.setVisibility(View.GONE);
        resultPage.setVisibility(View.GONE);

        scanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ocrResult = UtilityMethods.texRecognition(image, MainActivity.this);
                ocrResultUserInterface(ocrResult);
            }
        });
    }

    /**
     * Changes the user interface according to the OCR's result
     *
     * @param ocrResult String resulted from the image recognition
     */
    private void ocrResultUserInterface(String ocrResult) {
        scanImage.setVisibility(View.GONE);
        // empty means no text is gotten from the image
        if (ocrResult.length() == 0) {
            noResultText.setVisibility(View.VISIBLE);
            // if result is less than 30 characters, show the text
        } else if (ocrResult.length() <= 15) {
            // initialize the state of the result page
            resultPage.setVisibility(View.VISIBLE);
            ocrResultText.setVisibility(View.VISIBLE);
            ocrResultText.setText(ocrResult);
            copyToClipBoard.setVisibility(View.VISIBLE);
        } else {
            // if not then don't and ask the user to copy to clipboard to get the result
            resultPage.setVisibility(View.VISIBLE);
            ocrResultText.setText(ocrResult);
            copyToClipBoard.setVisibility(View.VISIBLE);
            uploadTextView.setVisibility(View.VISIBLE);
            uploadTextView.setTextSize(18);
            uploadTextView.setText(R.string.text_too_long);
        }
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


