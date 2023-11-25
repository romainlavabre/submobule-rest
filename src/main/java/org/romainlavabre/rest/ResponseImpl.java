package org.romainlavabre.rest;

import kong.unirest.Header;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public class ResponseImpl implements Response {

    protected HttpResponse< JsonNode > responseJson;
    protected HttpResponse< String >   responseString;


    @Override
    public int status() {
        if ( this.responseJson != null ) {
            return this.responseJson.getStatus();
        }

        if ( this.responseString != null ) {
            return this.responseString.getStatus();
        }

        return 0;
    }


    @Override
    public boolean isSuccess() {
        if ( this.responseJson != null ) {
            return this.responseJson.getStatus() >= 200
                    && this.responseJson.getStatus() < 300;
        }

        if ( this.responseString != null ) {
            return this.responseString.getStatus() >= 200
                    && this.responseString.getStatus() < 300;
        }

        return false;
    }


    @Override
    public boolean hasHeader( final String header ) {
        if ( this.responseJson != null ) {
            return this.responseJson.getHeaders().containsKey( header );
        }

        if ( this.responseString != null ) {
            return this.responseString.getHeaders().containsKey( header );
        }

        return false;
    }


    @Override
    public String getHeader( final String header ) {
        if ( this.responseJson != null ) {
            return this.responseJson.getHeaders().get( header ).get( 0 );
        }

        if ( this.responseString != null ) {
            return this.responseString.getHeaders().get( header ).get( 0 );
        }

        return null;
    }


    @Override
    public Map< String, String > getHeaders() {
        if ( responseJson != null ) {
            final Map< String, String > headers = new HashMap<>();

            for ( final Header header : this.responseJson.getHeaders().all() ) {
                headers.put( header.getName(), header.getValue() );
            }

            return headers;
        }

        if ( responseString != null ) {
            final Map< String, String > headers = new HashMap<>();

            for ( final Header header : this.responseString.getHeaders().all() ) {
                headers.put( header.getName(), header.getValue() );
            }

            return headers;
        }

        return new HashMap<>();
    }


    @Override
    public Map< String, Object > getBodyAsMap() {
        if ( this.responseJson != null ) {
            if ( responseJson.getBody() == null ) {
                return new HashMap<>();
            }

            return this.responseJson.getBody().getObject().toMap();
        }

        return new HashMap<>();
    }


    @Override
    public List< Object > getBodyAsList() {
        if ( this.responseJson != null ) {
            if ( this.responseJson.getBody() == null ) {
                return new ArrayList<>();
            }
            
            return this.responseJson.getBody().getArray().toList();
        }

        return new ArrayList<>();
    }


    @Override
    public String getBodyAsString() {
        if ( this.responseString != null ) {
            return this.responseString.getBody();
        }

        return "";
    }


    protected Response supplyString( final HttpResponse< String > response ) {
        this.responseString = response;

        return this;
    }


    protected Response supplyJson( final HttpResponse< JsonNode > response ) {
        this.responseJson = response;

        return this;
    }


    protected Response supply() {
        this.responseJson = null;

        return this;
    }
}
