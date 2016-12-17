package com.example.andreea.dog_app.net;

/**
 * Created by Andreea on 17.12.2016.
 */

import java.util.List;

public class LastModifiedList<E> {
    private String mLastModified;
    private List<E> mList;

    public LastModifiedList(String lastModified, List<E> list) {
        mLastModified = lastModified;
        mList = list;
    }

    public String getLastModified() {
        return mLastModified;
    }

    public List<E> getList() {
        return mList;
    }
}
