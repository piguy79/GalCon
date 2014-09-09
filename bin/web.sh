#!/bin/bash

if [ "$NODE_DEPLOYMENT" == "true" ]; then
  echo "Deploying NodeJS APP."
  node GalCon-Server/app.js
elif [ "$CLOJURE_DEPLOYMENT" == "true" ]; then
  echo "Deploying Clojure APP"
  java $JVM_OPTS -cp GalCon-Admin/target/galcon-admin-standalone.jar clojure.main -m galcon-admin.web
fi