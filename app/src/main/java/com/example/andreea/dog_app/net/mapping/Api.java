package com.example.andreea.dog_app.net.mapping;

/**
 * Created by Andreea on 17.12.2016.
 */
public class Api {
    public static class Dog {
        public static final String URL = "api/dog";
        public static final String DOG_CREATED = "dog/created";
        public static final String DOG_UPDATED = "dog/updated";
        public static final String DOG_DELETED = "dog/deleted";
        public static final String _ID = "_id";
        public static final String TEXT = "text";
        public static final String STATUS = "status";
        public static final String IMG = "img";
        public static final String UPDATED = "updated";
        public static final String USER_ID = "user";
        public static final String VERSION = "version";
    }

    public static class Auth {
        public static final String TOKEN = "token";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }
}