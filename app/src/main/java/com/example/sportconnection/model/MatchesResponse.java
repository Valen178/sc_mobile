package com.example.sportconnection.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MatchesResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("matches")
    private List<Match> matches;

    @SerializedName("count")
    private int count;

    public boolean isSuccess() {
        return success;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public int getCount() {
        return count;
    }
}

