package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RetrieveResponseModel extends BaseResponseModel {
    @JsonProperty(value = "items")
    private ItemModel items[] = null;

    public RetrieveResponseModel(){};

    @JsonCreator
    public RetrieveResponseModel(@JsonProperty(value = "items") ItemModel items[])
    {
        this.items = items;
    }

    @JsonIgnore
    public void setItem(ItemModel items[])
    {
        this.items = items;
    }

    @JsonProperty("items")
    public ItemModel[] getItems()
    {
        return items;
    }
}
