package org.romainlavabre.rest.fix;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public class ZonedDateTimeTypeAdapter extends TypeAdapter< ZonedDateTime > {

    @Override
    public void write( final JsonWriter jsonWriter, final ZonedDateTime zonedDateTime ) throws IOException {
        if ( zonedDateTime == null ) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value( zonedDateTime.toString() );
    }


    @Override
    public ZonedDateTime read( final JsonReader jsonReader ) throws IOException {
        if ( jsonReader.peek() == JsonToken.NULL ) {
            jsonReader.nextNull();
            return null;
        }
        return ZonedDateTime.parse( jsonReader.nextString() );
    }
}
