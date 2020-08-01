package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieIDRequestModel extends BaseRequestModel {
    @JsonProperty(value = "movie_id", required = true)
    private String movie_id;

    @JsonCreator
    public MovieIDRequestModel(@JsonProperty(value = "email", required = true) String email,
                               @JsonProperty(value = "movie_id", required = true) String movie_id)
    {
        super(email);
        this.movie_id = movie_id;
    }

    @JsonProperty("movie_id")
    public String getMovie_id()
    {
        return movie_id;
    }
}
