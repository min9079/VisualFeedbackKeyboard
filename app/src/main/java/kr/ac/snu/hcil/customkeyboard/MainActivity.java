package kr.ac.snu.hcil.customkeyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.RawRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();
    private Button mSetupButton;
    private Button mTestButton;
    private Button mExitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSetupButton = (Button) findViewById(R.id.setup_button);
        mSetupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
        mTestButton = (Button) findViewById(R.id.test_button);
        mTestButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TestActivity.class));
            }
        });
        mExitButton = (Button) findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MainActivity.super.onBackPressed();
            }
        });

        Log.d(TAG, getFilesDir().getAbsolutePath());
        Log.d(TAG, getExternalFilesDir(null).getAbsolutePath());

        File directory = new File(getExternalFilesDir(null), "external");
        if (! directory.exists()){
            Log.d(TAG, "Let\'s make external directory");
            directory.mkdir();

            SystemUtils.saveFile(getResources().openRawResource(R.raw.error_probs), directory,"/error_probs.json");
            SystemUtils.saveFile(getResources().openRawResource(R.raw.phonetic_index), directory, "/phonetic_index.json");
            SystemUtils.saveFile(getResources().openRawResource(R.raw.twitter_c_lm), directory, "/twitter_c_lm.klm");
            SystemUtils.saveFile(getResources().openRawResource(R.raw.twitter_lm), directory, "/twitter_lm.klm");
            SystemUtils.saveFile(getResources().openRawResource(R.raw.all_misspellings_4gram_lm_b), directory, "/all_misspellings_4gram_lm_b.klm");
            SystemUtils.saveFile(getResources().openRawResource(R.raw.twitter_voca_alpha_lower_128k), directory, "/twitter_voca_alpha_lower_128k.txt");

       } else {
            Log.d(TAG, "The directory already exist!");
            File[] listOfFiles = directory.listFiles();
            for(int i=0; i< listOfFiles.length; i++) {
                Log.d(TAG, listOfFiles[i].getName());
            }
       }
    }


}
