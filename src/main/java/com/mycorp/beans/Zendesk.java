package com.mycorp.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mycorp.exceptions.ZendeskException;
import com.mycorp.support.Ticket;
import com.mycorp.utils.ZendeskAsyncCompletionHandler;
import com.ning.http.client.*;
import com.ning.http.client.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

@Component
public class Zendesk implements Closeable {
    private static final String JSON = "application/json; charset=UTF-8";
    private static final Pattern RESTRICTED_PATTERN = Pattern.compile("%2B", Pattern.LITERAL);

    private final boolean closeClient;
    private AsyncHttpClient client = null;
    private final Realm realm;
    private final String url;
    private final String oauthToken;
    private String username;
    private String password;
    private final ObjectMapper mapper;
    private final Logger logger;
    private boolean closed = false;

    @Value("#{envPC['zendesk.token']} : ''")
    public String TOKEN_ZENDESK;

    @Value("#{envPC['zendesk.url']} : ''")
    public String URL_ZENDESK;

    @Value("#{envPC['zendesk.user']} : ''")
    public String ZENDESK_USER;


    // Modificamos el constructor para poder inyectar el bean con valores por defecto
    // El cliente no se inyecta, lo generamos en el constructor, lo que facilitara el testeo
    // Usamos el valor del token por defecto que se usaba en ZendeskService
    // En ZendeskService no se usaba el password asi que le asignamos oauth por defecto
    // Pasamos el token
    private Zendesk(@Value("#{envPC['zendesk.url']}") String url,
                    @Value("#{envPC['zendesk.user']}") String username,
                    @Value("#{envPC['zendesk.token']}") String oauthToken) {
        this.logger = LoggerFactory.getLogger(Zendesk.class);
        this.closeClient = client == null;
        this.oauthToken = oauthToken;
        this.username = username;
        this.password = oauthToken;
        this.client = client == null ? new AsyncHttpClient() : client;
        this.url = url.endsWith("/") ? url + "api/v2" : url + "/api/v2";
        if (username != null) {
            this.realm = new Realm.RealmBuilder()
                    .setScheme(Realm.AuthScheme.BASIC)
                    .setPrincipal(username)
                    .setPassword(password)
                    .setUsePreemptiveAuth(true)
                    .build();
        } else {
            if (password != null) {
                throw new IllegalStateException("Cannot specify token or password without specifying username");
            }
            this.realm = null;
        }
        this.mapper = createMapper();
    }

    public Ticket createTicket(Ticket ticket) {
        return complete(submit(req("POST", cnst("/tickets.json"),
                JSON, json(Collections.singletonMap("ticket", ticket))),
                new ZendeskAsyncCompletionHandler<Ticket>(Ticket.class, "ticket", mapper)));
    }

    private byte[] json(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ZendeskException(e.getMessage(), e);
        }
    }



    private Request req(String method, Uri template, String contentType, byte[] body) {
        RequestBuilder builder = new RequestBuilder(method);
        if (realm != null) {
            builder.setRealm(realm);
        } else {
            builder.addHeader("Authorization", "Bearer " + oauthToken);
        }
        builder.setUrl(RESTRICTED_PATTERN.matcher(template.toString()).replaceAll("+")); //replace out %2B with + due to API restriction
        builder.addHeader("Content-type", contentType);
        builder.setBody(body);
        return builder.build();
    }

    public static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private Uri cnst(String template) {
        return Uri.create(url + template);
    }

    private <T> ListenableFuture<T> submit(Request request, ZendeskAsyncCompletionHandler<T> handler) {
        if (logger.isDebugEnabled()) {
            if (request.getStringData() != null) {
                logger.debug("Request {} {}\n{}", request.getMethod(), request.getUrl(), request.getStringData());
            } else if (request.getByteData() != null) {
                logger.debug("Request {} {} {} {} bytes", request.getMethod(), request.getUrl(),
                        request.getHeaders().getFirstValue("Content-type"), request.getByteData().length);
            } else {
                logger.debug("Request {} {}", request.getMethod(), request.getUrl());
            }
        }
        return client.executeRequest(request, handler);
    }

    //////////////////////////////////////////////////////////////////////
    // Closeable interface methods
    //////////////////////////////////////////////////////////////////////

    public boolean isClosed() {
        return closed || client.isClosed();
    }

    public void close() {
        if (closeClient && !client.isClosed()) {
            client.close();
        }
        closed = true;
    }

    //////////////////////////////////////////////////////////////////////
    // Static helper methods
    //////////////////////////////////////////////////////////////////////

    private static <T> T complete(ListenableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ZendeskException(e.getMessage(), e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ZendeskException) {
                throw (ZendeskException) e.getCause();
            }
            throw new ZendeskException(e.getMessage(), e);
        }
    }

    // Eliminamos la clase Builder que ya no es necesaria
}