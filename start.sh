#!/bin/sh
cd "$(dirname "$0")"
mkdir -p data
curl http://cgv.cgv.tugraz.at/oop2/sensor-data.zip --output data/sensor-data.zip
cd data
unzip sensor-data.zip
rm failed_sensors
rm sensor-data.zip