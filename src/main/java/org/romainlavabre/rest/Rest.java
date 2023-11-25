package org.romainlavabre.rest;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public class Rest {
    public static RequestBuilder builder() {
        return new RequestBuilderImpl();
    }
}
