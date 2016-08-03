package io.vertx.workshop.trader.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A compulsive trader...
 */
public class JavaCompulsiveTraderVerticle extends MicroServiceVerticle {


    @Override
    public void start(Future<Void> future) {
        super.start();

        String company = TraderUtils.pickACompany();
        int numberOfShares = TraderUtils.pickANumber();
        System.out.println("Java compulsive trader configured for company " + company
            + " and shares: " + numberOfShares);

        // We need to retrieve two services, create two futures object that
        // will get the services
        Future<MessageConsumer<JsonObject>> marketFuture = Future.future();
        Future<PortfolioService> portfolioFuture = Future.future();

        // Retrieve the services, use the "special" completed to assign the future
        ////MessageSource.getConsumer(discovery, new JsonObject().put("name", "market-data"),
        ////    marketFuture.completer());
        tryGetMarketConsumer(marketFuture);

        ////EventBusService.getProxy(discovery, PortfolioService.class, portfolioFuture.completer());
        tryGetPortfolioService(portfolioFuture);

        // When done (both services retrieved), execute the handler
        CompositeFuture.all(marketFuture, portfolioFuture).setHandler(ar -> {
            if (ar.failed()) {
                future.fail("One of the required service cannot be retrieved: " + ar.cause());
            } else {
                // Our services:
                PortfolioService portfolio = portfolioFuture.result();
                MessageConsumer<JsonObject> marketConsumer = marketFuture.result();

                // Listen the market...
                marketConsumer.handler(message -> {
                    JsonObject quote = message.body();
                    TraderUtils.dumbTradingLogic(company, numberOfShares, portfolio, quote);
                });

                future.complete();
            }
        });

    }


    private void tryGetMarketConsumer(Future<MessageConsumer<JsonObject>> marketFuture) {
        AtomicInteger retry = new AtomicInteger(5);
        MessageSource.<JsonObject>getConsumer(discovery, new JsonObject().put("name", "market-data"), ar -> {
            if (ar.failed()) {
                if (retry.decrementAndGet() < 0) {
                    marketFuture.fail(ar.cause());
                } else {
                    vertx.setTimer(1000, l -> {
                        vertx.cancelTimer(l);
                        tryGetMarketConsumer(marketFuture);
                    });
                }
            } else {
                marketFuture.complete(ar.result());
            }
        });
    }

    private void tryGetPortfolioService(Future<PortfolioService> portfolioFuture) {
        AtomicInteger retry = new AtomicInteger(5);
        EventBusService.getProxy(discovery, PortfolioService.class, ar -> {
            if (ar.failed()) {
                if (retry.decrementAndGet() < 0) {
                    portfolioFuture.fail(ar.cause());
                } else {
                    vertx.setTimer(1000, l -> {
                        vertx.cancelTimer(l);
                        tryGetPortfolioService(portfolioFuture);
                    });
                }
            } else {
                portfolioFuture.complete(ar.result());
            }
        });
        ;
    }


}
