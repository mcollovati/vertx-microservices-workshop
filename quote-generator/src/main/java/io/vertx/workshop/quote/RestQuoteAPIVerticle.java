package io.vertx.workshop.quote;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This verticle exposes a HTTP endpoint to retrieve the current / last values of the maker data (quotes).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RestQuoteAPIVerticle extends AbstractVerticle {

    private Map<String, JsonObject> quotes = new HashMap<>();

    @Override
    public void start() throws Exception {
        vertx.eventBus().<JsonObject>consumer(GeneratorConfigVerticle.ADDRESS, message -> {
            // TODO Populate the `quotes` map with the received quote
            // Quotes are json objects you can retrieve from the message body
            // The map is structured as follows: name -> quote
            // ----
            JsonObject quote = message.body(); // 1
            quotes.put(quote.getString("name"), quote); // 2
            // ----
        });


        vertx.createHttpServer()
            .requestHandler(request -> {
                HttpServerResponse response = request.response()
                    .putHeader("content-type", "application/json");
                String company = request.getParam("name");
                if (company == null) {
                    String content = Json.encodePrettily(quotes);
                    response.end(content);
                } else {
                    JsonObject quote = quotes.get(company);
                    if (quote == null) {
                        response.setStatusCode(404).end();
                    } else {
                        response.end(quote.encodePrettily());
                    }
                }

                // ----

                // ----
            })
            .listen(8080, ar -> {
                if (ar.succeeded()) {
                    System.out.println("Server started");
                } else {
                    System.out.println("Cannot start the server: " + ar.cause());
                }
            });
    }
}
