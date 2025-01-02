package com.example.localens.improvement.service;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.s3.service.S3Service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImprovementService {

    private final EventRepository eventRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final InfluxDBService influxDBService;
    private final S3Service s3Service;

    @Autowired
    public ImprovementService(EventRepository eventRepository,
                              EventMetricsRepository eventMetricsRepository,
                              InfluxDBService influxDBService,
                              S3Service s3Service) {
        this.eventRepository = eventRepository;
        this.eventMetricsRepository = eventMetricsRepository;
        this.influxDBService = influxDBService;
        this.s3Service = s3Service;
    }

    /*public Map<String, Double> recommendationEvents(String districts1, String districts2) {

    }*/

    private Map<String, Double> calculateMetricDifferences(Map<String, Double> metricsA, Map<String, Double> metricsB) {
        Map<String, Double> differences = new HashMap<>();

        for (String metric : metricsA.keySet()) {
            if (metricsB.containsKey(metric)) {
                Double valueA = metricsA.get(metric);
                Double valueB = metricsB.get(metric);
                if (valueA > valueB) {
                    continue;
                }
                double difference = valueB - valueA;
                differences.put(metric, difference);
            }
        }
        return differences;
    }

    private List<String> getTopTwoMetrics(Map<String, Double> metricDifferences) {
        return metricDifferences.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /*private List<Event> findEventByMetrics(List<String> metricsUuids) {
        List<EventMetrics> eventMetricsList = eventMetricsRepository.findByMetricsUuidIn(metricsUuids);

        Set<String> eventUuids = eventMetricsList.stream()
                .map(EventMetrics::getEventUuid)
                .collect(Collectors.toSet());

        return eventRepository.findAllById(eventUuids);
    }*/
}
