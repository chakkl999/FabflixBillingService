package edu.uci.ics.chakkl.service.billing.resources;

import com.braintreepayments.http.HttpResponse;
import com.braintreepayments.http.exceptions.HttpException;
import com.braintreepayments.http.serializer.Json;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.*;
import edu.uci.ics.chakkl.service.billing.BillingService;
import edu.uci.ics.chakkl.service.billing.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.billing.models.*;
import edu.uci.ics.chakkl.service.billing.util.Header;
import edu.uci.ics.chakkl.service.billing.util.Result;
import edu.uci.ics.chakkl.service.billing.util.Util;
import javafx.util.Pair;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("order")
public class OrderEndpoint {
    private static final String clientId = "";
    private static final String clientSecret = "";
    //setup paypal envrionment
    public static PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
    //Create client for environment
    private static PayPalHttpClient client = new PayPalHttpClient(environment);

    @Path("place")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeOrder(@Context HttpHeaders headers, String jsonText)
    {
        Header header = new Header(headers);
        ObjectMapper mapper = new ObjectMapper();
        BaseRequestModel requestModel;
        OrderResponseModel responseModel = new OrderResponseModel(null, null);
        try {
            requestModel = mapper.readValue(jsonText, BaseRequestModel.class);
        } catch (JsonParseException e) {
            responseModel.setResult(Result.JSON_PARSE_ERROR);
            return responseModel.buildResponse(header);
        } catch (JsonMappingException e) {
            responseModel.setResult(Result.JSON_MAPPING_ERROR);
            return responseModel.buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        if(!header.getEmail().equals(requestModel.getEmail())) {
            responseModel.setResult(Result.ORDER_CREATION_FAILED);
            return responseModel.buildResponse(header);
        }
        CartItemInfo items[] = getCart(requestModel.getEmail());
        if(items == null) {
            responseModel.setResult(Result.CART_ITEM_DOES_NOT_EXIST);
            return responseModel.buildResponse(header);
        }
        Order order = createOrder(items);
        if(order == null){
            responseModel.setResult(Result.ORDER_CREATION_FAILED);
            return responseModel.buildResponse(header);
        }
        String approve_url = null;
        for(LinkDescription link : order.links()) {
            if(link.rel().equals("approve")) {
                approve_url = link.href();
                break;
            }
        }
        String token = order.id();
        insertSale(items, token);
        responseModel.setApprove_url(approve_url);
        responseModel.setToken(token);
        responseModel.setResult(Result.ORDER_PLACED_SUCCESSFUL);
        return responseModel.buildResponse(header);
    }

    @Path("complete")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeOrder(@Context HttpHeaders headers, @QueryParam("token") String token, @QueryParam("payer_id") String payer_id)
    {
        ServiceLogger.LOGGER.info("Completing order with token: " + token);
        Header header = new Header(headers);
        if(!tokenExist(token))
            return new BaseResponseModel(Result.TOKEN_NOT_FOUND).buildResponse(header);
        Order order = captureOrder(token);
        if(order == null)
            return new BaseResponseModel(Result.ORDER_CANNOT_BE_COMPLETE).buildResponse(header);
        String captureID = order.purchaseUnits().get(0).payments().captures().get(0).id();
        if(updateCaptureID(token, captureID)) {
            clearCart(token);
            return new BaseResponseModel(Result.ORDER_COMPLETED).buildResponse(header);
        }
        return new BaseResponseModel(Result.ORDER_CANNOT_BE_COMPLETE).buildResponse(header);
    }

    @Path("retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveBilling(@Context HttpHeaders headers, String jsonText)
    {
        ServiceLogger.LOGGER.info("Retrieving billing");
        Header header = new Header(headers);
        ObjectMapper mapper = new ObjectMapper();
        BaseRequestModel requestModel;
        BillingResponseModel responseModel = new BillingResponseModel(null);
        try {
            requestModel = mapper.readValue(jsonText, BaseRequestModel.class);
        } catch (JsonParseException e) {
            responseModel.setResult(Result.JSON_PARSE_ERROR);
            return responseModel.buildResponse(header);
        } catch (JsonMappingException e) {
            responseModel.setResult(Result.JSON_MAPPING_ERROR);
            return responseModel.buildResponse(header);
        } catch (Exception e) {
            System.out.println("first");
            System.out.println(e.getMessage());
            return Util.internal_server_error(header);
        }
        if(!header.getEmail().equals(requestModel.getEmail())) {
            responseModel.setResult(Result.ORDER_HISTORY_NOT_EXIST);
            return responseModel.buildResponse(header);
        }
        Map<String, BillingHistoryItemModel[]> transaction = getTransaction(requestModel.getEmail());
//        HttpResponse<Order> response = getOrder();
        ArrayList<TransactionModel> t = new ArrayList<>();
        transaction.forEach((k,v) -> {
            try {
                HttpResponse<Order> response = getOrder(k);
                t.add(new TransactionModel(response.result().purchaseUnits().get(0).payments().captures().get(0).id(), response.result().status(),
                                            new AmountModel(response.result().purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().grossAmount().value(), response.result().purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().grossAmount().currencyCode()),
                                            new TransactionFeeModel(response.result().purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().paypalFee().value(), response.result().purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().paypalFee().currencyCode()),
                                            response.result().createTime(), response.result().updateTime(), v));
//                System.out.println("Status: " + response.result().status());
//                System.out.println("Capture_id: " + response.result().purchaseUnits().get(0).payments().captures().get(0).id());
//                System.out.println("Created_time: " + response.result().createTime());
//                System.out.println("Update_time: " + response.result().updateTime());
//                System.out.println("Amount:" + new Json().serialize(response.result().purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().grossAmount()));
//                System.out.println("Fee: " + new Json().serialize(response.result().purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().paypalFee()));
//                System.out.println("Full response body:" + (new Json().serialize(getOrder(k.getKey()).result())));
            } catch (IOException e) {
                System.out.println("io");
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("someother");
                System.out.println(e.getMessage());
            }
        });
        TransactionModel temp[] = t.toArray(new TransactionModel[t.size()]);
        responseModel.setTransaction(temp);
        responseModel.setResult(Result.ORDER_RETRIEVED_SUCCESSFUL);
        return responseModel.buildResponse(header);
    }

    private Order createOrder(CartItemInfo items[])
    {
        float total = 0.00f;
        Order order = null;
        OrderRequest orderRequest = new OrderRequest();

        orderRequest.checkoutPaymentIntent("CAPTURE");

        ApplicationContext applicationContext = new ApplicationContext().returnUrl("http://localhost:3000/complete");

        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        for(CartItemInfo i : items) {
            total += (i.getQuantity() * (i.getUnit_price() * (1 - i.getDiscount())));
        }
        purchaseUnits.add(new PurchaseUnitRequest().amountWithBreakdown(new AmountWithBreakdown().currencyCode("USD").value(Double.toString(Math.round(total*100.0) / 100.0))));
        orderRequest.purchaseUnits(purchaseUnits);
        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);
        try {
            HttpResponse<Order> response = client.execute(request);
            order = response.result();
            return order;
        } catch (IOException ioe) {
            System.err.println("*******COULD NOT CREATE ORDER*******");
            if (ioe instanceof HttpException) {
                HttpException he = (HttpException) ioe;
                System.out.println(he.getMessage());
                he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
            } else {
                ServiceLogger.LOGGER.info("Something wrong with placing order: " + ioe.getMessage());
            }
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Something wrong with placing order: " + e.getMessage());
        }
        return null;
    }

    private Order captureOrder(String token)
    {
        Order order = null;
        OrdersCaptureRequest request = new OrdersCaptureRequest(token);
        try {
            HttpResponse<Order> response = client.execute(request);
            order = response.result();
        } catch (IOException ioe) {
            if (ioe instanceof HttpException) {
                // Something went wrong server-side
                HttpException he = (HttpException) ioe;
                System.out.println(he.getMessage());
                he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
            } else {
                ServiceLogger.LOGGER.info("Something wrong with placing order: " + ioe.getMessage());
            }
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Something wrong with placing order: " + e.getMessage());
        }
        return order;
    }

    private CartItemInfo[] getCart(String email)
    {
        String query =  "SELECT JSON_ARRAYAGG(JSON_OBJECT('email', c2.email, 'movie_id', c2.movie_id, 'quantity', c2.quantity, 'unit_price', c2.unit_price, 'discount', c2.discount)) as ItemInfo\n" +
                        "FROM (SELECT c.email, c.movie_id, c.quantity, mp.unit_price, mp.discount\n"+
                        "FROM cart as c\n"+
                        "INNER JOIN movie_price as mp on c.movie_id = mp.movie_id\n"+
                        "WHERE c.email LIKE ?) as c2";
        CartItemInfo items[] = null;
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                items = Util.mapping(rs.getString("ItemInfo"), CartItemInfo[].class);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Error in getting the cart.");
            return null;
        }
        return items;
    }

    private void insertSale(CartItemInfo items[], String token)
    {
        String querySale = "INSERT INTO sale(email, movie_id, quantity, sale_date) VALUES (?, ? ,? ,?)";
        String queryTransaction = "INSERT INTO transaction(sale_id, token) VALUES (?, ?)";
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(querySale, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = BillingService.getCon().prepareStatement(queryTransaction);
            ps2.setString(2, token);
            ResultSet rs;
            int last_inserted_id = 0;
            for(CartItemInfo item : items) {
                ps.setString(1, item.getEmail());
                ps.setString(2, item.getMovie_id());
                ps.setInt(3, item.getQuantity());
                ps.setDate(4, new Date(System.currentTimeMillis()));
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                if(rs.next())
                    last_inserted_id = rs.getInt(1);
                ps2.setInt(1, last_inserted_id);
                ps2.executeUpdate();
            }
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Error in inserting into sale: " + e.getMessage());
        }
    }

    private boolean tokenExist(String token)
    {
        String query = "SELECT * FROM transaction WHERE token LIKE ? LIMIT 1";
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return true;
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Error: " + e.getMessage());
        }
        return false;
    }

    private boolean updateCaptureID(String token, String captureID)
    {
        String query = "UPDATE transaction SET capture_id = ? WHERE token LIKE ?";
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, captureID);
            ps.setString(2, token);
            int row = ps.executeUpdate();
            if(row > 0)
                return true;
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Error: " + e.getMessage());
        }
        return false;
    }

    private void clearCart(String token)
    {
        String query = "DELETE FROM cart WHERE email LIKE (SELECT s.email FROM sale as s INNER JOIN transaction as t on s.sale_id = t.sale_id WHERE t.token LIKE ? LIMIT 1)";
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Error with clearing cart: " + e.getMessage());
        }
    }

    private HttpResponse<Order> getOrder(String orderId) throws IOException {
        OrdersGetRequest request = new OrdersGetRequest(orderId);
        HttpResponse<Order> response = client.execute(request);
        return response;
        // System.out.println(new JSONObject(new Json().serialize(response.result())).toString(4));
    }

    private Map<String, BillingHistoryItemModel[]> getTransaction(String email)
    {
        String query =  "SELECT s.email, s.movie_id, s.quantity, s.sale_date, m.unit_price, m.discount, t.token\n"+
                        "FROM sale as s\n"+
                        "INNER JOIN movie_price as m on s.movie_id = m.movie_id\n"+
                        "INNER JOIN transaction as t on s.sale_id = t.sale_id\n"+
                        "WHERE s.email LIKE ?";
        Map<String, ArrayList<BillingHistoryItemModel>> transaction = new HashMap<>();
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String key = rs.getString("token");
                if(!transaction.containsKey(key))
                    transaction.put(key, new ArrayList<>());
                transaction.get(key).add(new BillingHistoryItemModel(rs.getString("email"), rs.getString("movie_id"),
                                                                    rs.getInt("quantity"), rs.getFloat("unit_price"),
                                                                    rs.getFloat("discount"), rs.getDate("sale_date").toString()));
            }
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Error in getting transaction items: " + e.getMessage());
            return null;
        }
        Map<String, BillingHistoryItemModel[]> finalResult = new HashMap<>();
        transaction.forEach((k,v) -> {
            BillingHistoryItemModel temp[] = v.toArray(new BillingHistoryItemModel[v.size()]);
            finalResult.put(k, temp);
        });
        return finalResult;
    }
}
