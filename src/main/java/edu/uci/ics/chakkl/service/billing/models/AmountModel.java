package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AmountModel {
    @JsonProperty(value = "total", required = true)
    private String total;
    @JsonProperty(value = "currency", required = true)
    private String currency;

    @JsonCreator
    public AmountModel(@JsonProperty(value = "total", required = true) String total,
                       @JsonProperty(value = "currency", required = true) String currency)
    {
        this.total = total;
        this.currency = currency;
    }

    @JsonProperty("total")
    public String getTotal(){
        return total;
    }
    @JsonProperty("currency")
    public String getCurrency(){
        return currency;
    }
}
