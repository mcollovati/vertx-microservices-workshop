package io.vertx.workshop.dashboard;

/**
 * Created by marco on 31/07/16.
 */

import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import com.github.mcollovati.vertx.vaadin.VertxVaadin;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Constants;
import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.docker.DockerLinksServiceImporter;
import io.vertx.servicediscovery.spi.ServiceImporter;
import io.vertx.servicediscovery.spi.ServicePublisher;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.workshop.portfolio.PortfolioService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@VaadinServletConfiguration(productionMode = false, ui = DashboardUI.class)
public class DashboardVaadinVerticle extends VaadinVerticle {

    private PortfolioService portfolio;
    private ServiceDiscovery discovery;

    public PortfolioService getPortfolio() {
        return portfolio;
    }

    public ServiceDiscovery getDiscovery() {
        return discovery;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<Void> starter = Future.future();
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
        discovery.registerServiceImporter(new DockerLinksServiceImporter(), new JsonObject());

        Future<PortfolioService> portfolioFuture = Future.future();
        EventBusService.getProxy(discovery, PortfolioService.class, portfolioFuture.completer());

        super.start(starter);

        CompositeFuture.all(starter, portfolioFuture).setHandler(ar -> {
            if (ar.failed()) {
                startFuture.fail("One of the required service cannot be retrieved: " + ar.cause());
            } else {
                // Our services:
                portfolio = portfolioFuture.result();
                startFuture.complete();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        discovery.close();
        super.stop();
    }

    // TODO: don't like this, find a better way
    @Override
    protected void serviceInitialized(VertxVaadinService service, Router router) {
        service.addSessionInitListener( e -> e.getSession().addUIProvider(new DefaultUIProvider() {
            @Override
            public UI createInstance(UICreateEvent event) {
                UI ui = super.createInstance(event);
                Optional.ofNullable(ui)
                    .filter(DashboardUI.class::isInstance)
                    .map(DashboardUI.class::cast)
                    .ifPresent( dashboardUI -> dashboardUI.injectServices(discovery, portfolio));
                return ui;
            }
        }));
    }

    @Override
    protected VertxVaadin createVertxVaadin(JsonObject vaadinConfig) {
        return VertxVaadin.create(vertx, LocalSessionStore.create(vertx), vaadinConfig);
    }



}
