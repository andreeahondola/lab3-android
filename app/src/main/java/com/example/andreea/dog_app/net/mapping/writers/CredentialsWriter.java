package com.example.andreea.dog_app.net.mapping.writers;

/**
 * Created by Andreea on 17.12.2016.
 */
import android.util.JsonWriter;


import com.example.andreea.dog_app.content.User;

import java.io.IOException;

import static com.example.andreea.dog_app.net.mapping.Api.Auth.PASSWORD;
import static com.example.andreea.dog_app.net.mapping.Api.Auth.USERNAME;

public class CredentialsWriter implements ResourceWriter<User, JsonWriter> {
    @Override
    public void write(User user, JsonWriter writer) throws IOException {
        writer.beginObject();
        {
            writer.name(USERNAME).value(user.getUsername());
            writer.name(PASSWORD).value(user.getPassword());
        }
        writer.endObject();
    }
}
