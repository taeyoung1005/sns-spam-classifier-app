package com.example.smsspamclassifierapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/spam_messages")
    Call<List<SpamMessage>> getSpamMessages();
}
