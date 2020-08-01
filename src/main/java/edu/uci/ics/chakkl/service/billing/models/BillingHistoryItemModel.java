package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BillingHistoryItemModel extends CartItemInfo {
    @JsonProperty(value = "sale_date", required = true)
    private String sale_date;

    @JsonCreator
    public BillingHistoryItemModel(@JsonProperty(value = "email", required = true) String email,
                                    @JsonProperty(value = "movie_id", required = true) String movie_id,
                                    @JsonProperty(value = "quantity", required = true) int quantity,
                                    @JsonProperty(value = "unit_price", required = true) float unit_price,
                                    @JsonProperty(value = "discount", required = true) float discount,
                                    @JsonProperty(value = "sale_date", required = true) String sale_date)
    {
        super(email, movie_id, quantity, unit_price, discount);
        this.sale_date = sale_date;
    }

    @JsonProperty("sale_date")
    public String getSale_date(){
        return sale_date;
    }
}
