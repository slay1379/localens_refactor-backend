package com.example.localens.analysis.util;

import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfluxQueryUtils {

    // 시간대 숫자 -> 영어단어 매핑
    private static final Map<String, String> NUMBER_TO_WORD_MAP;
    private static final Map<String, String> WORD_TO_NUMBER_MAP;

    static {
        Map<String, String> tempMap = new LinkedHashMap<>();
        tempMap.put("0", "zero"); tempMap.put("1", "one"); tempMap.put("2", "two");
        tempMap.put("3", "three"); tempMap.put("4", "four"); tempMap.put("5", "five");
        tempMap.put("6", "six"); tempMap.put("7", "seven"); tempMap.put("8", "eight");
        tempMap.put("9", "nine"); tempMap.put("10", "ten"); tempMap.put("11", "eleven");
        tempMap.put("12", "twelve"); tempMap.put("13", "thirteen"); tempMap.put("14", "fourteen");
        tempMap.put("15", "fifteen"); tempMap.put("16", "sixteen"); tempMap.put("17", "seventeen");
        tempMap.put("18", "eighteen"); tempMap.put("19", "nineteen"); tempMap.put("20", "twenty");
        tempMap.put("21", "twentyOne"); tempMap.put("22", "twentyTwo"); tempMap.put("23", "twentyThree");
        NUMBER_TO_WORD_MAP = Collections.unmodifiableMap(tempMap);

        // 영어단어 -> 숫자 맵
        WORD_TO_NUMBER_MAP = NUMBER_TO_WORD_MAP.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    // 숫자(문자열) -> 영어 단어
    public static String numberToWord(String numString) {
        return NUMBER_TO_WORD_MAP.getOrDefault(numString, numString);
    }

    // 영어 단어 -> 숫자(문자열)
    public static String wordToNumber(String wordString) {
        return WORD_TO_NUMBER_MAP.getOrDefault(wordString, wordString);
    }

    // Min-Max 정규화 (기본 0.1~1.0 구간 매핑)
    public static double normalize(double value, double min, double max) {
        if (Double.compare(max, min) == 0) {
            return 0.1;
        }
        return ((value - min) / (max - min)) * (1 - 0.1) + 0.1;
    }

    // FluxTable -> double  값 추출
    public static double extractSingleValue(List<FluxTable> tables) {
        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0.0;
        }
        FluxRecord record = tables.get(0).getRecords().get(0);
        Object value = record.getValueByKey("_value");
        if (value == null) return 0.0;
        return Double.parseDouble(value.toString());
    }

    // 시간대별 데이터( tmzn -> _value )를 Map으로 추출
    public static Map<String, Double> extractTimeZoneMap(List<FluxTable> tables) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String tmzn = String.valueOf(record.getValueByKey("tmzn"));
                Object valObj = record.getValueByKey("_value");
                if (tmzn != null && valObj != null) {
                    double value = Double.parseDouble(valObj.toString());
                    String tmznInWords = numberToWord(tmzn);
                    result.put(tmznInWords, value);
                }
            }
        }
        return result;
    }
}
