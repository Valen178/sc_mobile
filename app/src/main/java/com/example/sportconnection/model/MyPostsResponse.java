package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MyPostsResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("post")
    private List<Post> posts;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}

