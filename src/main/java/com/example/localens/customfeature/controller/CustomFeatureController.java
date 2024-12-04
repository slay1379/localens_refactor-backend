package com.example.localens.customfeature.controller;

import com.example.localens.analysis.service.RadarComparisonService;
import com.example.localens.analysis.service.RadarCongestionRateService;
import com.example.localens.analysis.service.RadarFloatingPopulationService;
import com.example.localens.analysis.service.RadarInfoService;
import com.example.localens.analysis.service.RadarStayDurationChangeService;
import com.example.localens.analysis.service.RadarStayPerVisitorService;
import com.example.localens.analysis.service.RadarStayVisitRatioService;
import com.example.localens.analysis.service.RadarVisitConcentrationService;
import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.domain.CustomFeatureCalculationRequest;
import com.example.localens.customfeature.service.CustomFeatureService;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.member.service.MemberService;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/customFeatures")
public class CustomFeatureController {

    private final CustomFeatureService customFeatureService;
    private final InfluxDBService influxDBService;
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    private final RadarComparisonService radarComparisonService;
    private final RadarFloatingPopulationService radarFloatingPopulationService;
    private final RadarStayVisitRatioService radarStayVisitRatioService;
    private final RadarCongestionRateService radarCongestionRateService;
    private final RadarStayPerVisitorService radarStayPerVisitorService;
    private final RadarStayDurationChangeService radarStayDurationChangeService;
    private final RadarInfoService radarInfoService;
    private final RadarVisitConcentrationService radarVisitConcentrationService;

    @Value("${influxdb.measurement}")
    private String measurement;

    @Autowired
    public CustomFeatureController(CustomFeatureService customFeatureService,
                                   InfluxDBService influxDBService,
                                   MemberService memberService,
                                   TokenProvider tokenProvider,
                                   RadarComparisonService radarComparisonService,
                                   RadarFloatingPopulationService radarFloatingPopulationService,
                                   RadarStayVisitRatioService radarStayVisitRatioService,
                                   RadarCongestionRateService radarCongestionRateService,
                                   RadarStayPerVisitorService radarStayPerVisitorService,
                                   RadarStayDurationChangeService radarStayDurationChangeService,
                                   RadarInfoService radarInfoService,
                                   RadarVisitConcentrationService radarVisitConcentrationService) {
        this.customFeatureService = customFeatureService;
        this.influxDBService = influxDBService;
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
        this.radarComparisonService = radarComparisonService;
        this.radarFloatingPopulationService = radarFloatingPopulationService;
        this.radarStayVisitRatioService = radarStayVisitRatioService;
        this.radarCongestionRateService = radarCongestionRateService;
        this.radarStayPerVisitorService = radarStayPerVisitorService;
        this.radarStayDurationChangeService = radarStayDurationChangeService;
        this.radarInfoService = radarInfoService;
        this.radarVisitConcentrationService = radarVisitConcentrationService;
    }

    // 현재 지표 가져오기
    @GetMapping("/metrics")
    public ResponseEntity<List<String>> getAvailableMetrics() {
        List<String> metrics = influxDBService.getFieldKeys(measurement);
        return new ResponseEntity<>(metrics, HttpStatus.OK);
    }

    // 커스텀 수식 계산
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateCustomFeature(@RequestBody CustomFeatureCalculationRequest request) {
        String formula = request.getFormula();
        Map<String, Double> variables = request.getVariables();

        if (!isValidFormula(formula)) {
            return new ResponseEntity<>("Invalid formula", HttpStatus.BAD_REQUEST);
        }

        try {
            Expression e = new ExpressionBuilder(formula)
                    .variables(variables.keySet())
                    .build();

            for (Map.Entry<String, Double> entry : variables.entrySet()) {
                e.setVariable(entry.getKey(), entry.getValue());
            }

            double result = e.evaluate();

            return new ResponseEntity<>(Collections.singletonMap("result", result), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>("Error calculating formula", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/compare/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<Map<String,Object>> compareDistricts(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2
    ) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 두 상권의 데이터를 각각 가져옴
        Map<String, Object> district1Data = radarComparisonService.constructDistrictData(
                districtUuid1,
                radarFloatingPopulationService,
                radarStayVisitRatioService,
                radarCongestionRateService,
                radarStayPerVisitorService,
                radarVisitConcentrationService,
                radarStayDurationChangeService,
                radarInfoService
        );

        Map<String, Object> district2Data = radarComparisonService.constructDistrictData(
                districtUuid2,
                radarFloatingPopulationService,
                radarStayVisitRatioService,
                radarCongestionRateService,
                radarStayPerVisitorService,
                radarVisitConcentrationService,
                radarStayDurationChangeService,
                radarInfoService
        );

        // 두 상권의 기본 정보에서 위도와 경도 제거
        Map<String, Object> district1Info = (Map<String, Object>) district1Data.get("districtInfo");
        district1Info.remove("latitude");
        district1Info.remove("longitude");

        Map<String, Object> district2Info = (Map<String, Object>) district2Data.get("districtInfo");
        district2Info.remove("latitude");
        district2Info.remove("longitude");

        // 두 상권의 overallData 추출
        Map<String, Integer> district1Overall = (Map<String, Integer>) district1Data.get("overallData");
        Map<String, Integer> district2Overall = (Map<String, Integer>) district2Data.get("overallData");

        // 사용자 커스텀 피처 조회 및 계산 후 기존 데이터에 추가
        List<CustomFeature> customFeatures = customFeatureService.getCustomFeaturesByUserUuid(userUuid);
        if (!customFeatures.isEmpty()) {
            CustomFeature customFeature = customFeatures.get(0); // 첫 번째 커스텀 피처만 사용한다고 가정

            // 커스텀 피처 값 계산
            double customFeatureValue1 = customFeatureService.calculateCustomFeatureValue(customFeature, districtUuid1);
            double customFeatureValue2 = customFeatureService.calculateCustomFeatureValue(customFeature, districtUuid2);

            // 각 상권의 overallData에 커스텀 피처 추가
            district1Overall.put(customFeature.getFeatureName(), (int) (customFeatureValue1 * 100));
            district2Overall.put(customFeature.getFeatureName(), (int) (customFeatureValue2 * 100));
        }

        // RadarComparisonService를 사용하여 차이가 큰 두 항목 찾기
        Map<String, Map<String, Object>> topDifferences = radarComparisonService.findTopDifferences(district1Overall, district2Overall);

        // 결과 반환
        Map<String, Object> comparisonResult = new LinkedHashMap<>();
        comparisonResult.put("district1", district1Data);
        comparisonResult.put("district2", district2Data);
        comparisonResult.put("largestDifferences", topDifferences);

        return ResponseEntity.ok(comparisonResult);
    }

    //현재 사용자 커스텀 피처 조회
    @GetMapping("/list")
    public ResponseEntity<List<CustomFeature>> listCustomFeatures(@RequestHeader("Authorization") String authorizationHeader) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UUID userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<CustomFeature> customFeatures = customFeatureService.getCustomFeaturesByUserUuid(userUuid);
        return new ResponseEntity<>(customFeatures, HttpStatus.OK);
    }

    // 피처 생성 처리
    @PostMapping("/create")
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
    }

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

        CustomFeature customFeature = customFeatureService.getCustomFeatureById(customFeatureId);
        if (customFeature == null || !customFeature.getMember().getMemberUuid().equals(userUuid)) {
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
}