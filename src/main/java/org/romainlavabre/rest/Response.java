package org.romainlavabre.rest;

import java.util.List;
import java.util.Map;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public interface Response {

    int status();


    boolean isSuccess();


    boolean hasHeader( String header );


    String getHeader( String header );


    Map< String, String > getHeaders();


    Map< String, Object > getBodyAsMap();


    String getBodyAsString();


    List< Object > getBodyAsList();


    /**
     * @return the body of a request sent with RESPONSE_BYTES, else an empty array
     * @since 3.2.0
     */
    default byte[] getBodyAsBytes() {
        return new byte[ 0 ];
    }


    /**
     * @return why no answer came (connection refused, timeout, unknown host…), null when the server answered.
     * {@link #status()} is then 0
     * @since 3.2.0
     */
    default Throwable getError() {
        return null;
    }


    /**
     * @return true when the connection was never established (refused, connect timeout, unknown host): nothing was
     * sent, so the request can be replayed without risk of a second effect
     * @since 3.2.0
     */
    default boolean isUnreachable() {
        return false;
    }
}
