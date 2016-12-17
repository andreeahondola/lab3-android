package com.example.andreea.dog_app.net;

/**
 * Created by Andreea on 17.12.2016.
 */

public interface ResourceChangeListener<E> {
    void onCreated(E e);

    void onUpdated(E e);

    void onDeleted(String id);

    void onError(Throwable t);
}
