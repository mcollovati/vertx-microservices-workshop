package io.vertx.workshop.dashboard.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.label.MLabel;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Created by marco on 31/07/16.
 */
public class ServicesListWidget extends DashboardWidget {

    private long timerID;
    private MTable<Record> table;

    public ServicesListWidget() {
        setCaption("AVAILABLE SERVICES");
    }

    @Override
    protected Component createContent() {
        table = new MTable<>(Record.class);
        table.setWidth("100%");
        table.addGeneratedColumn("status", (source, itemId, columnId) ->
            statusLabel(((Record) itemId).getStatus())
        );
        table.addGeneratedColumn("location", (source, itemId, columnId) ->
            listJsonObject(((Record) itemId).getLocation())
        );
        table.addGeneratedColumn("metadata", (source, itemId, columnId) ->
            listJsonObject(((Record) itemId).getMetadata())
        );
        table.setVisibleColumns("name", "status", "type", "registration", "location", "metadata");
        table.setColumnHeaders("Name", "Status", "Type", "Registration Id", "Location", "Metadata");
        return table;
    }

    private Component listJsonObject(JsonObject data) {
        return new MLabel(
            data.getMap().entrySet().stream()
                .map(kv -> String.format("<li>%s = %s</li>", kv.getKey(), kv.getValue()))
                .collect(Collectors.joining("", "<ul>", "</ul>"))
        ).withContentMode(ContentMode.HTML);
    }

    @Override
    public void attach() {
        super.attach();
        listServices();
        this.timerID = getUI().getVertx().setPeriodic(5000, ar -> {
            listServices();
        });
    }

    @Override
    public void detach() {
        getUI().getVertx().cancelTimer(this.timerID);
        super.detach();
    }


    private Label statusLabel(Status status) {
        String caption = status.toString().replace('_', ' ');
        String styleName = (Status.UP == status) ? ValoTheme.LABEL_SUCCESS : ValoTheme.LABEL_FAILURE;
        return new MLabel(caption)
            .withStyleName("service-status").withStyleName(styleName);
    }


    private void listServices() {
        getUI().getServiceDiscovery().getRecords(new JsonObject(), ar -> {
            if (ar.failed()) {
                onError(ar.cause());
                fillServicesTable(emptyList());
            } else {
                fillServicesTable(ar.result());
            }
        });

    }

    private void fillServicesTable(List<Record> records) {
        getUI().access(() -> {
            table.setBeans(records);
            table.setPageLength(records.size());
        });
    }
}
