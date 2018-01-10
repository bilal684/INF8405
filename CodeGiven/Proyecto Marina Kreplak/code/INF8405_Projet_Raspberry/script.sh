#!/bin/bash

cd /home/pi/projet

curl -d @ "https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyD782q3neKmVoL7XnhYTw-qz1GOt1kYE5E" -o localisation.json

ls

more localisation.json