# Vaadin Dashboard

This repository contains a module with an implementation of the dashboard built with [Vaadin](http://vaadin.com).

![Vaadin dashboard](/docs/images/vertx-vaadin-dashboard.png)


The dashboard is implemented as a Verticle with Vaadin support; before building the project you should
clone the [vertx-vaadin-samples repository](https://github.com/mcollovati/vaadin-vertx-samples) and run
`mvn install` on module **vertx-vaadin** (this module contain the Vaadin verticle implementation).

After that you can launch `mvn clean package docker:build` to create the docker image for the dashboard.

Finally you can run 

```
docker run -p 8083:8080 --rm --name vaadin-dashboard --link audit:AUDIT vertx-microservice-workshop/trader-dashboard-vaadin
```

and then navigate to http://localhost:8084/ and enjoy the Vaadin dashboard.

