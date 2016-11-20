package org.kilo.laughbagandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

public class PopUpActivity extends Activity {

    @Override
    protected void onStart() {
        super.onStart();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.popup_activity);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.3));

    }


    public void onClick(View view){
        switch (view.getId()){
            case R.id.cancel:{
                finish();
                break;
            }
            case R.id.yes:{

                Bundle extras = getIntent().getExtras();
                if (extras != null){
                    String laughFile = extras.getString("laughPath");


                    assert laughFile != null;
                    File file = new File(laughFile);
                    if (file.exists()) {
                        boolean d = file.delete();
                    } else {
                        Toast.makeText(getApplicationContext(), "The file was not found", Toast.LENGTH_SHORT);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }

                finish();
                break;
            }
        }

    }
}
