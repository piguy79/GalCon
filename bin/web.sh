#!/bin/bash

if [ "$NODE_DEPLOYMENT" == "true" ]; then
  echo "Deploying NodeJS APP."
  node GalCon-Server/app.js
else
  echo "Deploying Clojure APP"
fi