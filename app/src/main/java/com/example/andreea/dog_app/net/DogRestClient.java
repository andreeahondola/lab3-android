package com.example.andreea.dog_app.net;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.example.andreea.dog_app.DogApp;
import com.example.andreea.dog_app.R;
import com.example.andreea.dog_app.content.Dog;
import com.example.andreea.dog_app.content.User;
import com.example.andreea.dog_app.net.mapping.writers.CredentialsWriter;
import com.example.andreea.dog_app.net.mapping.readers.DogReader;
import com.example.andreea.dog_app.net.mapping.writers.DogWriter;
import com.example.andreea.dog_app.net.mapping.readers.IssueReader;
import com.example.andreea.dog_app.net.mapping.readers.ResourceListReader;
import com.example.andreea.dog_app.net.mapping.readers.TokenReader;
import com.example.andreea.dog_app.util.Cancellable;
import com.example.andreea.dog_app.util.OnErrorListener;
import com.example.andreea.dog_app.util.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DogRestClient {
    private static final String TAG = DogRestClient.class.getSimpleName();
    public static final String APPLICATION_JSON = "application/json";
    public static final String UTF_8 = "UTF-8";
    public static final String LAST_MODIFIED = "Last-Modified";

    private final OkHttpClient mOkHttpClient;
    private final String mApiUrl;
    private final String mDogUrl;
    private final Context mContext;
    private final String mAuthUrl;
    private User mUser;

    public DogRestClient(Context context) {
        mContext = context;
        mOkHttpClient = new OkHttpClient();
        mApiUrl = context.getString(R.string.api_url);
        mDogUrl = mApiUrl.concat("/api/dog");
        mAuthUrl = mApiUrl.concat("/api/auth");
        Log.d(TAG, "DogRestClient created");
    }

    private void addAuthToken(Request.Builder requestBuilder) {
        if (mUser == null) {
            mUser = ((DogApp) mContext).getDogManager().getCurrentUser();
        }
        if (mUser != null) {
            requestBuilder.header("Authorization", String.format("Bearer %s", mUser.getToken()));
        }
    }

    public void setUser(User user) {
        mUser = user;
    }

    public Cancellable deleteAsync(String dogId, OnSuccessListener<Boolean> successListener, OnErrorListener errorListener) {
        Request.Builder builder = new Request.Builder()
                .url(String.format("%s/%s", mDogUrl, dogId))
                .delete();
        addAuthToken(builder);
        return new CancellableOkHttpAsync<>(
                builder.build(),
                new ResponseReader<Boolean>() {
                    @Override
                    public Boolean read(Response response) throws Exception {
                        if (response.code() == 204) { // no content
                            return true;
                        } else { // 404 not found
                            return false;
                        }
                    }
                },
                successListener,
                errorListener
        );
    }

    public Cancellable createDogAsync(Dog dog, OnSuccessListener<Dog> successListener, OnErrorListener errorListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos, UTF_8));
            new DogWriter().write(dog, writer);
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "createAsync failed", e);
            errorListener.onError(new ResourceException(e));
        } finally {
            Request.Builder builder = new Request.Builder()
                    .url(String.format("%s/", mDogUrl))
                    .post(RequestBody.create(MediaType.parse(APPLICATION_JSON), baos.toByteArray()));
            addAuthToken(builder);
            return new CancellableOkHttpAsync<Dog>(
                    builder.build(),
                    new ResponseReader<Dog>() {
                        @Override
                        public Dog read(Response response) throws Exception {
                            int code = response.code();
                            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), UTF_8));
                            if (code == 400) { //bad request
                                throw new ResourceException(new ResourceListReader<Issue>(new IssueReader()).read(reader));
                            }
                            return new DogReader().read(reader);// 201 created
                        }
                    },
                    successListener,
                    errorListener
            );
        }
    }

    private static interface ResponseReader<E> {
        E read(Response response) throws Exception;
    }

    public CancellableOkHttpAsync<String> getToken(User user, OnSuccessListener<String> successListener, OnErrorListener errorListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = null;
        try {
            writer = new JsonWriter(new OutputStreamWriter(baos, UTF_8));
            new CredentialsWriter().write(user, writer);
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "getToken failed", e);
            throw new ResourceException(e);
        }
        return new CancellableOkHttpAsync<String>(
                new Request.Builder()
                        .url(String.format("%s/session", mAuthUrl))
                        .post(RequestBody.create(MediaType.parse(APPLICATION_JSON), baos.toByteArray()))
                        .build(),
                new ResponseReader<String>() {
                    @Override
                    public String read(Response response) throws Exception {
                        JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), UTF_8));
                        if (response.code() == 201) { //created
                            return new TokenReader().read(reader);
                        } else {
                            return null;
                        }
                    }
                },
                successListener,
                errorListener
        );
    }

    public Cancellable searchAsync(String mDogsLastUpdate, final OnSuccessListener<LastModifiedList<Dog>> successListener, final OnErrorListener errorListener) {
        Request.Builder requestBuilder = new Request.Builder().url(mDogUrl);
        if (mDogsLastUpdate != null) {
            requestBuilder.header(LAST_MODIFIED, mDogsLastUpdate);
        }
        addAuthToken(requestBuilder);
        return new CancellableOkHttpAsync<LastModifiedList<Dog>>(
                requestBuilder.build(),
                new ResponseReader<LastModifiedList<Dog>>() {
                    @Override
                    public LastModifiedList<Dog> read(Response response) throws Exception {
                        JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), UTF_8));
                        if (response.code() == 304) { //not modified
                            return new LastModifiedList<Dog>(response.header(LAST_MODIFIED), null);
                        } else {
                            return new LastModifiedList<Dog>(
                                    response.header(LAST_MODIFIED),
                                    new ResourceListReader<Dog>(new DogReader()).read(reader));
                        }
                    }
                },
                successListener,
                errorListener
        );
    }

    public Cancellable readAsync(String dogId, final OnSuccessListener<Dog> successListener, final OnErrorListener errorListener) {
        Request.Builder builder = new Request.Builder().url(String.format("%s/%s", mDogUrl, dogId));
        addAuthToken(builder);
        return new CancellableOkHttpAsync<Dog>(
                builder.build(),
                new ResponseReader<Dog>() {
                    @Override
                    public Dog read(Response response) throws Exception {
                        if (response.code() == 200) {
                            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), UTF_8));
                            return new DogReader().read(reader);
                        } else { //404 not found
                            return null;
                        }
                    }
                },
                successListener,
                errorListener
        );
    }

    public Cancellable updateAsync(Dog dog, final OnSuccessListener<Dog> successListener, final OnErrorListener errorListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos, UTF_8));
            new DogWriter().write(dog, writer);
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "updateAsync failed", e);
            errorListener.onError(new ResourceException(e));
        } finally {
            Request.Builder builder = new Request.Builder()
                    .url(String.format("%s/%s", mDogUrl, dog.getId()))
                    .put(RequestBody.create(MediaType.parse(APPLICATION_JSON), baos.toByteArray()));
            addAuthToken(builder);
            return new CancellableOkHttpAsync<Dog>(
                    builder.build(),
                    new ResponseReader<Dog>() {
                        @Override
                        public Dog read(Response response) throws Exception {
                            int code = response.code();
                            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), UTF_8));
                            if (code == 400 || code == 409 || code == 405) { //bad request, conflict, method not allowed
                                throw new ResourceException(new ResourceListReader<Issue>(new IssueReader()).read(reader));
                            }
                            return new DogReader().read(reader);
                        }
                    },
                    successListener,
                    errorListener
            );
        }
    }


    private class CancellableOkHttpAsync<E> implements Cancellable {
        private Call mCall;

        public CancellableOkHttpAsync(
                final Request request,
                final ResponseReader<E> responseReader,
                final OnSuccessListener<E> successListener,
                final OnErrorListener errorListener) {
            try {
                mCall = mOkHttpClient.newCall(request);
                Log.d(TAG, String.format("started %s %s", request.method(), request.url()));
                //retry 3x, renew token
                mCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        notifyFailure(e, request, errorListener);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            notifySuccess(response, request, successListener, responseReader);
                        } catch (Exception e) {
                            notifyFailure(e, request, errorListener);
                        }
                    }
                });
            } catch (Exception e) {
                notifyFailure(e, request, errorListener);
            }
        }

        @Override
        public void cancel() {
            if (mCall != null) {
                mCall.cancel();
            }
        }

        private void notifySuccess(Response response, Request request, OnSuccessListener<E> successListener, ResponseReader<E> responseReader) throws Exception {
            if (mCall.isCanceled()) {
                Log.d(TAG, String.format("completed, but cancelled %s %s", request.method(), request.url()));
            } else {
                Log.d(TAG, String.format("completed %s", request.method(), request.url()));
                successListener.onSuccess(responseReader.read(response));
            }
        }

        private void notifyFailure(Exception e, Request request, OnErrorListener errorListener) {
            if (mCall.isCanceled()) {
                Log.d(TAG, String.format("failed, but cancelled %s %s", request.method(), request.url()));
            } else {
                Log.e(TAG, String.format("failed %s %s", request.method(), request.url()), e);
                errorListener.onError(e instanceof ResourceException ? e : new ResourceException(e));
            }
        }
    }
}
