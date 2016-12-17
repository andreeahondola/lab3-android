package com.example.andreea.dog_app.net.mapping.readers;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.util.JsonReader;

import com.example.andreea.dog_app.net.mapping.ResourceReader;

import java.util.ArrayList;
import java.util.List;

public class ResourceListReader<E> implements ResourceReader<List<E>, JsonReader> {

    private final ResourceReader<E, JsonReader> mResourceReader;

    public ResourceListReader(ResourceReader<E, JsonReader> resourceReader) {
        mResourceReader = resourceReader;
    }

    @Override
    public List<E> read(JsonReader reader) throws Exception {
        List<E> entityList = new ArrayList<E>();
        reader.beginArray();
        while (reader.hasNext()) {
            entityList.add(mResourceReader.read(reader));
        }
        reader.endArray();
        return entityList;
    }
}
