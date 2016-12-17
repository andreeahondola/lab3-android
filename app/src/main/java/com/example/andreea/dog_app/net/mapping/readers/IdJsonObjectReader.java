package com.example.andreea.dog_app.net.mapping.readers;

/**
 * Created by Andreea on 17.12.2016.
 */

import com.example.andreea.dog_app.net.mapping.ResourceReader;

import org.json.JSONObject;

import static com.example.andreea.dog_app.net.mapping.Api.Dog._ID;


public class IdJsonObjectReader implements ResourceReader<String, JSONObject> {
    private static final String TAG = IdJsonObjectReader.class.getSimpleName();

    @Override
    public String read(JSONObject obj) throws Exception {
        return obj.getString(_ID);
    }
}
