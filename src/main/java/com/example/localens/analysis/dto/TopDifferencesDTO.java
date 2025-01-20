package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopDifferencesDTO {
    private DifferenceItemDTO key1;
    private DifferenceItemDTO key2;
    private DifferenceItemDTO key3;
}
