package io.vertx.workshop.dashboard.ui;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import io.vertx.workshop.dashboard.DashboardUI;

public abstract class DashboardComponent extends CustomComponent {

    public DashboardComponent() {
        setWidth("100%");
        setStyleName("block");
        setCompositionRoot(createContent());
    }

    protected abstract Component createContent();

    @Override
    public DashboardUI getUI() {
        return (DashboardUI)super.getUI();
    }


    protected void onError(Throwable throwable) {
        getUI().access(() -> Notification.show("Error", throwable.getMessage(), Notification.Type.TRAY_NOTIFICATION) );
    }
}
