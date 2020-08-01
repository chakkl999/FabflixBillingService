package edu.uci.ics.chakkl.service.billing.util;

import javax.ws.rs.core.Response;

public enum Result {
    FOUND_MOVIE_WItH_SEARCH_PARAMETERS (210, "Found movie(s) with search parameters.", Response.Status.OK),
    NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS (211, "No movies found with search parameters.", Response.Status.OK),
    FOUND_PEOPLE_WITH_SEARCH_PARAMETERS (212, "Found people with search parameters.", Response.Status.OK),
    NO_PEOPLE_FOUND_WITH_SEARCH_PARAMETERS (213, "No people found with search parameters.", Response.Status.OK),
    JSON_PARSE_ERROR (-3, "JSON Parse Exception.", Response.Status.BAD_REQUEST),
    JSON_MAPPING_ERROR (-2, "JSON Mapping Exception.", Response.Status.BAD_REQUEST),
    USER_NOT_FOUND (14, "User not found.", Response.Status.OK),
    QUANTITY_INVALID (33, "Quantity has invalid value.", Response.Status.OK),
    DUPLICATE_INSERTION (311, "Duplicate insertion.", Response.Status.OK),
    CART_ITEM_DOES_NOT_EXIST (312, "Shopping cart item does not exist.", Response.Status.OK),
    ORDER_HISTORY_NOT_EXIST (313, "Order history does not exist.", Response.Status.OK),
    ORDER_CREATION_FAILED (342, "Order creation failed.", Response.Status.OK),
    CART_INSERTION_SUCCESSFUL (3100, "Shopping cart item inserted successfully.", Response.Status.OK),
    CART_UPDATE_SUCCESSFUL (3110, "Shopping cart item updated successfully.", Response.Status.OK),
    CART_ITEM_DELETE_SUCCESSFUL (3120, "Shopping cart item deleted successfully.", Response.Status.OK),
    CART_RETRIEVE_SUCCESSFUL (3130, "Shopping cart retrieved successfully.", Response.Status.OK),
    CART_CLEAR_SUCCESSFUL (3140, "Shopping cart cleared successfully.", Response.Status.OK),
    CART_OPERATION_FAILED (3150, "Shopping cart operation failed.", Response.Status.OK),
    ORDER_PLACED_SUCCESSFUL (3400, "Order placed successfully.", Response.Status.OK),
    ORDER_RETRIEVED_SUCCESSFUL (3410, "Orders retrieved successfully.", Response.Status.OK),
    ORDER_COMPLETED (3420, "Order is completed successfully.", Response.Status.OK),
    TOKEN_NOT_FOUND (3421, "Token not found.", Response.Status.OK),
    ORDER_CANNOT_BE_COMPLETE (3422, "Order can not be completed.", Response.Status.OK);

    private final int resultCode;
    private final String message;
    private final Response.Status httpCode;

    Result(int resultCode, String message, Response.Status httpCode)
    {
        this.resultCode = resultCode;
        this.message = message;
        this.httpCode = httpCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getMessage() {
        return message;
    }

    public Response.Status getHttpCode() {
        return httpCode;
    }
}
