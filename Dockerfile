# build image : `docker build -t brapicoordinatorselby/brava ./`
# run container (dev): `docker run --name=brava --network=bridge -p 8081:8081 -d brapicoordinatorselby/brava`
# run container (prod): `docker run --name=brava --restart always --network=brapi_net -d brapicoordinatorselby/brava`

FROM jetty

COPY target/brapivalidator.war $JETTY_BASE/webapps/

COPY src/main/resources/config.properties.example $JETTY_BASE/resources/config.properties

COPY start_jetty.sh /

ENTRYPOINT ["/start_jetty.sh"]