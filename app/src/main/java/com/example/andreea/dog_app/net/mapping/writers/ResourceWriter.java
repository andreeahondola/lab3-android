package com.example.andreea.dog_app.net.mapping.writers;

/**
 * Created by Andreea on 17.12.2016.
 */

import java.io.IOException;

public interface ResourceWriter<E, Writer> {
    void write(E e, Writer writer) throws IOException;
}
