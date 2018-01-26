package edu.illinois.finalproject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailedViewPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view_page);

        final TextView ocrResult = (TextView) findViewById(R.id.ocrResult);
        ImageView ocrImage = (ImageView) findViewById(R.id.ocrImage);
        Button copyButton = (Button) findViewById(R.id.copyButton);

        Intent detailedPageIntent = getIntent();
        Bundle thisBundle = detailedPageIntent.getExtras();

        if (thisBundle != null) {
            ocrResult.setText((String)thisBundle.get("result"));
            String filePath = (String) thisBundle.get("memory location");
            ocrImage.setImageBitmap(UtilityMethods.getCorrectOrientedImage(filePath));
        }

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilityMethods.copyToClipboard(ocrResult, DetailedViewPage.this);
            }
        });
    }
}
