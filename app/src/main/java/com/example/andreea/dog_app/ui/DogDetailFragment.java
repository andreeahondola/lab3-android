package com.example.andreea.dog_app.ui;

/**
 * Created by Andreea on 17.12.2016.
 */
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.andreea.dog_app.DogApp;
import com.example.andreea.dog_app.R;
import com.example.andreea.dog_app.content.Dog;
import com.example.andreea.dog_app.util.Cancellable;
import com.example.andreea.dog_app.util.DialogUtils;
import com.example.andreea.dog_app.util.OnErrorListener;
import com.example.andreea.dog_app.util.OnSuccessListener;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Dog detail screen.
 * This fragment is either contained in a {@link DogListActivity}
 * in two-pane mode (on tablets) or a {@link DogDetailActivity}
 * on handsets.
 */
public class DogDetailFragment extends Fragment {
    public static final String TAG = DogDetailFragment.class.getSimpleName();

    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String DOG_ID = "dog_id";
    public static final String DOG_IMG = "dog_img";

    /**
     * The dummy content this fragment is presenting.
     */
    private Dog mDog;

    private DogApp mApp;

    private Cancellable mFetchDogAsync;
    private Cancellable mDeleteDogAsync;
    private EditText mDogTextView;
    private CollapsingToolbarLayout mAppBarLayout;
    private ImageView mDogImageView;
    private FloatingActionButton mDogUpdateFab, mDogDeleteFab;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DogDetailFragment() {
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        mApp = (DogApp) context.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(DOG_ID)) {
            // In a real-world scenario, use a Loader
            // to load content from a content provider.
            Activity activity = this.getActivity();
            mAppBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.dog_detail, container, false);
        mDogTextView = (EditText) rootView.findViewById(R.id.dog_text);
        mDogImageView = (ImageView) rootView.findViewById(R.id.dog_img);
        setupDeleteFab(rootView);
        setupUpdateFab(rootView);
        fillDogDetails();
        fetchDogAsync();
        return rootView;
    }

    private void setupUpdateFab(View rootView) {
        mDogUpdateFab = (FloatingActionButton) rootView.findViewById(R.id.fabUpdate);
        mDogUpdateFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDogAsync();
            }
        });
    }

    private void setupDeleteFab(View rootView) {
        mDogDeleteFab = (FloatingActionButton) rootView.findViewById(R.id.fabDelete);
        mDogDeleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDogAsync();
            }
        });
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    private void fetchDogAsync() {
        mFetchDogAsync = mApp.getDogManager().getDogAsync(
                getArguments().getString(DOG_ID),
                new OnSuccessListener<Dog>() {

                    @Override
                    public void onSuccess(final Dog dog) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDog = dog;
                                fillDogDetails();
                            }
                        });
                    }
                }, new OnErrorListener() {

                    @Override
                    public void onError(final Exception e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.showError(getActivity(), e);
                            }
                        });
                    }
                });
    }

    private void deleteDogAsync() {
        mDeleteDogAsync = mApp.getDogManager().deleteDogAsync(
                getArguments().getString(DOG_ID),
                new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(final Boolean bol) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bol) {
                                    // dog has been deleted, nothing to see here, go back to list view
                                    getFragmentManager().popBackStack();
                                }
                            }
                        });
                    }
                },
                new OnErrorListener() {
                    @Override
                    public void onError(final Exception e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.showError(getActivity(), e);
                            }
                        });
                    }
                }
        );
    }

    private void updateDogAsync() {
        // copy dog in case of fail, to not modify the original dog
        String newDogText = mDogTextView.getText().toString();
        Dog newDog = mDog;
        newDog.setText(newDogText);
        mDeleteDogAsync = mApp.getDogManager().updateDogAsync(
                newDog,
                new OnSuccessListener<Dog>() {
                    @Override
                    public void onSuccess(final Dog dog) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDog.getUpdated() < dog.getUpdated()) {
                                    mDog = dog;
                                    fillDogDetails();
                                }
                            }
                        });
                    }
                },
                new OnErrorListener() {
                    @Override
                    public void onError(final Exception e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.showError(getActivity(), e);
                            }
                        });
                    }
                }
        );
    }

    private void fillDogDetails() {
        if (mDog != null) {
            if (mAppBarLayout != null) {
                mAppBarLayout.setTitle(mDog.getText());
            }
            mDogTextView.setText(mDog.getText());
            Picasso.with(mApp)
                    .load(getArguments().getString(DOG_IMG))
                    .placeholder(R.drawable.dog_placeholder)
                    .error(R.drawable.dog_placeholder_error)
                    .into(mDogImageView);
        }
    }
}
