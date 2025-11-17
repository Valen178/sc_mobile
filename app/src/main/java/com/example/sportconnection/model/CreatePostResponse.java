package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;

public class CreatePostResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("post")
    private Post post;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}

