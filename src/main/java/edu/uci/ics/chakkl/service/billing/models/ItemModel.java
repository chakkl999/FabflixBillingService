package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemModel extends CartItemInfo{
    @JsonProperty(value = "movie_title", required = true)
    private String movie_title;
    @JsonProperty(value = "backdrop_path")
    private String backdrop_path;
    @JsonProperty(value = "poster_path")
    private String poster_path;

    @JsonCreator
    public ItemModel(@JsonProperty(value = "email", required = true) String email,
                     @JsonProperty(value = "unit_price", required = true) float unit_price,
                     @JsonProperty(value = "discount", required = true) float discount,
                     @JsonProperty(value = "quantity", required = true) int quantity,
                     @JsonProperty(value = "movie_id", required = true) String movie_id,
                     @JsonProperty(value = "movie_title", required = true) String movie_title,
                     @JsonProperty(value = "backdrop_path") String backdrop_path,
                     @JsonProperty(value = "poster_path") String poster_path)
    {
        super(email, movie_id, quantity, unit_price, discount);
        this.movie_title = movie_title;
        this.backdrop_path = backdrop_path;
        this.poster_path = poster_path;
    }

    @JsonProperty("movie_title")
    public String getMovie_title() {
        return movie_title;
    }
    @JsonProperty("backdrop_path")
    public String getBackdrop_path() {
        return backdrop_path;
    }
    @JsonProperty("poster_path")
    public String getPoster_path() {
        return poster_path;
    }
}
