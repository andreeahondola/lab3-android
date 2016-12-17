package com.example.andreea.dog_app.net.mapping;

/**
 * Created by Andreea on 17.12.2016.
 */

import org.json.JSONException;

import java.io.IOException;

public interface ResourceReader<E, Reader> {
    E read(Reader reader) throws IOException, JSONException, Exception;
}
