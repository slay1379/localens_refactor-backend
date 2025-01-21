package com.example.localens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenderDataDTO {
    @JsonProperty("FEMALE")
    private double FEMALE;

    @JsonProperty("MALE")
    private double MALE;
}
