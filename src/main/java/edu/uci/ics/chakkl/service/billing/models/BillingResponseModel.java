package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BillingResponseModel extends BaseResponseModel {
    @JsonProperty(value = "transactions")
    private TransactionModel transaction[];

    @JsonCreator
    public BillingResponseModel(@JsonProperty(value = "transactions") TransactionModel transaction[])
    {
        this.transaction = transaction;
    }

    @JsonProperty("transactions")
    public TransactionModel[] getTransaction(){
        return transaction;
    }

    @JsonIgnore
    public void setTransaction(TransactionModel transaction[])
    {
        this.transaction = transaction;
    }
}
