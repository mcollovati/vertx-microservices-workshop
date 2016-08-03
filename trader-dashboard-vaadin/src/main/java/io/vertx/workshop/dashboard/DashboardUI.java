package io.vertx.workshop.dashboard;

import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import io.vertx.core.Vertx;
import io.vertx.workshop.dashboard.ui.Graph;
import io.vertx.workshop.dashboard.ui.LastOperations;
import io.vertx.workshop.dashboard.ui.PortfolioComponent;
import io.vertx.workshop.dashboard.ui.ServicesList;
import io.vertx.workshop.portfolio.PortfolioService;
import org.vaadin.viritin.layouts.MGridLayout;

/**
 * Created by marco on 31/07/16.
 */
@Push
@Theme(ValoTheme.THEME_NAME)
@Title("MicroTrader Dashboard")
public class DashboardUI extends UI {


    @Override
    protected void init(VaadinRequest request) {
        MGridLayout content = new MGridLayout().withFullWidth()
            .withStyleName("dashboard")
            .withMargin(true).withSpacing(true);

        content.setColumns(2);
        content.setRows(3);
        content.addComponent(new PortfolioComponent(), 0, 0);
        content.addComponent(new Graph(), 0, 1);
        content.addComponent(new LastOperations(), 1, 0, 1, 1);
        content.addComponent(new ServicesList(), 0, 2, 1, 2);
        setContent(content);
    }

    public Vertx getVertx() {
        return getService().getVertx();
    }

    public PortfolioService getPortolioService() {
        return verticle().getPortfolio();
    }

    public DashboardVaadinVerticle verticle() {
        return (DashboardVaadinVerticle) getService().getVerticle();
    }

    private VertxVaadinService getService() {
        return ((VertxVaadinService) getSession().getService());
    }
}
