package com.example.localens.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InfluxDBClientWrapper {

    private final InfluxDBClient influxDBClient;
    private final String org;
    private final String bucket;

    public InfluxDBClientWrapper(
            @Value("${influxdb.url}") String url,
            @Value("${influxdb.token}") String token,
            @Value("${influxdb.org}") String org,
            @Value("${influxdb.bucket}") String bucket) {
        this.influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
        this.org = org;
        this.bucket = bucket;
    }

    public InfluxDBClient getInfluxDBClient() {
        return influxDBClient;
    }

    public String getOrg() {
        return org;
    }

    public String getBucket() {
        return bucket;
    }
}
