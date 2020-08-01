package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemInfo {
    @JsonProperty(value = "email", required = true)
    private String email;
    @JsonProperty(value = "movie_id", required = true)
    private String movie_id;
    @JsonProperty(value = "quantity", required = true)
    private int quantity;
    @JsonProperty(value = "unit_price", required = true)
    private float unit_price;
    @JsonProperty(value = "discount", required = true)
    private float discount;

    @JsonCreator
    public CartItemInfo(@JsonProperty(value = "email", required = true) String email,
                        @JsonProperty(value = "movie_id", required = true) String movie_id,
                        @JsonProperty(value = "quantity", required = true) int quantity,
                        @JsonProperty(value = "unit_price", required = true) float unit_price,
                        @JsonProperty(value = "discount", required = true) float discount)
    {
        this.email = email;
        this.movie_id = movie_id;
        this.quantity = quantity;
        this.unit_price = unit_price;
        this.discount = discount;
    }

    @JsonProperty("email")
    public String getEmail()
    {
        return email;
    }
    @JsonProperty("movie_id")
    public String getMovie_id() {
        return movie_id;
    }
    @JsonProperty("quantity")
    public int getQuantity() {
        return quantity;
    }
    @JsonProperty("unit_price")
    public float getUnit_price() {
        return unit_price;
    }
    @JsonProperty("discount")
    public float getDiscount() {
        return discount;
    }
}
