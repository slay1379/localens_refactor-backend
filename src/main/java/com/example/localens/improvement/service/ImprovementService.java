package com.example.localens.improvement.service;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImprovementService {

    private final EventRepository eventRepository;
    private final EventMetricsRepository eventMetricsRepository;

    @Autowired
    public ImprovementService(EventRepository eventRepository,
                              EventMetricsRepository eventMetricsRepository) {
        this.eventRepository = eventRepository;
        this.eventMetricsRepository = eventMetricsRepository;
    }

    public List<Event> recommendEvents(String districtUuid, String targetClusterUuid) {
        // 개선이 필요한 지표 식별 (구현)
        List<String> metricsToImprove = identifyMetricsToImprove(districtUuid, targetClusterUuid);

        List<EventMetrics> eventMetricsList = eventMetricsRepository.findByMetricsUuidIn(metricsToImprove);

        Set<String> eventUuids = new HashSet<>();
        for (EventMetrics em : eventMetricsList) {
            eventUuids.add(em.getEventUuid());
        }

        List<Event> events = eventRepository.findAllById(eventUuids);

        // 이벤트 정렬 및 추천

        return events;
    }

    private List<String> identifyMetricsToImprove(String districtUuid, String targetClusterUuid) {
        //현재 상권 지표와 목표 클러스터 평균 지표를 비교하여 개선이 필요한 지표 식별
        return Arrays.asList("metric_uuid_1", "metric_uuid_2");
    }
}
