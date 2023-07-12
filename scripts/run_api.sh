#!/bin/bash

arg1="$1"

parquet-tools csv search-api/src/main/resources/$1.parquet.gz > search_pq.csv
cd ../search-api/
mvn clean install
mvn compile exec:java -Dexec.mainClass="com.example.SearchAPI" -Dexec.args="$arg1"
