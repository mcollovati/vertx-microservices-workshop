package io.vertx.workshop.dashboard.ui;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Hover;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.MarkerSymbolEnum;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.States;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.ui.Component;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by marco on 31/07/16.
 */
public class GraphWidget extends DashboardWidget {

    private long timerID;
    private Chart chart;
    private AtomicInteger xAxisCounter;
    private LinkedHashMap<Company, Integer> quotes;

    private void initQuotes() {
        quotes = new LinkedHashMap<>();
        Stream.of(Company.values())
            .forEach( c -> quotes.put(c, 0) );
    }

    @Override
    protected Component createContent() {
        initQuotes();

        chart = new Chart();
        chart.setWidth("100%");

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.AREASPLINE);
        configuration.getTitle().setText("");
        configuration.getLegend().setEnabled(false);

        PlotOptionsArea plotOptions = new PlotOptionsArea();
        plotOptions.setPointStart(1);
        Marker marker = new Marker();
        marker.setEnabled(false);
        marker.setSymbol(MarkerSymbolEnum.CIRCLE);
        marker.setRadius(2);
        States states = new States();
        states.setHover(new Hover(true));
        marker.setStates(states);
        plotOptions.setMarker(marker);
        configuration.setPlotOptions(plotOptions);

        XAxis xAxis = new XAxis();
        Labels labels = new Labels();
        labels.setFormatter("this.value");
        xAxis.setLabels(labels);
        xAxis.setAllowDecimals(false);
        xAxis.setType(AxisType.CATEGORY);
        configuration.addxAxis(xAxis);


        YAxis yAxis = new YAxis();
        yAxis.setTitle("");
        labels = new Labels();
        labels.setFormatter("this.value");
        yAxis.setLabels(labels);
        configuration.addyAxis(yAxis);


        List<DataSeriesItem> initialData = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new DataSeriesItem(i, 0))
            .collect(Collectors.toList());
        xAxisCounter = new AtomicInteger(10);


        List<Series> series = quotes.keySet().stream()
            .map(Company::toString)
            .map(DataSeries::new).collect(Collectors.toList());
        series.stream().map(DataSeries.class::cast)
            .forEach(s -> s.setData(new ArrayList<>(initialData)));

        configuration.setSeries(series);
        chart.drawChart(configuration);
        return chart;
    }

    @Override
    public void attach() {
        super.attach();
        MessageConsumer marketConsumer = getUI().getVertx().eventBus().<JsonObject>consumer("market").handler(event -> {
            JsonObject quote = event.body();
            Integer price = quote.getInteger("bid", 0);
            String name = quote.getString("name", "").toLowerCase().replace(" ", "");
            Company.fromName(quote.getString("name", ""))
                .ifPresent(c -> quotes.computeIfPresent(c, (q, oldPrice) -> price));
        });
        updateChart();

        this.timerID = getUI().getVertx().setPeriodic(5000, ar -> {
            updateChart();
        });

    }

    private void updateChart() {
        getUI().access(() -> {
            List<Series> series = chart.getConfiguration().getSeries();
            List<Integer> currentQuotes = new ArrayList<>(quotes.values());
            Integer currentCounter = xAxisCounter.incrementAndGet();
            IntStream.range(0, currentQuotes.size())
                .forEach(idx -> {
                    DataSeries l = ((DataSeries) series.get(idx));
                    l.add(new DataSeriesItem(currentCounter, currentQuotes.get(idx)), true, true);
                });
        });
    }

    @Override
    public void detach() {
        getUI().getVertx().cancelTimer(this.timerID);
        super.detach();
    }

}
