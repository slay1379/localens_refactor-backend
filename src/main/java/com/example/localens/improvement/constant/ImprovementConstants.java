package com.example.localens.improvement.constant;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class ImprovementConstants {
    public static final Map<String, String> METRIC_DB_MAPPING = Map.of(
            "stayVisit", "STAY_VISIT_RATIO",
            "stayPerVisitor", "STAY_PER_VISITOR",
            "population", "TOTAL_POPULATION",
            "congestion", "CONGESTION_RATE",
            "visitConcentration", "VISIT_CONCENTRATION",
            "stayTimeChange", "STAY_TIME_CHANGE"
    );

    public static final List<String> NORMALIZED_METRICS = List.of(
            "hourlyFloatingPopulation",
            "hourlyStayVisitRatio",
            "hourlyCongestionRateChange",
            "stayPerVisitorDuration",
            "visitConcentration",
            "hourlyAvgStayDurationChange"
    );

    public static final DateTimeFormatter EVENT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    public static final DateTimeFormatter DISTRICT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 MM월");

    private ImprovementConstants() {}
}
