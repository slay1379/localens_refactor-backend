package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class StayPerVisitorResponse {
    private Map<String, Double> 체류시간_대비_방문자수;
}
