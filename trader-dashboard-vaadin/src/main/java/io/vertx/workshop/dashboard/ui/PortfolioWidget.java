package io.vertx.workshop.dashboard.ui;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import io.vertx.core.Vertx;
import io.vertx.workshop.dashboard.DashboardUI;
import io.vertx.workshop.portfolio.Portfolio;
import io.vertx.workshop.portfolio.PortfolioService;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Created by marco on 31/07/16.
 */
public class PortfolioWidget extends DashboardWidget {


    private ObjectProperty<Double> cash;
    private ObjectProperty<Double> value;
    private ObjectProperty<Double> totalValue;
    private MVerticalLayout shares = new MVerticalLayout().withMargin(false).withSpacing(false)
        .withFullWidth();

    private long timerID;
    private Map<Company, MLabel> quotes;

    public PortfolioWidget() {
        setCaption("PORTFOLIO");
    }

    @Override
    protected Component createContent() {
        cash = new ObjectProperty<>(0.0);
        value = new ObjectProperty<>(0.0);
        totalValue = new ObjectProperty<>(0.0);
        quotes = Stream.of(Company.values())
            .collect(toMap(identity(), c -> new MLabel(c.toString(), "0").withFullWidth()));
        return new MVerticalLayout(
            //new MHorizontalLayout(
                createLabel("Cash", cash), createLabel("Value", value), createLabel("Total value", totalValue)
            //).withFullWidth().withMargin(false).withSpacing(true),
            //new MHorizontalLayout().add(new ArrayList<>(quotes.values()))
            //    .withFullWidth().withMargin(false).withSpacing(true)
        )
            .add(new ArrayList<>(quotes.values()))
            .withFullWidth().withMargin(false).withSpacing(false);
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
                    portfolio.getShares().entrySet().stream()
                        .filter( kv -> Company.fromName(kv.getKey()).isPresent() )
                        .forEach(kv -> Company.fromName(kv.getKey()).ifPresent( c ->
                            quotes.get(c).setValue(kv.getValue().toString())
                        ));
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
