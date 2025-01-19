package com.example.localens.improvement.domain;

import java.util.List;
import jdk.dynalink.linker.ConversionComparator.Comparison;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommercialDistrictComparisonDTO {
    private List<RecommendedEvent> recommendedEvents;
    private ComparisonData comparisonData;

    @Getter
    @Builder
    public static class RecommendedEvent {
        private String uuid;
        private String name;
        private String imageUrl;
        private String place;
        private String period;
        private String detail;
    }

    @Getter
    @Builder
    public static class ComparisonData {
        private DistrictSnapshot before;
        private DistrictSnapshot after;
        private List<MetricChange> changes;
    }

    @Getter
    @Builder
    public static class DistrictSnapshot {
        private List<MetricsData> overallData;
        private List<String> dates;
    }

    @Getter
    @Builder
    public static class MetricsData {
        private int population;
        private int stayVisit;
        private int congestion;
        private int stayPerVisitor;
        private int visitConcentration;
        private int stayTimeChange;
    }

    @Getter
    @Builder
    public static class MetricChange {
        private String name;
        private Integer value;
    }
}
