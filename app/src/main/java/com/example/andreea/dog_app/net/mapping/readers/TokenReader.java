package com.example.andreea.dog_app.net.mapping.readers;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.util.JsonReader;

import com.example.andreea.dog_app.net.mapping.ResourceReader;

import java.io.IOException;

import static com.example.andreea.dog_app.net.mapping.Api.Auth.TOKEN;

public class TokenReader implements ResourceReader<String, JsonReader> {
    @Override
    public String read(JsonReader reader) throws IOException {
        reader.beginObject();
        String token = null;
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(TOKEN)) {
                token = reader.nextString();
            }
        }
        reader.endObject();
        return token;
    }
}
