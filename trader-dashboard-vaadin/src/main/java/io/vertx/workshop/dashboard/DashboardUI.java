package io.vertx.workshop.dashboard;

import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.hazelcast.spi.discovery.integration.DiscoveryService;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.docker.DockerLinksServiceImporter;
import io.vertx.workshop.dashboard.ui.GraphWidget;
import io.vertx.workshop.dashboard.ui.LastOperationsWidget;
import io.vertx.workshop.dashboard.ui.PortfolioWidget;
import io.vertx.workshop.dashboard.ui.ServicesListWidget;
import io.vertx.workshop.portfolio.Portfolio;
import io.vertx.workshop.portfolio.PortfolioService;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by marco on 31/07/16.
 */
@Push
@Theme("dashboard")
@Title("MicroTrader Dashboard")
@Viewport("width=device-width, initial-scale=1.0")
public class DashboardUI extends UI {

    private ServiceDiscovery serviceDiscovery;
    private PortfolioService portfolioService;

    @Override
    protected void init(VaadinRequest request) {

        VertxVaadinService vertxVaadinService = (VertxVaadinService)request.getService();

        PortfolioWidget portfolioWidget = new PortfolioWidget();
        GraphWidget graphWidget = new GraphWidget();
        LastOperationsWidget lastOperationsWidget = new LastOperationsWidget();
        setContent(new MVerticalLayout(
            new MHorizontalLayout(portfolioWidget, graphWidget, lastOperationsWidget)
                .withExpand(portfolioWidget, 1)
                .withExpand(graphWidget, 2)
                .withExpand(lastOperationsWidget, 2)
                .withFullWidth().withMargin(false).withSpacing(true),
            new ServicesListWidget()
        ).withFullWidth().withMargin(true).withSpacing(true));
    }

    // TODO: don't like this, find a better way
    void injectServices(ServiceDiscovery serviceDiscovery, PortfolioService portfolioService) {
        this.serviceDiscovery = serviceDiscovery;
        this.portfolioService = portfolioService;
    }

    public Vertx getVertx() {
        return getService().getVertx();
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public PortfolioService getPortolioService() {
        return portfolioService;
    }

    /*
    public DashboardVaadinVerticle verticle() {
        return (DashboardVaadinVerticle) getService().getVerticle();
    }
    */

    private VertxVaadinService getService() {
        return ((VertxVaadinService) getSession().getService());
    }
}
