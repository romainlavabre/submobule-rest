package org.romainlavabre.rest.fix;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public class LocalDateTimeTypeAdapter extends TypeAdapter< LocalDateTime > {

    @Override
    public void write( final JsonWriter jsonWriter, final LocalDateTime localDate ) throws IOException {
        if ( localDate == null ) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value( localDate.toString() );
    }


    @Override
    public LocalDateTime read( final JsonReader jsonReader ) throws IOException {
        if ( jsonReader.peek() == JsonToken.NULL ) {
            jsonReader.nextNull();
            return null;
        }
        return ZonedDateTime.parse( jsonReader.nextString() ).toLocalDateTime();
    }
}
