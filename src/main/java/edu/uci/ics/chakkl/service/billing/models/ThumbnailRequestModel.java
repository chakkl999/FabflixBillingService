package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThumbnailRequestModel {
    @JsonProperty(value = "movie_ids", required = true)
    String movie_id[];

    @JsonCreator
    public ThumbnailRequestModel(@JsonProperty(value = "movie_ids", required = true) String movie_id[])
    {
        this.movie_id = movie_id;
    }

    @JsonProperty("movie_ids")
    public String[] getMovie_id()
    {
        return movie_id;
    }
}
