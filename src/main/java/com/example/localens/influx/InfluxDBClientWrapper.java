package com.example.localens.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.influxdb.query.FluxTable;

import java.util.List;

@Component
public class InfluxDBClientWrapper {

    private final InfluxDBClient influxDBClient;
    private final String org;
    private final String resultBucket;
    private final String stayVisitBucket;
    private final String congestionRateBucket;
    private final String stayPerVisitorBucket;

    public InfluxDBClientWrapper(
            @Value("${influxdb.url}") String url,
            @Value("${influxdb.token}") String token,
            @Value("${influxdb.org}") String org,
            @Value("${influxdb.buckets.result}") String resultBucket,
            @Value("${influxdb.buckets.stayVisit}") String stayVisitBucket,
            @Value("${influxdb.buckets.congestionRate}") String congestionRateBucket,
            @Value("${influxdb.buckets.stayPerVisitor}") String stayPerVisitorBucket) {
        this.influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org);
        this.org = org;
        this.resultBucket = resultBucket;
        this.stayVisitBucket = stayVisitBucket;
        this.congestionRateBucket = congestionRateBucket;
        this.stayPerVisitorBucket = stayPerVisitorBucket;
    }

    public InfluxDBClient getInfluxDBClient() {
        return influxDBClient;
    }

    public String getOrg() {
        return org;
    }

    public String getResultBucket() {
        return resultBucket;
    }

    public String getStayVisitBucket() {
        return stayVisitBucket;
    }

    public String getCongestionRateBucket() {
        return congestionRateBucket;
    }

    public String getStayPerVisitorBucket() {
        return stayPerVisitorBucket;
    }

    // Flux 쿼리를 실행하고 결과를 반환하는 메서드
    public List<FluxTable> query(String fluxQuery) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(fluxQuery, org); // 조직 정보와 함께 쿼리 실행
    }
}
