package com.example.andreea.dog_app.net.mapping.writers;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.util.JsonWriter;

import com.example.andreea.dog_app.content.Dog;

import java.io.IOException;

import static com.example.andreea.dog_app.net.mapping.Api.Dog.IMG;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.STATUS;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.TEXT;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.UPDATED;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.USER_ID;
import static com.example.andreea.dog_app.net.mapping.Api.Dog.VERSION;
import static com.example.andreea.dog_app.net.mapping.Api.Dog._ID;

public class DogWriter implements ResourceWriter<Dog, JsonWriter> {
    @Override
    public void write(Dog dog, JsonWriter writer) throws IOException {
        writer.beginObject();
        {
            if (dog.getId() != null) {
                writer.name(_ID).value(dog.getId());
            }
            writer.name(TEXT).value(dog.getText());
            writer.name(STATUS).value(dog.getStatus().name());
            writer.name(IMG).value(dog.getImg());
            if (dog.getUpdated() > 0) {
                writer.name(UPDATED).value(dog.getUpdated());
            }
            if (dog.getUserId() != null) {
                writer.name(USER_ID).value(dog.getUserId());
            }
            if (dog.getVersion() > 0) {
                writer.name(VERSION).value(dog.getVersion());
            }
        }
        writer.endObject();
    }
}