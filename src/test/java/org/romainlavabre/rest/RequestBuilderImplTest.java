package org.romainlavabre.rest;

import com.sun.net.httpserver.HttpServer;
import kong.unirest.core.Unirest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RequestBuilderImplTest {

    private HttpServer                server;
    private AtomicReference< byte[] > received;
    private AtomicReference< String > receivedContentType;
    private Integer                   globalRequestTimeout;


    @Before
    public void startTheServer() throws IOException {
        globalRequestTimeout = Unirest.config().getRequestTimeout();
        received             = new AtomicReference<>();
        receivedContentType  = new AtomicReference<>();

        server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
        server.createContext( "/json", exchange -> answer( exchange.getResponseHeaders(), exchange, 200, "{\"id\":\"abc\"}" ) );
        server.createContext( "/slow", exchange -> {
            sleep( 1500 );
            answer( exchange.getResponseHeaders(), exchange, 200, "{}" );
        } );
        server.createContext( "/echo", exchange -> {
            received.set( exchange.getRequestBody().readAllBytes() );
            receivedContentType.set( exchange.getRequestHeaders().getFirst( "Content-Type" ) );
            answer( exchange.getResponseHeaders(), exchange, 201, "{}" );
        } );
        server.createContext( "/bytes", exchange -> {
            byte[] content = { 0, 1, 2, ( byte ) 255 };
            exchange.getResponseHeaders().add( "X-Subject-Token", "token" );
            exchange.sendResponseHeaders( 200, content.length );
            try ( OutputStream output = exchange.getResponseBody() ) {
                output.write( content );
            }
        } );
        server.start();
    }


    @After
    public void stopTheServer() {
        server.stop( 0 );
        Unirest.config().requestTimeout( globalRequestTimeout );
    }


    // What the applications already do must answer as in 3.1.0
    @Test
    public void a_json_request_answers_as_before() {
        Response response = Rest.builder().get( url( "/json" ) ).buildAndSend();

        assertEquals( 200, response.status() );
        assertTrue( response.isSuccess() );
        assertEquals( "abc", response.getBodyAsMap().get( "id" ) );
        assertNull( response.getError() );
        assertFalse( response.isUnreachable() );
    }


    // MyPilotClientImpl (fairfair) reads a network error as status 0
    @Test
    public void a_refused_connection_still_answers_status_0_and_now_tells_why() throws IOException {
        Response response = Rest.builder().get( "http://127.0.0.1:" + aClosedPort() + "/json" ).buildAndSend();

        assertEquals( 0, response.status() );
        assertFalse( response.isSuccess() );
        assertTrue( response.getBodyAsMap().isEmpty() );
        assertNotNull( response.getError() );
        assertTrue( response.isUnreachable() );
    }


    @Test
    public void the_timeout_of_a_request_overrides_the_global_one() {
        Unirest.config().requestTimeout( 60_000 );

        Response response = Rest.builder().get( url( "/slow" ) ).timeout( Duration.ofMillis( 300 ) ).buildAndSend();

        assertEquals( 0, response.status() );
        assertNotNull( response.getError() );
        // Sent: the server may have acted, it must not be replayed blindly
        assertFalse( response.isUnreachable() );
    }


    // service-message-broker, service-statistic and service-call-center (fairfair) set it globally
    @Test
    public void without_a_timeout_of_its_own_a_request_keeps_the_global_one() {
        Unirest.config().requestTimeout( 300 );

        Response response = Rest.builder().get( url( "/slow" ) ).buildAndSend();

        assertEquals( 0, response.status() );
        assertNotNull( response.getError() );
    }


    @Test
    public void the_timeout_of_a_request_does_not_leak_to_the_next_one() {
        Unirest.config().requestTimeout( 60_000 );
        RequestBuilder builder = Rest.builder();

        assertEquals( 0, builder.get( url( "/slow" ) ).timeout( Duration.ofMillis( 300 ) ).buildAndSend().status() );
        assertEquals( 200, builder.get( url( "/slow" ) ).buildAndSend().status() );
    }


    @Test
    public void a_raw_body_is_sent_as_is() {
        byte[] content = "a plan".getBytes( StandardCharsets.UTF_8 );

        Response response = Rest.builder().put( url( "/echo" ) ).body( content, "image/jpeg" ).buildAndSend();

        assertEquals( 201, response.status() );
        assertArrayEquals( content, received.get() );
        assertEquals( "image/jpeg", receivedContentType.get() );
    }


    @Test
    public void a_stream_body_is_sent_as_is() {
        byte[] content = "a plan, streamed".getBytes( StandardCharsets.UTF_8 );

        Response response = Rest.builder().put( url( "/echo" ) ).body( new ByteArrayInputStream( content ), "application/pdf" ).buildAndSend();

        assertEquals( 201, response.status() );
        assertArrayEquals( content, received.get() );
        assertEquals( "application/pdf", receivedContentType.get() );
    }


    @Test
    public void a_json_list_body_is_sent() {
        Rest.builder().patch( url( "/echo" ) ).jsonBody( List.of( Map.of( "op", "replace" ) ) ).buildAndSend();

        assertEquals( "[{\"op\":\"replace\"}]", new String( received.get(), StandardCharsets.UTF_8 ) );
        assertEquals( "application/json", receivedContentType.get() );
    }


    // Hydra and the captcha providers read a form
    @Test
    public void the_fields_are_sent_as_a_url_encoded_form() {
        Rest.builder().post( url( "/echo" ) ).withBasicAuth( "client", "secret" ).field( "grant_type", "client_credentials" ).field( "response", "a b&c" ).buildAndSend();

        assertTrue( receivedContentType.get(), receivedContentType.get().startsWith( RequestBuilder.FORM_URL_ENCODED ) );
        assertEquals( "grant_type=client_credentials&response=a+b%26c", new String( received.get(), StandardCharsets.UTF_8 ) );
    }


    @Test
    public void a_body_is_read_as_bytes_with_its_headers() {
        Response response = Rest.builder().get( url( "/bytes" ) ).buildAndSend( RequestBuilder.RESPONSE_BYTES );

        assertEquals( 200, response.status() );
        assertTrue( response.isSuccess() );
        assertArrayEquals( new byte[]{ 0, 1, 2, ( byte ) 255 }, response.getBodyAsBytes() );
        assertTrue( response.hasHeader( "X-Subject-Token" ) );
        assertEquals( "token", response.getHeader( "X-Subject-Token" ) );
    }


    @Test
    public void a_body_is_read_as_text() {
        Response response = Rest.builder().get( url( "/json" ) ).buildAndSend( RequestBuilder.RESPONSE_TEXT );

        assertEquals( "{\"id\":\"abc\"}", response.getBodyAsString() );
        assertEquals( 0, response.getBodyAsBytes().length );
    }


    private String url( String path ) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }


    private static int aClosedPort() throws IOException {
        try ( ServerSocket socket = new ServerSocket( 0 ) ) {
            return socket.getLocalPort();
        }
    }


    private static void answer( com.sun.net.httpserver.Headers headers, com.sun.net.httpserver.HttpExchange exchange, int status, String body ) throws IOException {
        byte[] content = body.getBytes( StandardCharsets.UTF_8 );
        headers.add( "Content-Type", "application/json" );
        exchange.sendResponseHeaders( status, content.length );

        try ( OutputStream output = exchange.getResponseBody() ) {
            output.write( content );
        }
    }


    private static void sleep( long millis ) {
        try {
            Thread.sleep( millis );
        } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
        }
    }
}
