package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionModel {
    @JsonProperty(value = "capture_id", required = true)
    private String capture_id;
    @JsonProperty(value = "state", required = true)
    private String state;
    @JsonProperty(value = "amount", required = true)
    private AmountModel amount;
    @JsonProperty(value = "transaction_fee", required = true)
    private TransactionFeeModel fee;
    @JsonProperty(value = "create_time", required = true)
    private String create_time;
    @JsonProperty(value = "update_time", required = true)
    private String update_time;
    @JsonProperty(value = "items")
    private BillingHistoryItemModel items[];

    @JsonCreator
    public TransactionModel(@JsonProperty(value = "capture_id", required = true) String capture_id,
                            @JsonProperty(value = "state", required = true) String state,
                            @JsonProperty(value = "amount", required = true) AmountModel amount,
                            @JsonProperty(value = "transaction_fee", required = true) TransactionFeeModel fee,
                            @JsonProperty(value = "create_time", required = true) String create_time,
                            @JsonProperty(value = "update_time", required = true) String update_time,
                            @JsonProperty(value = "items") BillingHistoryItemModel items[])
    {
        this.capture_id = capture_id;
        this.state = state;
        this.amount = amount;
        this.fee = fee;
        this.create_time = create_time;
        this.update_time = update_time;
        this.items = items;
    }

    @JsonProperty("capture_id")
    public String getCapture_id() {
        return capture_id;
    }
    @JsonProperty("state")
    public String getState() {
        return state;
    }
    @JsonProperty("amount")
    public AmountModel getAmount() {
        return amount;
    }
    @JsonProperty("transaction_fee")
    public TransactionFeeModel getFee() {
        return fee;
    }
    @JsonProperty("create_time")
    public String getCreate_time() {
        return create_time;
    }
    @JsonProperty("update_time")
    public String getUpdate_time() {
        return update_time;
    }
    @JsonProperty("items")
    public BillingHistoryItemModel[] getItems() {
        return items;
    }
}
