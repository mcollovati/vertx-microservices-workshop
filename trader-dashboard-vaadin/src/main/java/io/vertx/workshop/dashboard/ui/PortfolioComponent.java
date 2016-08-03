package io.vertx.workshop.dashboard.ui;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import io.vertx.core.Vertx;
import io.vertx.workshop.dashboard.DashboardUI;
import io.vertx.workshop.portfolio.Portfolio;
import io.vertx.workshop.portfolio.PortfolioService;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by marco on 31/07/16.
 */
public class PortfolioComponent extends CustomComponent {


    private ObjectProperty<Double> cash = new ObjectProperty<>(0.0);
    private ObjectProperty<Double> value = new ObjectProperty<>(0.0);
    private ObjectProperty<Double> totalValue = new ObjectProperty<>(0.0);
    private MVerticalLayout shares = new MVerticalLayout().withMargin(false).withSpacing(false)
        .withFullWidth();

    private long timerID;

    public PortfolioComponent() {
        setCaption("PORTFOLIO");
        setWidth("100%");
        setStyleName("block portfolio");
        setCompositionRoot(new MVerticalLayout(
            createLabel("Cash", cash), createLabel("Value", value), createLabel("Total value", totalValue), shares
        ).withMargin(false).withSpacing(false));

    }

    @Override
    public DashboardUI getUI() {
        return (DashboardUI)super.getUI();
    }

    @Override
    public void attach() {
        super.attach();
        startUpdates();
    }

    private void startUpdates() {
        DashboardUI ui = getUI();
        updatePortfolio(ui);
        this.timerID = ui.getVertx().setPeriodic(5000, ar -> {
            updatePortfolio(ui);
        });
    }

    @Override
    public void detach() {
        getUI().getVertx().cancelTimer(this.timerID);
        super.detach();
    }

    private void updatePortfolio(DashboardUI ui) {
        ui.getPortolioService().getPortfolio(ar -> {
            ui.access(() -> {
                if (ar.failed()) {
                    Notification.show("Error while retrieving the portfolio", Notification.Type.WARNING_MESSAGE);
                } else {
                    Portfolio portfolio = ar.result();
                    cash.setValue(portfolio.getCash());
                    shares.removeAllComponents();
                    portfolio.getShares().entrySet()
                        .stream().sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(e -> new MLabel(e.getKey(), e.getValue().toString()).withFullWidth())
                        .forEach(shares::add);
                }
            });
            evaluatePortfolio(ui);
        });
    }

    private void evaluatePortfolio(DashboardUI ui) {
        ui.getPortolioService().evaluate(ar2 -> {
            ui.access(() -> {
                if (ar2.failed()) {
                    Notification.show("Cannot evaluate portfolio", Notification.Type.WARNING_MESSAGE);
                } else {
                    value.setValue(ar2.result());
                    totalValue.setValue(value.getValue() + cash.getValue());
                }
            });
        });
    }

    private MLabel createLabel(String caption, ObjectProperty<Double> property) {
        MLabel label = new MLabel().withCaption(caption);
        label.setPropertyDataSource(property);
        label.setConverter(new StringToDoubleConverter());
        return label;
    }
}
