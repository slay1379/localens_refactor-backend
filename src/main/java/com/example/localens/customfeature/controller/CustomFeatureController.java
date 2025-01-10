package com.example.localens.customfeature.controller;

import com.example.localens.analysis.domain.Pair;
import com.example.localens.analysis.dto.RadarCongestionRateResponse;
import com.example.localens.analysis.dto.RadarFloatingPopulationResponse;
import com.example.localens.analysis.dto.RadarStayDurationChangeResponse;
import com.example.localens.analysis.dto.RadarStayPerVisitorResponse;
import com.example.localens.analysis.dto.RadarStayVisitRatioResponse;
import com.example.localens.analysis.dto.RadarVisitConcentrationResponse;
import com.example.localens.analysis.service.PopulationDetailsService;
import com.example.localens.analysis.service.RadarComparisonService;
import com.example.localens.analysis.service.RadarInfoService;
import com.example.localens.analysis.util.RadarUtils;
import com.example.localens.customfeature.DTO.CustomFeatureDto;
import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.domain.CustomFeatureCalculationRequest;
import com.example.localens.customfeature.service.CustomFeatureService;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.member.domain.Member;
import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.member.repository.MemberRepository;
import com.example.localens.member.service.MemberService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customFeatures")
public class CustomFeatureController {

    private final CustomFeatureService customFeatureService;
    private final InfluxDBService influxDBService;
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    private final PopulationDetailsService populationDetailsService;

    @Value("${influxdb.measurement}")
    private String measurement;

    @Autowired
    public CustomFeatureController(CustomFeatureService customFeatureService,
                                   InfluxDBService influxDBService,
                                   MemberService memberService,
                                   TokenProvider tokenProvider,
                                   MemberRepository memberRepository,
                                   PopulationDetailsService populationDetailsService) {
        this.customFeatureService = customFeatureService;
        this.influxDBService = influxDBService;
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
        this.populationDetailsService = populationDetailsService;
    }

    // 현재 지표 가져오기
    //@GetMapping("/metrics")
    //public ResponseEntity<List<String>> getAvailableMetrics() {
    //    List<String> metrics = influxDBService.getLatestMetricsByDistrictUuid();
    //    return new ResponseEntity<>(metrics, HttpStatus.OK);
    //}

    // 커스텀 수식 계산
    @PostMapping("/calculateAndCreate/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<?> calculateCustomFeature(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CustomFeatureCalculationRequest request,
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2) {

        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String formula = request.getFormula();
        /*if (!isValidFormula(formula)) {
            return new ResponseEntity<>("유효하지 않은 식", HttpStatus.BAD_REQUEST);
        }*/

        Map<String, String> fieldMapping = Map.of(
                "유동인구 수", "population",
                "체류 방문 비율", "stayVisit",
                "혼잡도 변화율", "congestion",
                "체류시간 대비 방문자 수", "stayPerVisitor",
                "방문 집중도", "visitConcentration",
                "평균 체류시간 변화율", "stayTimeChange"
        );

        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            formula = formula.replace(entry.getKey(), entry.getValue());
        }

        try {
            Map<String, Object> details1 = populationDetailsService.getDetailsByDistrictUuid(districtUuid1);
            Map<String, Object> details2 = populationDetailsService.getDetailsByDistrictUuid(districtUuid2);

            Map<String, Object> overallDataMap1 = buildOverallData(details1);
            Map<String, Object> overallDataMap2 = buildOverallData(details2);

            Expression e1 = new ExpressionBuilder(formula)
                    .variables(overallDataMap1.keySet())
                    .build();

            for (Map.Entry<String, Object> entry : overallDataMap1.entrySet()) {
                e1.setVariable(entry.getKey(), ((Number) entry.getValue()).doubleValue());
            }

            double result1 = e1.evaluate();

            Expression e2 = new ExpressionBuilder(formula)
                    .variables(overallDataMap2.keySet())
                    .build();

            for (Map.Entry<String, Object> entry : overallDataMap2.entrySet()) {
                e2.setVariable(entry.getKey(), ((Number) entry.getValue()).doubleValue());
            }

            double result2 = e2.evaluate();

            Optional<Member> memberOptional = memberRepository.findById(userUuid);
            if (memberOptional.isEmpty()) {
                return new ResponseEntity<>("해당 사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST);
            }
            Member member = memberOptional.get();

            CustomFeature customFeature = new CustomFeature();
            customFeature.setFormula(formula);
            customFeature.setMember(member);
            customFeature.setFeatureName(request.getFeatureName());

            customFeatureService.saveCustomFeature(customFeature, userUuid);

            Map<String, Object> response = new HashMap<>();
            response.put("district1_result", result1);
            response.put("district2_result", result2);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ResponseEntity<>("Error calculating formula", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/compare/{districtUuid1}/{districtUuid2}/{customFeatureUuid}")
    public ResponseEntity<Map<String,Object>> compareDistricts(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2,
            @PathVariable String customFeatureUuid
    ) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CustomFeature customFeature = customFeatureService.getCustomFeatureById(customFeatureUuid);
        if (customFeature == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid featureUuid"));
        }
        String formula = customFeature.getFormula();

        Map<String, Object> district1Info = (Map<String, Object>) district1Data.get("districtInfo");
        district1Info.remove("latitude");
        district1Info.remove("longitude");

        Map<String, Object> district2Info = (Map<String, Object>) district2Data.get("districtInfo");
        district2Info.remove("latitude");
        district2Info.remove("longitude");

        // 두 상권의 overallData 추출
        Map<String, Integer> district1Overall = (Map<String, Integer>) district1Data.get("overallData");
        Map<String, Integer> district2Overall = (Map<String, Integer>) district2Data.get("overallData");

        CustomFeature customFeature = customFeatureService.getCustomFeatureById(customFeatureUuid);
        String formula = customFeature.getFormula();

        try {
            // 3) 상권1, 상권2 종합 파생지표
            Map<String, Object> details1 = populationDetailsService.getDetailsByDistrictUuid(districtUuid1);
            Map<String, Object> details2 = populationDetailsService.getDetailsByDistrictUuid(districtUuid2);

            // 4) buildOverallData
            Map<String, Object> overallDataMap1 = buildOverallData(details1);
            Map<String, Object> overallDataMap2 = buildOverallData(details2);

            // 5) Expression 계산
            Expression e1 = new ExpressionBuilder(formula)
                    .variables(overallDataMap1.keySet())
                    .build();
            for (Map.Entry<String, Object> entry : overallDataMap1.entrySet()) {
                e1.setVariable(entry.getKey(), ((Number) entry.getValue()).doubleValue());
            }
            double result1 = e1.evaluate();

            Expression e2 = new ExpressionBuilder(formula)
                    .variables(overallDataMap2.keySet())
                    .build();
            for (Map.Entry<String, Object> entry : overallDataMap2.entrySet()) {
                e2.setVariable(entry.getKey(), ((Number) entry.getValue()).doubleValue());
            }
            double result2 = e2.evaluate();

            // 6) 정규화
            List<Integer> normalizeResult = normalize((int) result1, (int) result2);

            // 7) 임의로 districtName/clusterName등을 "details"에서 구하거나 DB에서 구할 수도 있음
            //    여기서는 생략 or 필요 시 populationDetailsService가 districtName까지 주도록 해도 됨.

            // 예시 응답
            Map<String, Object> district1Response = new LinkedHashMap<>();
            district1Response.put("districtName", "상권1"); // 임의
            district1Response.put("clusterName", "클러스터1");
            district1Response.put("overallData", overallDataMap1);
            district1Response.put("customFeature", Map.of(
                    "name", customFeature.getFeatureName(),
                    "value", normalizeResult.get(0)
            ));

            Map<String, Object> district2Response = new LinkedHashMap<>();
            district2Response.put("districtName", "상권2"); // 임의
            district2Response.put("clusterName", "클러스터2");
            district2Response.put("overallData", overallDataMap2);
            district2Response.put("customFeature", Map.of(
                    "name", customFeature.getFeatureName(),
                    "value", normalizeResult.get(1)
            ));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("district1", district1Response);
            result.put("district2", district2Response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","Error in compareDistricts"));
        }
    }

    //현재 사용자 커스텀 피처 조회

    @GetMapping("/list")
    public ResponseEntity<List<CustomFeatureDto>> listCustomFeatures(@RequestHeader("Authorization") String authorizationHeader) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<CustomFeature> customFeatures = customFeatureService.getCustomFeaturesByUserUuid(userUuid);

        Map<String, String> reverseFieldMapping = Map.of(
                "population", "유동인구 수",
                "stayVisit", "체류 방문 비율",
                "congestion", "혼잡도 변화율",
                "stayPerVisitor", "체류시간 대비 방문자 수",
                "visitConcentration", "방문 집중도",
                "stayTimeChange", "평균 체류시간 변화율"
        );

        List<CustomFeatureDto> dtoList = customFeatures.stream()
                .map(feature -> {
                    String f = feature.getFormula();
                    for (Map.Entry<String, String> entry : reverseFieldMapping.entrySet()) {
                        f = f.replace(entry.getKey(), entry.getValue());
                    }
                    return new CustomFeatureDto(
                            feature.getFeatureUuid(),
                            feature.getFeatureName(),
                            f
                    );
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    // 피처 생성 처리
    /*@PostMapping("/create")
    public ResponseEntity<?> createCustomFeature(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody CustomFeature customFeature) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        if (!isValidFormula(customFeature.getFormula())) {
            return new ResponseEntity<>("Invalid formula", HttpStatus.BAD_REQUEST);
        }

        CustomFeature savedCustomFeature = customFeatureService.saveCustomFeature(customFeature, userUuid);
        return new ResponseEntity<>(savedCustomFeature, HttpStatus.CREATED);
    }*/
    //피처 삭제

    @DeleteMapping("/{customFeatureId}")
    public ResponseEntity<?> deleteCustomFeature(@RequestHeader("Authorization") String authorizationHeader,
                                                 @PathVariable String customFeatureId) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        CustomFeature cf = customFeatureService.getCustomFeatureById(customFeatureId);
        if (cf == null || !cf.getMember().getMemberUuid().equals(userUuid)) {
            return new ResponseEntity<>("Not Found or Unauthorized", HttpStatus.NOT_FOUND);
        }
        customFeatureService.deleteFeature(customFeatureId);
        return new ResponseEntity<>("Feature deleted successfully", HttpStatus.OK);
    }
    // 수식 검증 메서드

    private boolean isValidFormula(String formula) {
        try {
            // 변수 목록을 InfluxDBService에서 가져옴
            Set<String> variables = new HashSet<>(influxDBService.getFieldKeys(measurement));

            // 수식 파싱
            Expression e = new ExpressionBuilder(formula)
                    .variables(variables)
                    .build();

            for (String var : variables) {
                e.setVariable(var, 1);
            }

            e.evaluate();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
    private List<Integer> normalize(int e1, int e2) {
        List<Integer> result = new ArrayList<>();

        // 최대값 계산
        int max = Math.max(e1, e2);

        // 최대값이 100보다 큰 경우 값을 비례적으로 나눠 정규화
        if (max > 100) {
            double scaleFactor = 100.0 / max;
            e1 = (int) (e1 * scaleFactor);
            e2 = (int) (e2 * scaleFactor);
        }

        result.add(e1);
        result.add(e2);
        return result;
    }

    private Map<String, Object> buildOverallData(Map<String, Object> details) {
        // 1) 결과 map
        Map<String, Object> result = new LinkedHashMap<>();

        // (a) population
        double pop = averageOf( (Map<String,Double>) details.get("hourlyFloatingPopulation") );
        result.put("population", (int)(pop * 100));

        // (b) 체류/방문 비율
        double stayVisit = averageOf( (Map<String,Double>) details.get("hourlyStayVisitRatio") );
        result.put("stayVisit", (int)(stayVisit * 100));

        // (c) 혼잡도
        double congestion = averageOf( (Map<String,Double>) details.get("hourlyCongestionRateChange") );
        result.put("congestion", (int)(congestion * 100));

        // (d) 체류시간 대비 방문자 수
        double spv = averageOf( (Map<String,Double>) details.get("stayPerVisitorDuration") );
        result.put("stayPerVisitor", (int)(spv * 100));

        // (e) 방문 집중도
        double vConc = averageOf( (Map<String,Double>) details.get("visitConcentration") );
        result.put("visitConcentration", (int)(vConc * 100));

        // (f) 평균 체류시간 변화율
        double stayTimeChange = averageOf( (Map<String,Double>) details.get("hourlyAvgStayDurationChange") );
        result.put("stayTimeChange", (int)(stayTimeChange * 100));

        return result;
    }

    private double averageOf(Map<String, Double> map) {
        if (map == null || map.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double val : map.values()) {
            sum += val;
        }
        return sum / map.size();
    }
}