package com.example.andreea.dog_app.service;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.content.Context;
import android.util.Log;


import com.example.andreea.dog_app.content.Dog;
import com.example.andreea.dog_app.content.User
import com.example.andreea.dog_app.content.database.DogDatabase;
import com.example.andreea.dog_app.net.DogRestClient;
import com.example.andreea.dog_app.net.DogSocketClient;
import com.example.andreea.dog_app.net.LastModifiedList;
import com.example.andreea.dog_app.net.ResourceChangeListener;
import com.example.andreea.dog_app.net.ResourceException;
import com.example.andreea.dog_app.util.Cancellable;
import com.example.andreea.dog_app.util.OnErrorListener;
import com.example.andreea.dog_app.util.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DogManager extends Observable {
    private static final String TAG = DogManager.class.getSimpleName();
    private final DogDatabase mKD;

    private ConcurrentMap<String, Dog> mDogs = new ConcurrentHashMap<String, Dog>();
    private String mDogsLastUpdate;

    private final Context mContext;
    private DogRestClient mDogRestClient;
    private DogSocketClient mDogSocketClient;
    private String mToken;
    private User mCurrentUser;

    public DogManager(Context context) {
        mContext = context;
        mKD = new DogDatabase(context);
    }

    public void setDogRestClient(DogRestClient dogRestClient) {
        mDogRestClient = dogRestClient;
    }

    public void setDogSocketClient(DogSocketClient dogSocketClient) {
        mDogSocketClient = dogSocketClient;
    }

    public Cancellable createDogAsync(final Dog dog, final OnSuccessListener<Boolean> successListener, OnErrorListener errorListener) {
        Log.d(TAG, "createDogAsync");
        return mDogRestClient.createDogAsync(dog, new OnSuccessListener<Dog>() {
            @Override
            public void onSuccess(Dog dog) {
                if (dog != null) {
                    setChanged();
                    mDogs.put(dog.getId(), dog);
                    successListener.onSuccess(true);
                }
                notifyObservers();
            }
        }, errorListener);
    }

    public Cancellable deleteDogAsync(final String dogId, final OnSuccessListener<Boolean> successListener, OnErrorListener errorListener) {
        Log.d(TAG, "deleteDogAsync");
        return mDogRestClient.deleteAsync(dogId, new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean deleted) {
                Log.d(TAG, "deleteDogAsync succeeded");
                if (deleted) {
                    setChanged();
                    mDogs.remove(dogId);
                }
                successListener.onSuccess(deleted);
                notifyObservers();
            }
        }, errorListener);
    }

    public Cancellable getDogsAsync(final OnSuccessListener<List<Dog>> successListener, OnErrorListener errorListener) {
        Log.d(TAG, "getDogsAsync");
        return mDogRestClient.searchAsync(mDogsLastUpdate, new OnSuccessListener<LastModifiedList<Dog>>() {

            @Override
            public void onSuccess(LastModifiedList<Dog> result) {
                Log.d(TAG, "getDogsAsync succeeded");
                List<Dog> dogs = result.getList();
                if (dogs != null) {
                    mDogsLastUpdate = result.getLastModified();
                    updateCachedDogs(dogs);
                }
                successListener.onSuccess(cachedDogsByUpdated());
                notifyObservers();
            }
        }, errorListener);
    }

    public Cancellable getDogAsync(final String dogId, final OnSuccessListener<Dog> successListener, final OnErrorListener errorListener) {
        Log.d(TAG, "getDogAsync");
        return mDogRestClient.readAsync(dogId, new OnSuccessListener<Dog>() {

            @Override
            public void onSuccess(Dog dog) {
                Log.d(TAG, "getDogAsync succeeded");
                if (dog == null) {
                    setChanged();
                    mDogs.remove(dogId);
                } else if (!dog.equals(mDogs.get(dog.getId()))) {
                    setChanged();
                    mDogs.put(dogId, dog);

                }
                successListener.onSuccess(dog);
                notifyObservers();
            }
        }, errorListener);
    }

    public Cancellable updateDogAsync(final Dog dog, final OnSuccessListener<Dog> successListener, final OnErrorListener errorListener) {
        Log.d(TAG, "saveDogAsync");
        return mDogRestClient.updateAsync(dog, new OnSuccessListener<Dog>() {

            @Override
            public void onSuccess(Dog dog) {
                Log.d(TAG, "saveDogAsync succeeded");
                mDogs.put(dog.getId(), dog);
                successListener.onSuccess(dog);
                setChanged();
                notifyObservers();
            }
        }, errorListener);
    }

    public void subscribeChangeListener() {
        mDogSocketClient.subscribe(new ResourceChangeListener<Dog>() {
            @Override
            public void onCreated(Dog dog) {
                Log.d(TAG, "changeListener, onCreated");
                ensureDogCached(dog);
            }

            @Override
            public void onUpdated(Dog dog) {
                Log.d(TAG, "changeListener, onUpdated");
                ensureDogCached(dog);
            }

            @Override
            public void onDeleted(String dogId) {
                Log.d(TAG, "changeListener, onDeleted");
                if (mDogs.remove(dogId) != null) {
                    setChanged();
                    notifyObservers();
                }
            }

            private void ensureDogCached(Dog dog) {
                if (!dog.equals(mDogs.get(dog.getId()))) {
                    Log.d(TAG, "changeListener, cache updated");
                    mDogs.put(dog.getId(), dog);
                    setChanged();
                    notifyObservers();
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "changeListener, error", t);
            }
        });
    }

    public void unsubscribeChangeListener() {
        mDogSocketClient.unsubscribe();
    }

    private void updateCachedDogs(List<Dog> dogs) {
        Log.d(TAG, "updateCachedDogs");
        for (Dog dog : dogs) {
            mDogs.put(dog.getId(), dog);
        }
        setChanged();
    }

    private List<Dog> cachedDogsByUpdated() {
        ArrayList<Dog> dogs = new ArrayList<>(mDogs.values());
        Collections.sort(dogs, new DogByUpdatedComparator());
        return dogs;
    }

    public List<Dog> getCachedDogs() {
        return cachedDogsByUpdated();
    }

    public Cancellable loginAsync(String username, String password, final OnSuccessListener<String> successListener, final OnErrorListener errorListener) {
        final User user = new User(username, password);
        return mDogRestClient.getToken(
                user, new OnSuccessListener<String>() {

                    @Override
                    public void onSuccess(String token) {
                        mToken = token;
                        if (mToken != null) {
                            user.setToken(mToken);
                            setCurrentUser(user);
                            mKD.saveUser(user);
                            successListener.onSuccess(mToken);
                        } else {
                            errorListener.onError(new ResourceException(new IllegalArgumentException("Invalid credentials")));
                        }
                    }
                }, errorListener);
    }

    public void setCurrentUser(User currentUser) {
        mCurrentUser = currentUser;
        mDogRestClient.setUser(currentUser);
    }

    public User getCurrentUser() {
        return mKD.getCurrentUser();
    }

    private class DogByUpdatedComparator implements java.util.Comparator<Dog> {
        @Override
        public int compare(Dog n1, Dog n2) {
            return (int) (n1.getUpdated() - n2.getUpdated());
        }
    }
}
