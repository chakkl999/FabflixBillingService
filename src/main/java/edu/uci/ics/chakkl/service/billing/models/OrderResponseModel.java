package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderResponseModel extends BaseResponseModel{
    @JsonProperty(value = "approve_url")
    private String approve_url;
    @JsonProperty(value = "token")
    private String token;

    @JsonCreator
    public OrderResponseModel(@JsonProperty(value = "approve_url") String approve_url,
                              @JsonProperty(value = "token") String token)
    {
        this.approve_url = approve_url;
        this.token = token;
    }

    @JsonProperty("approve_url")
    public String getApprove_url(){
        return approve_url;
    }
    @JsonProperty("token")
    public String getToken(){
        return token;
    }

    @JsonIgnore
    public void setApprove_url(String approve_url) {
        this.approve_url = approve_url;
    }

    @JsonIgnore
    public void setToken(String token){
        this.token = token;
    }
}
