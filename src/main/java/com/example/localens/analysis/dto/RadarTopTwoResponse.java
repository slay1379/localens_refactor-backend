package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RadarTopTwoResponse {
    private Map<String, Object> overallData; // 전체 데이터 리스트를 Map 형태로 저장
    private Map<String, Object> topTwo;      // 상위 두 항목을 Map 형태로 저장
}
