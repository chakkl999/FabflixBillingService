package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuantityRequestModel extends MovieIDRequestModel{
    @JsonProperty(value = "quantity", required = true)
    private int quantity;

    @JsonCreator
    public QuantityRequestModel(@JsonProperty(value = "email", required = true) String email,
                                @JsonProperty(value = "movie_id", required = true) String movie_id,
                                @JsonProperty(value = "quantity", required = true) int quantity)
    {
        super(email, movie_id);
        this.quantity = quantity;
    }

    @JsonProperty("quantity")
    public int getQuantity()
    {
        return quantity;
    }
}
