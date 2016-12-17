package com.example.andreea.dog_app.net.mapping.readers;

/**
 * Created by Andreea on 17.12.2016.
 */

import android.util.JsonReader;

import com.example.andreea.dog_app.net.Issue;
import com.example.andreea.dog_app.net.mapping.ResourceReader;

import java.io.IOException;

public class IssueReader implements ResourceReader<Issue, JsonReader> {
    @Override
    public Issue read(JsonReader reader) throws IOException {
        Issue issue = new Issue();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            issue.add(name, reader.nextString());
        }
        reader.endObject();
        return issue;
    }
}

