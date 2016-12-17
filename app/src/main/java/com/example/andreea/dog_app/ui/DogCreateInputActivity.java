package com.example.andreea.dog_app.ui;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.andreea.dog_app.DogApp;
import com.example.andreea.dog_app.R;
import com.example.andreea.dog_app.content.Dog;
import com.example.andreea.dog_app.util.Cancellable;
import com.example.andreea.dog_app.util.DialogUtils;
import com.example.andreea.dog_app.util.OnErrorListener;
import com.example.andreea.dog_app.util.OnSuccessListener;

public class DogCreateInputActivity extends AppCompatActivity {
    public static final String TAG = DogCreateInputActivity.class.getSimpleName();
    private DogApp mApp;
    private Cancellable mCreateDogAsyncCall;
    private EditText textDog;
    private EditText imgUrlDog;
    // ar tb rafinata clasa, nu imi place ce am facut aici...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (DogApp) getApplication();
        setContentView(R.layout.dog_create_input);

        textDog = (EditText) findViewById(R.id.textDog);
        imgUrlDog = (EditText) findViewById(R.id.imgUrlDog);
        Button createDog = (Button) findViewById(R.id.createDog);
        Button cancelCreateDog = (Button) findViewById(R.id.cancelCreateDog);

        createDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String text = textDog.getText().toString();
                String imgUrl = imgUrlDog.getText().toString();
                Dog dog = new Dog();
                dog.setText(text);
                dog.setImg(imgUrl);
                mCreateDogAsyncCall = mApp.getDogManager().createDogAsync(
                        dog,
                        new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean created) {
                                if (created) {
                                    Snackbar.make(v, "Dog created", Snackbar.LENGTH_LONG).show();
                                    finish();// terminate activity
                                }
                            }
                        }, new OnErrorListener() {
                            @Override
                            public void onError(final Exception e) {
                                Log.d(TAG, "getDogsAsyncCall - error");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(v, "Error: " + e, Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                );
            }
        });
        cancelCreateDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mCreateDogAsyncCall != null) {
                    mCreateDogAsyncCall.cancel();
                    Snackbar.make(v, "Dog create has been cancelled!", Snackbar.LENGTH_LONG).show();
                    finish();// terminate activity
                }
            }
        });

    }
}