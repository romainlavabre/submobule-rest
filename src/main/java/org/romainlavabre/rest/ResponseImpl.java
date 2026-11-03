package org.romainlavabre.rest;

import kong.unirest.core.Header;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpConnectTimeoutException;
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
    protected HttpResponse< byte[] >   responseBytes;
    // Why no answer came; null when the server answered
    protected Throwable                error;


    @Override
    public int status() {
        if ( this.responseJson != null ) {
            return this.responseJson.getStatus();
        }

        if ( this.responseString != null ) {
            return this.responseString.getStatus();
        }

        if ( this.responseBytes != null ) {
            return this.responseBytes.getStatus();
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

        if ( this.responseBytes != null ) {
            return this.responseBytes.getStatus() >= 200
                    && this.responseBytes.getStatus() < 300;
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

        if ( this.responseBytes != null ) {
            return this.responseBytes.getHeaders().containsKey( header );
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

        if ( this.responseBytes != null ) {
            return this.responseBytes.getHeaders().get( header ).get( 0 );
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

        if ( responseBytes != null ) {
            final Map< String, String > headers = new HashMap<>();

            for ( final Header header : this.responseBytes.getHeaders().all() ) {
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


    @Override
    public byte[] getBodyAsBytes() {
        if ( this.responseBytes != null && this.responseBytes.getBody() != null ) {
            return this.responseBytes.getBody();
        }

        return new byte[ 0 ];
    }


    @Override
    public Throwable getError() {
        return this.error;
    }


    @Override
    public boolean isUnreachable() {
        for ( Throwable cause = this.error; cause != null; cause = cause.getCause() ) {
            if ( cause instanceof ConnectException
                    || cause instanceof HttpConnectTimeoutException
                    || cause instanceof UnknownHostException ) {
                return true;
            }

            if ( cause.getCause() == cause ) {
                break;
            }
        }

        return false;
    }


    protected Response supplyBytes( final HttpResponse< byte[] > response ) {
        this.responseBytes = response;

        return this;
    }


    protected Response supplyError( final Throwable error ) {
        this.responseJson = null;
        this.error        = error;

        return this;
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
