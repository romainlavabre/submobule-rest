package org.romainlavabre.rest;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public interface RequestBuilder {
    String JSON             = "application/json";
    String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    String FORM_DATA        = "multipart/form-data";
    String PLAIN_TEXT       = "text/plain";
    String GET              = "GET";
    String POST             = "POST";
    String PUT              = "PUT";
    String PATCH            = "PATCH";
    String DELETE           = "DELETE";
    String OPTIONS          = "OPTIONS";
    String RESPONSE_JSON    = "JSON";
    String RESPONSE_HTML    = "HTML";
    String RESPONSE_XML     = "XML";
    String RESPONSE_TEXT    = "TEXT";
    String RESPONSE_BYTES   = "BYTES";


    @Deprecated
    RequestBuilder init( String method, String url );


    RequestBuilder request( String method, String url );


    RequestBuilder get( String url );


    RequestBuilder post( String url );


    RequestBuilder put( String url );


    RequestBuilder patch( String url );


    RequestBuilder delete( String url );


    RequestBuilder options( String url );


    RequestBuilder withBasicAuth( String username, String password );


    RequestBuilder withBearerToken( String token );


    RequestBuilder withXApiKey( String apiKey );


    RequestBuilder routeParam( String param, String value );


    RequestBuilder queryString( String key, String value );


    RequestBuilder field( String key, String value );


    RequestBuilder field( String key, File value );


    RequestBuilder field( String key, Collection value );


    RequestBuilder jsonBody( Map< String, Object > json );


    RequestBuilder jsonBody( List< Object > json );


    RequestBuilder plainTextBody( String plainText );


    /**
     * A raw body, sent as is.
     *
     * @since 3.2.0
     */
    default RequestBuilder body( byte[] content, String contentType ) {
        throw new UnsupportedOperationException( "body( byte[], String ) is not supported by " + getClass().getName() );
    }


    /**
     * A raw body read while it is sent, never held in memory.
     *
     * @since 3.2.0
     */
    default RequestBuilder body( InputStream content, String contentType ) {
        throw new UnsupportedOperationException( "body( InputStream, String ) is not supported by " + getClass().getName() );
    }


    /**
     * The longest this request may take once sent, its answer included. Overrides for this request only the
     * request timeout of the global Unirest configuration, which applies when this is not called. The connect
     * timeout stays the global one.
     *
     * @since 3.2.0
     */
    default RequestBuilder timeout( Duration timeout ) {
        throw new UnsupportedOperationException( "timeout( Duration ) is not supported by " + getClass().getName() );
    }


    RequestBuilder inContentType( String contentType );


    RequestBuilder addHeader( String header, String value );


    RequestBuilder addHeader( String header, Integer value );


    /**
     * @param media RESPONSE_JSON, RESPONSE_BYTES, or any other value for the body as a string
     * @return Response in choices media
     */
    Response buildAndSend( String media );


    /**
     * @return Response in json media
     */
    Response buildAndSend();
}
