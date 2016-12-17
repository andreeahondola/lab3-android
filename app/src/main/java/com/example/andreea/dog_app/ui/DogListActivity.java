package com.example.andreea.dog_app.ui;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andreea.dog_app.DogApp;
import com.example.andreea.dog_app.R;
import com.example.andreea.dog_app.content.Dog;
import com.example.andreea.dog_app.util.Cancellable;
import com.example.andreea.dog_app.util.DialogUtils;
import com.example.andreea.dog_app.util.OnErrorListener;
import com.example.andreea.dog_app.util.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * An activity representing a list of Dogs. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DogDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class DogListActivity extends AppCompatActivity {

    public static final String TAG = DogListActivity.class.getSimpleName();

    /**
     * Whether or not the the dogs were loaded.
     */
    private boolean mDogsLoaded;

    /**
     * Reference to the singleton app used to access the app state and logic.
     */
    private DogApp mApp;

    /**
     * Reference to the last async call used for cancellation.
     */
    private Cancellable mGetDogsAsyncCall, mCreateDogAsyncCall;
    private View mContentLoadingView;
    private RecyclerView mRecyclerView;
    private int GRID_ITEMS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mApp = (DogApp) getApplication();
        setContentView(R.layout.activity_dog_list);
        setupToolbar();
        setupFloatingActionBar();
        setupRecyclerView();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        startGetDogsAsyncCall();
        mApp.getDogManager().subscribeChangeListener();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        ensureGetDogsAsyncCallCancelled();
        mApp.getDogManager().unsubscribeChangeListener();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
    }

    private void setupFloatingActionBar() {
        FloatingActionButton fabCreate = (FloatingActionButton) findViewById(R.id.fabCreate);
        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(DogListActivity.this, DogCreateInputActivity.class);
                DogListActivity.this.startActivity(myIntent);
            }
        });
    }

    private void setupRecyclerView() {
        mContentLoadingView = findViewById(R.id.content_loading);
        mRecyclerView = (RecyclerView) findViewById(R.id.dog_list);
    }

    private void startGetDogsAsyncCall() {
        if (mDogsLoaded) {
            Log.d(TAG, "start getDogsAsyncCall - content already loaded, return");
            return;
        }
        showLoadingIndicator();
        mGetDogsAsyncCall = mApp.getDogManager().getDogsAsync(
                new OnSuccessListener<List<Dog>>() {
                    @Override
                    public void onSuccess(final List<Dog> dogs) {
                        Log.d(TAG, "getDogsAsyncCall - success");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showContent(dogs);
                            }
                        });
                    }
                }, new OnErrorListener() {
                    @Override
                    public void onError(final Exception e) {
                        Log.d(TAG, "getDogsAsyncCall - error");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showError(e);
                            }
                        });
                    }
                }
        );
    }


    private void ensureGetDogsAsyncCallCancelled() {
        if (mGetDogsAsyncCall != null) {
            Log.d(TAG, "ensureGetDogsAsyncCallCancelled - cancelling the task");
            mGetDogsAsyncCall.cancel();
        }
    }

    private void showError(Exception e) {
        Log.e(TAG, "showError", e);
        if (mContentLoadingView.getVisibility() == View.VISIBLE) {
            mContentLoadingView.setVisibility(View.GONE);
        }
        DialogUtils.showError(this, e);
    }

    private void showLoadingIndicator() {
        Log.d(TAG, "showLoadingIndicator");
        mRecyclerView.setVisibility(View.GONE);
        mContentLoadingView.setVisibility(View.VISIBLE);
    }

    private void showContent(final List<Dog> dogs) {
        Log.d(TAG, "showContent");
        mRecyclerView.setAdapter(new DogRecyclerViewAdapter(dogs));
        mContentLoadingView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), GRID_ITEMS);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    public class DogRecyclerViewAdapter extends RecyclerView.Adapter<DogRecyclerViewAdapter.ViewHolder> {

        private final List<Dog> mValues;

        public DogRecyclerViewAdapter(List<Dog> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dog_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).getId());
            holder.mContentView.setText(mValues.get(position).getText());
            Picasso.with(getApplicationContext())
                    .load(mValues.get(position).getImg())
                    .placeholder(R.drawable.dog_placeholder)
                    .error(R.drawable.dog_placeholder_error)
                    .into(holder.mImgView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DogDetailActivity.class);
                    intent.putExtra(DogDetailFragment.DOG_ID, holder.mItem.getId());
                    intent.putExtra(DogDetailFragment.DOG_IMG, holder.mItem.getImg());
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            private final ImageView mImgView;
            public Dog mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
                mImgView = (ImageView) view.findViewById(R.id.dog_img);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
