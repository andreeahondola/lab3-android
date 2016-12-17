package com.example.andreea.dog_app.net;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.content.Context;
import android.util.Log;

import com.example.andreea.dog_app.R;
import com.example.andreea.dog_app.content.Dog;
import com.example.andreea.dog_app.net.mapping.readers.DogJsonObjectReader;
import com.example.andreea.dog_app.net.mapping.readers.IdJsonObjectReader;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.example.andreea.dog_app.net.mapping.Api.Dog.DOG_CREATED;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.DOG_DELETED;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.DOG_UPDATED;

public class DogSocketClient {
    private static final String TAG = DogSocketClient.class.getSimpleName();
    private final Context mContext;
    private Socket mSocket;
    private ResourceChangeListener<Dog> mResourceListener;

    public DogSocketClient(Context context) {
        mContext = context;
        Log.d(TAG, "created");
    }

    public void subscribe(final ResourceChangeListener<Dog> resourceListener) {
        Log.d(TAG, "subscribe");
        mResourceListener = resourceListener;
        try {
            mSocket = IO.socket(mContext.getString(R.string.api_url));
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "socket connected");
                }
            });
            mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "socket disconnected");
                }
            });
            mSocket.on(DOG_CREATED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Dog dog = new DogJsonObjectReader().read((JSONObject) args[0]);
                        Log.d(TAG, String.format("dog created %s", dog.toString()));
                        mResourceListener.onCreated(dog);
                    } catch (Exception e) {
                        Log.w(TAG, "dog created", e);
                        mResourceListener.onError(new ResourceException(e));
                    }
                }
            });
            mSocket.on(DOG_UPDATED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Dog dog = new DogJsonObjectReader().read((JSONObject) args[0]);
                        Log.d(TAG, String.format("dog updated %s", dog.toString()));
                        mResourceListener.onUpdated(dog);
                    } catch (Exception e) {
                        Log.w(TAG, "dog updated", e);
                        mResourceListener.onError(new ResourceException(e));
                    }
                }
            });
            mSocket.on(DOG_DELETED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        String id = new IdJsonObjectReader().read((JSONObject) args[0]);
                        Log.d(TAG, String.format("dog deleted %s", id));
                        mResourceListener.onDeleted(id);
                    } catch (Exception e) {
                        Log.w(TAG, "dog deleted", e);
                        mResourceListener.onError(new ResourceException(e));
                    }
                }
            });
            mSocket.connect();
        } catch (Exception e) {
            Log.w(TAG, "socket error", e);
            mResourceListener.onError(new ResourceException(e));
        }
    }

    public void unsubscribe() {
        Log.d(TAG, "unsubscribe");
        if (mSocket != null) {
            mSocket.disconnect();
        }
        mResourceListener = null;
    }

}
