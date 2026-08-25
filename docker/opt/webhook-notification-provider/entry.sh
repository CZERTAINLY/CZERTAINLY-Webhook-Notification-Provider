#!/bin/sh

appHome="/opt/webhook-notification-provider"
source ${appHome}/static-functions

log "INFO" "Launching the Webhook Notification Provider"
java $JAVA_OPTS -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -jar ./app.jar
