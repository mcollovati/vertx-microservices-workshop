# Docker compose configuration

In the directory `docker-microsevice-workshop` there is a docker compose configuration that runs all container built 
for this workshop.

First you need to build all docker images following the instruction of the workshop and those provided for 
the [vaadin dashboard](vaadin-dashboard.md).

Then you can launch your containers

```
cd docker-microsevice-workshop
docker-compose up
```

To stop the containers simply press CTRL+C or, from another terminal


```
cd docker-microsevice-workshop 
docker-compose down
```


