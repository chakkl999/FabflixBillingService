package edu.uci.ics.chakkl.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.chakkl.service.billing.util.Result;

public class ThumbnailResponseModel extends BaseResponseModel{
    @JsonProperty(value = "thumbnails", required = true)
    private MinimalMovieInfo thumbnails[];

    @JsonCreator
    public ThumbnailResponseModel(@JsonProperty(value = "thumbnails", required = true) MinimalMovieInfo thumbnails[])
    {
        if(thumbnails == null || thumbnails.length == 0) {
            this.setResult(Result.NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS);
            this.thumbnails = null;
        }
        else {
            this.setResult(Result.FOUND_MOVIE_WItH_SEARCH_PARAMETERS);
            this.thumbnails = thumbnails;
        }
    }

    @JsonProperty("thumbnails")
    public MinimalMovieInfo[] getThumbnails()
    {
        return thumbnails;
    }
}
