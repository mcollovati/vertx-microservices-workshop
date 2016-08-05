package io.vertx.workshop.dashboard;

import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import io.vertx.core.Vertx;
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


    @Override
    protected void init(VaadinRequest request) {
        /*
        MGridLayout content = new MGridLayout().withFullWidth()
            .withStyleName("dashboard")
            .withMargin(true).withSpacing(true);

        content.setColumns(3);
        content.setRows(2);
        content.addComponent(new PortfolioWidget(), 0, 0);
        content.addComponent(new GraphWidget(), 1, 0);
        //content.addComponent(new LastOperationsWidget(), 1, 0, 1, 1);
        content.addComponent(new LastOperationsWidget(), 2, 0);
        content.addComponent(new ServicesListWidget(), 0, 1, 2, 1);
        setContent(content);
        */
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
