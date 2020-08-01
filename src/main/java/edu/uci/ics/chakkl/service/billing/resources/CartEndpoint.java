package edu.uci.ics.chakkl.service.billing.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.chakkl.service.billing.BillingService;
import edu.uci.ics.chakkl.service.billing.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.billing.models.*;
import edu.uci.ics.chakkl.service.billing.util.Header;
import edu.uci.ics.chakkl.service.billing.util.Parameter;
import edu.uci.ics.chakkl.service.billing.util.Result;
import edu.uci.ics.chakkl.service.billing.util.Util;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

@Path("cart")
public class CartEndpoint {
    @Path("insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cartInsert(@Context HttpHeaders headers, String jsonText)
    {
        Header header = new Header(headers);
        ObjectMapper mapper = new ObjectMapper();
        QuantityRequestModel requestModel;
//        System.out.println(jsonText);
        try {
            requestModel = mapper.readValue(jsonText, QuantityRequestModel.class);
        } catch (JsonParseException e) {
            return new BaseResponseModel(Result.JSON_PARSE_ERROR).buildResponse(header);
        } catch (JsonMappingException e) {
            return new BaseResponseModel(Result.JSON_MAPPING_ERROR).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        if(!header.getEmail().equals(requestModel.getEmail()))
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        if(requestModel.getQuantity() <= 0)
            return new BaseResponseModel(Result.QUANTITY_INVALID).buildResponse(header);
        if(!Util.getPlevel(requestModel.getEmail(), 5))
            return new BaseResponseModel(Result.USER_NOT_FOUND).buildResponse(header);
        if(!checkMovieExist(requestModel.getMovie_id()))
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        try {
            PreparedStatement ps = Util.preparedStatement(insertCartQuery(), insertCartParameter(requestModel.getEmail(), requestModel.getMovie_id(), requestModel.getQuantity()));
            ps.executeUpdate();
        } catch (SQLException e) {
            return new BaseResponseModel(Result.DUPLICATE_INSERTION).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Insert success");
        return new BaseResponseModel(Result.CART_INSERTION_SUCCESSFUL).buildResponse(header);
    }

    @Path("update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCart(@Context HttpHeaders headers, String jsonText)
    {
        Header header = new Header(headers);
        ObjectMapper mapper = new ObjectMapper();
        QuantityRequestModel requestModel;
        ServiceLogger.LOGGER.info("Received update request.");
        try {
            requestModel = mapper.readValue(jsonText, QuantityRequestModel.class);
        } catch (JsonParseException e) {
            return new BaseResponseModel(Result.JSON_PARSE_ERROR).buildResponse(header);
        } catch (JsonMappingException e) {
            return new BaseResponseModel(Result.JSON_MAPPING_ERROR).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        if(!header.getEmail().equals(requestModel.getEmail()))
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        if(requestModel.getQuantity() <= 0)
            return new BaseResponseModel(Result.QUANTITY_INVALID).buildResponse(header);
        try {
            PreparedStatement ps = Util.preparedStatement(updateCartQuery(), updateCartParameter(requestModel.getQuantity(), requestModel.getEmail(), requestModel.getMovie_id()));
            int row = ps.executeUpdate();
            if(row < 1)
                return new BaseResponseModel(Result.CART_ITEM_DOES_NOT_EXIST).buildResponse(header);
        } catch (SQLException e) {
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Update successful.");
        return new BaseResponseModel(Result.CART_UPDATE_SUCCESSFUL).buildResponse(header);
    }

    @Path("delete")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCart(@Context HttpHeaders headers, String jsonText)
    {
        Header header = new Header(headers);
        ObjectMapper mapper = new ObjectMapper();
        MovieIDRequestModel requestModel;
        try {
            requestModel = mapper.readValue(jsonText, MovieIDRequestModel.class);
        } catch (JsonParseException e) {
            return new BaseResponseModel(Result.JSON_PARSE_ERROR).buildResponse(header);
        } catch (JsonMappingException e) {
            return new BaseResponseModel(Result.JSON_MAPPING_ERROR).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        if(!header.getEmail().equals(requestModel.getEmail()))
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        try {
            PreparedStatement ps = Util.preparedStatement(deleteCartQuery(), deleteCartParameter(requestModel.getEmail(), requestModel.getMovie_id()));
            int row = ps.executeUpdate();
            if(row < 1)
                return new BaseResponseModel(Result.CART_ITEM_DOES_NOT_EXIST).buildResponse(header);
        } catch (SQLException e) {
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        return new BaseResponseModel(Result.CART_ITEM_DELETE_SUCCESSFUL).buildResponse(header);
    }

    @Path("retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveCart(@Context HttpHeaders headers, String jsonText)
    {
        Header header = new Header(headers);
        ObjectMapper mapper = new ObjectMapper();
        BaseRequestModel requestModel;
        RetrieveResponseModel responseModel = new RetrieveResponseModel(null);
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
            responseModel.setResult(Result.CART_OPERATION_FAILED);
            return responseModel.buildResponse(header);
        }
        CartItemInfo info[] = null;
        ArrayList<ItemModel> items = new ArrayList<>();
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(retrieveCartQuery());
            ps.setString(1, requestModel.getEmail());
            ServiceLogger.LOGGER.info(ps.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
//                ServiceLogger.LOGGER.info(rs.getString("ItemInfo"));
                info = Util.mapping(rs.getString("ItemInfo"), CartItemInfo[].class);
                if(info == null) {
                    ServiceLogger.LOGGER.info("Can't find cart for user.");
                    responseModel.setResult(Result.CART_ITEM_DOES_NOT_EXIST);
                    return responseModel.buildResponse(header);
                }
                ArrayList<String> movie_ids = new ArrayList<>();
//                ServiceLogger.LOGGER.info("Passed mapping.");
                for(CartItemInfo i : info)
                    movie_ids.add(i.getMovie_id());
//                ServiceLogger.LOGGER.info("Add to arraylist.");
                HashMap<String, MinimalMovieInfo> movieInfo = Util.getThumbnail(movie_ids);
//                ServiceLogger.LOGGER.info("Hashmap.");
                for(CartItemInfo i : info) {
                    MinimalMovieInfo temp = movieInfo.get(i.getMovie_id());
                    items.add(new ItemModel(i.getEmail(), i.getUnit_price(), i.getDiscount(), i.getQuantity(), i.getMovie_id(), temp.getTitle(), temp.getBackdrop_path(), temp.getPoster_path()));
                }
//                ServiceLogger.LOGGER.info("Add to map");
//                System.out.println(movieInfo.toString());
            }
            else {
                ServiceLogger.LOGGER.info("Can't find cart for user.");
                responseModel.setResult(Result.CART_ITEM_DOES_NOT_EXIST);
                return responseModel.buildResponse(header);
            }
        } catch (SQLException e) {
            responseModel.setResult(Result.CART_OPERATION_FAILED);
            return responseModel.buildResponse(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info(e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Retrieve successful.");
        ItemModel[] temp = items.toArray(new ItemModel[items.size()]);
//        for(ItemModel t : temp) {
//            ServiceLogger.LOGGER.info(t.toString());
//        }
        responseModel.setItem(temp);
        responseModel.setResult(Result.CART_RETRIEVE_SUCCESSFUL);
        return responseModel.buildResponse(header);
    }

    @Path("clear")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearCart(@Context HttpHeaders headers, String jsonText)
    {
        Header header = new Header(headers);
        ObjectMapper mapper = new ObjectMapper();
        BaseRequestModel requestModel;
        try {
            requestModel = mapper.readValue(jsonText, BaseRequestModel.class);
        } catch (JsonParseException e) {
            return new BaseResponseModel(Result.JSON_PARSE_ERROR).buildResponse(header);
        } catch (JsonMappingException e) {
            return new BaseResponseModel(Result.JSON_MAPPING_ERROR).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        if(!header.getEmail().equals(requestModel.getEmail()))
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(clearCartQuery());
            ps.setString(1, requestModel.getEmail());
            int row = ps.executeUpdate();
            if(row < 1)
                return new BaseResponseModel(Result.CART_ITEM_DOES_NOT_EXIST).buildResponse(header);
        } catch (SQLException e) {
            return new BaseResponseModel(Result.CART_OPERATION_FAILED).buildResponse(header);
        } catch (Exception e) {
            return Util.internal_server_error(header);
        }
        return new BaseResponseModel(Result.CART_CLEAR_SUCCESSFUL).buildResponse(header);
    }

    private String insertCartQuery()
    {
        return "INSERT INTO cart(email, movie_id, quantity) VALUES(?, ?, ?)";
    }

    private String updateCartQuery()
    {
        return "UPDATE cart SET quantity = ? WHERE email LIKE ? AND movie_id LIKE ?";
    }

    private String deleteCartQuery()
    {
        return "DELETE FROM cart WHERE email LIKE ? AND movie_id LIKE ?";
    }

    private String retrieveCartQuery()
    {
        String query =  "SELECT JSON_ARRAYAGG(JSON_OBJECT('email', c2.email, 'movie_id', c2.movie_id, 'quantity', c2.quantity, 'unit_price', c2.unit_price, 'discount', c2.discount)) as ItemInfo\n" +
                        "FROM (SELECT c.email, c.movie_id, c.quantity, mp.unit_price, mp.discount\n"+
                        "FROM cart as c\n"+
                        "INNER JOIN movie_price as mp on c.movie_id = mp.movie_id\n"+
                        "WHERE c.email LIKE ?) as c2";
        return query;
    }

    private String clearCartQuery()
    {
        return "DELETE FROM cart WHERE email LIKE ?";
    }

    private ArrayList<Parameter> insertCartParameter(String email, String movie_id, int quantity)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        p.add(Parameter.createParameter(Types.VARCHAR, email));
        p.add(Parameter.createParameter(Types.VARCHAR, movie_id));
        p.add(Parameter.createParameter(Types.INTEGER, quantity));
        return p;
    }

    private ArrayList<Parameter> updateCartParameter(int quantity, String email, String movie_id)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        p.add(Parameter.createParameter(Types.INTEGER, quantity));
        p.add(Parameter.createParameter(Types.VARCHAR, email));
        p.add(Parameter.createParameter(Types.VARCHAR, movie_id));
        return p;
    }

    private ArrayList<Parameter> deleteCartParameter(String email, String movie_id)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        p.add(Parameter.createParameter(Types.VARCHAR, email));
        p.add(Parameter.createParameter(Types.VARCHAR, movie_id));
        return p;
    }

    private boolean checkMovieExist(String movie_id)
    {
        String query = "SELECT movie_id FROM movie_price WHERE movie_id LIKE ?";
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, movie_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean checkItemExist(String email, String movie_id)
    {
        String query = "SELECT * FROM cart WHERE email LIKE ? AND movie_id like ?";
        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, movie_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
