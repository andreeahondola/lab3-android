package com.example.andreea.dog_app.net.mapping.readers;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.util.JsonReader;
import android.util.Log;


import com.example.andreea.dog_app.content.Dog;
import com.example.andreea.dog_app.net.mapping.ResourceReader;

import java.io.IOException;

import static com.example.andreea.dog_app.net.mapping.Api.Dog.IMG;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.STATUS;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.TEXT;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.UPDATED;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.USER_ID;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.VERSION;
import static com.example.andreea.dog_app.net.mapping.Api.Dog._ID;

public class DogReader implements ResourceReader<Dog, JsonReader> {
    private static final String TAG = DogReader.class.getSimpleName();

    @Override
    public Dog read(JsonReader reader) throws IOException {
        Dog dog = new Dog();
        reader.beginObject();
        while (reader.hasNext()) {
            String prop = reader.nextName();
            if (prop.equals(_ID)) {
                dog.setId(reader.nextString());
            } else if (prop.equals(TEXT)) {
                dog.setText(reader.nextString());
            } else if (prop.equals(STATUS)) {
                dog.setStatus(Dog.Status.valueOf(reader.nextString()));
            } else if (prop.equals(UPDATED)) {
                dog.setUpdated(reader.nextLong());
            } else if (prop.equals(IMG)) {
                dog.setImg(reader.nextString());
            } else if (prop.equals(USER_ID)) {
                dog.setUserId(reader.nextString());
            } else if (prop.equals(VERSION)) {
                dog.setVersion(reader.nextInt());
            } else {
                reader.skipValue();
                Log.w(TAG, String.format("Dog property '%s' ignored", prop));
            }
        }
        reader.endObject();
        return dog;
    }
}
