package edu.uci.ics.chakkl.service.billing.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.chakkl.service.billing.BillingService;
import edu.uci.ics.chakkl.service.billing.configs.IdmConfigs;
import edu.uci.ics.chakkl.service.billing.configs.MoviesConfigs;
import edu.uci.ics.chakkl.service.billing.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.billing.models.*;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Util {
    public static PreparedStatement preparedStatement(String query, ArrayList<Parameter> parameter) throws SQLException {
        ServiceLogger.LOGGER.info("Preparing statement.");
        PreparedStatement ps = BillingService.getCon().prepareStatement(query);
        int index = 1;
        for(Parameter p: parameter)
            ps.setObject(index++, p.getObject(), p.getType());
        ServiceLogger.LOGGER.info("Finished preparing statement.");
        ServiceLogger.LOGGER.info(ps.toString());
        return ps;
    }

    public static <T> T mapping(String jsonText, Class<T> className)
    {
        if(jsonText == null) {
            ServiceLogger.LOGGER.info("Nothing to map.");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();

        ServiceLogger.LOGGER.info("Mapping object: " + className.getName());

        try {
            return mapper.readValue(jsonText, className);
        } catch (IOException e) {
            ServiceLogger.LOGGER.info("Mapping Object Failed: " + e.getMessage());
            return null;
        }
    }

    public static Response internal_server_error(Header header)
    {
        Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        header.setHeader(builder);
        return builder.build();
    }

    public static boolean getPlevel(String email, int plevel)
    {
        PlevelRequestModel requestModel = new PlevelRequestModel(email, plevel);

        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);


        ServiceLogger.LOGGER.info("Building WebTarget...");
        IdmConfigs temp = BillingService.getIdmConfigs();
        WebTarget webTarget = client.target(temp.getScheme()+temp.getHostName()+":"+temp.getPort()+temp.getPath()).path(temp.getPrivilegePath());
        ServiceLogger.LOGGER.info("Sending to path: " + temp.getPath() + temp.getPrivilegePath());

        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);


        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");
        PlevelResponseModel responseModel = Util.mapping(response.readEntity(String.class), PlevelResponseModel.class);
        if(responseModel == null)
            return false;
        if(responseModel.getResultCode() == 140)
            return true;
        return false;
    }

    public static HashMap<String, MinimalMovieInfo> getThumbnail(ArrayList<String> movie_ids)
    {
        String[] temparr = movie_ids.toArray(new String[movie_ids.size()]);
        ThumbnailRequestModel requestModel = new ThumbnailRequestModel(temparr);

        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);


        ServiceLogger.LOGGER.info("Building WebTarget...");
        MoviesConfigs temp = BillingService.getMoviesConfigs();
        WebTarget webTarget = client.target(temp.getScheme()+temp.getHostName()+":"+temp.getPort()+temp.getPath()).path(temp.getThumbnailPath());
        ServiceLogger.LOGGER.info("Sending to path: " + temp.getPath() + temp.getThumbnailPath());

        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);


        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");
        ThumbnailResponseModel responseModel = Util.mapping(response.readEntity(String.class), ThumbnailResponseModel.class);
        MinimalMovieInfo movieInfo[] = responseModel.getThumbnails();
        HashMap<String, MinimalMovieInfo> mmi = new HashMap<>();
        for(MinimalMovieInfo info : movieInfo)
            mmi.put(info.getMovie_id(), info);
        return mmi;
    }
}
