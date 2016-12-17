package com.example.andreea.dog_app.ui;

/**
 * Created by Andreea on 17.12.2016.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.andreea.dog_app.DogApp;
import com.example.andreea.dog_app.R;
import com.example.andreea.dog_app.content.User;
import com.example.andreea.dog_app.service.DogManager;
import com.example.andreea.dog_app.util.Cancellable;
import com.example.andreea.dog_app.util.DialogUtils;
import com.example.andreea.dog_app.util.OnErrorListener;
import com.example.andreea.dog_app.util.OnSuccessListener;

public class LoginActivity extends AppCompatActivity {

    private Cancellable mCancellable;
    private DogManager mDogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mDogManager = ((DogApp) getApplication()).getDogManager();
        User user = mDogManager.getCurrentUser();
        if (user != null) {
            startDogListActivity();
            finish();
        }
        setupToolbar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCancellable != null) {
            mCancellable.cancel();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
                Snackbar.make(view, "Authenticating, please wait", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();
            }
        });
    }

    private void login() {
        EditText usernameEditText = (EditText) findViewById(R.id.username);
        EditText passwordEditText = (EditText) findViewById(R.id.password);
        mCancellable = mDogManager
                .loginAsync(
                        usernameEditText.getText().toString(), passwordEditText.getText().toString(),
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startDogListActivity();
                                    }
                                });
                            }
                        }, new OnErrorListener() {
                            @Override
                            public void onError(final Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogUtils.showError(LoginActivity.this, e);
                                    }
                                });
                            }
                        });
    }

    private void startDogListActivity() {
        startActivity(new Intent(this, DogListActivity.class));
    }
}

