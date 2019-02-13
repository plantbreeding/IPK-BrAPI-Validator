#!/bin/sh

if [ -z "$DB_USERNAME" ]
then
	sed -i "s|dbuser=DB_USERNAME||g" $JETTY_BASE/resources/config.properties
else
	sed -i "s|DB_USERNAME|$DB_USERNAME|g" $JETTY_BASE/resources/config.properties 
fi

if [ -z "$DB_PASSWORD" ]
then
	sed -i "s|dbpass=DB_PASSWORD||g" $JETTY_BASE/resources/config.properties
else
	sed -i "s|DB_PASSWORD|$DB_PASSWORD|g" $JETTY_BASE/resources/config.properties 
fi

if [ -z "$DB_URL" ]
then
	sed -i "s|dbpath=DB_URL||g" $JETTY_BASE/resources/config.properties
else
	sed -i "s|DB_URL|$DB_URL|g" $JETTY_BASE/resources/config.properties 
fi

if [ -z "$PROXY_HOST" ]
then
	sed -i "s|http.proxyHost=PROXY_HOST||g" $JETTY_BASE/resources/config.properties
else
	sed -i "s|PROXY_HOST|$PROXY_HOST|g" $JETTY_BASE/resources/config.properties 
fi

if [ -z "$PROXY_PORT" ]
then
	sed -i "s|http.proxyPort=PROXY_PORT||g" $JETTY_BASE/resources/config.properties
else
	sed -i "s|PROXY_PORT|$PROXY_PORT|g" $JETTY_BASE/resources/config.properties 
fi

/docker-entrypoint.sh