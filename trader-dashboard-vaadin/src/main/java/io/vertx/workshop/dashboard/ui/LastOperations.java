package io.vertx.workshop.dashboard.ui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.workshop.dashboard.DashboardUI;
import org.vaadin.viritin.label.MLabel;

/**
 * Created by marco on 31/07/16.
 */
public class LastOperations extends DashboardComponent {

    private Table operationsTable;;
    private long timerID;

    public LastOperations() {
        setCaption("LAST OPERATIONS");
    }

    @Override
    protected Component createContent() {
        operationsTable = new Table();
        operationsTable.setWidth("100%");
        operationsTable.setContainerDataSource(new IndexedContainer());
        operationsTable.addContainerProperty("action", String.class, null, "Action", null, Table.Align.LEFT);
        operationsTable.addContainerProperty("amount", Integer.class, 0, "Amount", null, Table.Align.LEFT);
        operationsTable.addContainerProperty("company", String.class, null, "Company", null, Table.Align.LEFT);
        operationsTable.setPageLength(10);
        return operationsTable;
    }

    @Override
    public void attach() {
        super.attach();
        lastOperations();
        this.timerID = getUI().getVertx().setPeriodic(5000, ar -> {
            lastOperations();
        });
    }

    @Override
    public void detach() {
        getUI().getVertx().cancelTimer(this.timerID);
        super.detach();
    }



    private void lastOperations() {
        HttpEndpoint.getClient(getUI().verticle().getDiscovery(), new JsonObject().put("name", "AUDIT"), client -> {
            if (client.failed() || client.result() == null) {
                noAuditService();
            } else {
                client.result().get("/", response -> {
                    response
                        .exceptionHandler(this::onError)
                        .bodyHandler(buffer -> {
                            updateOperations(buffer.toJsonArray());
                            client.result().close();
                        });
                })
                    .exceptionHandler(this::onError)
                    .end();
            }
        });
    }

    private void updateOperations(JsonArray jsonArray) {
        getUI().access(() -> {
            if (getCompositionRoot() != operationsTable) {
                setCompositionRoot(operationsTable);
            }
            Container container = operationsTable.getContainerDataSource();
            container.removeAllItems();
            jsonArray.stream().map(JsonObject.class::cast)
                .forEach(row -> {
                    Object itemId = container.addItem();
                    Item item = container.getItem(itemId);
                    item.getItemProperty("action").setValue(row.getString("action"));
                    item.getItemProperty("amount").setValue(row.getInteger("amount"));
                    item.getItemProperty("company").setValue(row.getJsonObject("quote").getString("name"));
                });
        });
    }

    private void noAuditService() {
        getUI().access(() -> {
            setCompositionRoot(new MLabel("No audit service available").withStyleName(ValoTheme.LABEL_BOLD));
        });
    }
}
