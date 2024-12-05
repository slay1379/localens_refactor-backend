package com.example.localens.customfeature.controller;

import com.example.localens.analysis.domain.Pair;
import com.example.localens.analysis.dto.RadarCongestionRateResponse;
import com.example.localens.analysis.dto.RadarFloatingPopulationResponse;
import com.example.localens.analysis.dto.RadarStayDurationChangeResponse;
import com.example.localens.analysis.dto.RadarStayPerVisitorResponse;
import com.example.localens.analysis.dto.RadarStayVisitRatioResponse;
import com.example.localens.analysis.dto.RadarVisitConcentrationResponse;
import com.example.localens.analysis.service.RadarComparisonService;
import com.example.localens.analysis.service.RadarCongestionRateService;
import com.example.localens.analysis.service.RadarFloatingPopulationService;
import com.example.localens.analysis.service.RadarInfoService;
import com.example.localens.analysis.service.RadarStayDurationChangeService;
import com.example.localens.analysis.service.RadarStayPerVisitorService;
import com.example.localens.analysis.service.RadarStayVisitRatioService;
import com.example.localens.analysis.service.RadarVisitConcentrationService;
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
import com.influxdb.exceptions.UnauthorizedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.aspectj.weaver.ast.Expr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
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
    private final MemberRepository memberRepository;

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
                                   MemberRepository memberRepository,
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
        this.memberRepository = memberRepository;
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
            RadarFloatingPopulationResponse floatingPopulation1 = radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid1);
            RadarStayVisitRatioResponse stayVisitRatio1 = radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid1);
            RadarCongestionRateResponse congestionRate1 = radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid1);
            RadarStayPerVisitorResponse stayPerVisitor1 = radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid1);
            RadarVisitConcentrationResponse visitConcentration1 = radarVisitConcentrationService.getVisitConcentrationByDistrictUuid(districtUuid1);
            RadarStayDurationChangeResponse stayDurationChange1 = radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid1);

            RadarFloatingPopulationResponse floatingPopulation2 = radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid2);
            RadarStayVisitRatioResponse stayVisitRatio2 = radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid2);
            RadarCongestionRateResponse congestionRate2 = radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid2);
            RadarStayPerVisitorResponse stayPerVisitor2 = radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid2);
            RadarVisitConcentrationResponse visitConcentration2 = radarVisitConcentrationService.getVisitConcentrationByDistrictUuid(districtUuid2);
            RadarStayDurationChangeResponse stayDurationChange2 = radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid2);

            Map<String, Object> overallDataMap1 = new LinkedHashMap<>();
            overallDataMap1.put("population", (int)(floatingPopulation1.get유동인구_수() * 100));
            overallDataMap1.put("stayVisit", (int)(stayVisitRatio1.get체류_방문_비율() * 100));
            overallDataMap1.put("congestion", (int)(congestionRate1.get혼잡도_변화율() * 100));
            overallDataMap1.put("stayPerVisitor", (int)(stayPerVisitor1.get체류시간_대비_방문자_수() * 100));
            overallDataMap1.put("visitConcentration", (int)(visitConcentration1.get방문_집중도() * 100));
            overallDataMap1.put("stayTimeChange", (int)(stayDurationChange1.get평균_체류시간_변화율() * 100));

            Map<String, Object> overallDataMap2 = new LinkedHashMap<>();
            overallDataMap2.put("population", (int)(floatingPopulation2.get유동인구_수() * 100));
            overallDataMap2.put("stayVisit", (int)(stayVisitRatio2.get체류_방문_비율() * 100));
            overallDataMap2.put("congestion", (int)(congestionRate2.get혼잡도_변화율() * 100));
            overallDataMap2.put("stayPerVisitor", (int)(stayPerVisitor2.get체류시간_대비_방문자_수() * 100));
            overallDataMap2.put("visitConcentration", (int)(visitConcentration2.get방문_집중도() * 100));
            overallDataMap2.put("stayTimeChange", (int)(stayDurationChange2.get평균_체류시간_변화율() * 100));

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

        // 사용자 커스텀 피처 조회 및 계산 후 기존 데이터에 추가
        RadarFloatingPopulationResponse floatingPopulation1 = radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid1);
        RadarStayVisitRatioResponse stayVisitRatio1 = radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid1);
        RadarCongestionRateResponse congestionRate1 = radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid1);
        RadarStayPerVisitorResponse stayPerVisitor1 = radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid1);
        RadarVisitConcentrationResponse visitConcentration1 = radarVisitConcentrationService.getVisitConcentrationByDistrictUuid(districtUuid1);
        RadarStayDurationChangeResponse stayDurationChange1 = radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid1);

        RadarFloatingPopulationResponse floatingPopulation2 = radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid2);
        RadarStayVisitRatioResponse stayVisitRatio2 = radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid2);
        RadarCongestionRateResponse congestionRate2 = radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid2);
        RadarStayPerVisitorResponse stayPerVisitor2 = radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid2);
        RadarVisitConcentrationResponse visitConcentration2 = radarVisitConcentrationService.getVisitConcentrationByDistrictUuid(districtUuid2);
        RadarStayDurationChangeResponse stayDurationChange2 = radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid2);

        Map<String, Object> overallDataMap1 = new LinkedHashMap<>();
        overallDataMap1.put("population", (int)(floatingPopulation1.get유동인구_수() * 100));
        overallDataMap1.put("stayVisit", (int)(stayVisitRatio1.get체류_방문_비율() * 100));
        overallDataMap1.put("congestion", (int)(congestionRate1.get혼잡도_변화율() * 100));
        overallDataMap1.put("stayPerVisitor", (int)(stayPerVisitor1.get체류시간_대비_방문자_수() * 100));
        overallDataMap1.put("visitConcentration", (int)(visitConcentration1.get방문_집중도() * 100));
        overallDataMap1.put("stayTimeChange", (int)(stayDurationChange1.get평균_체류시간_변화율() * 100));

        Map<String, Object> overallDataMap2 = new LinkedHashMap<>();
        overallDataMap2.put("population", (int)(floatingPopulation2.get유동인구_수() * 100));
        overallDataMap2.put("stayVisit", (int)(stayVisitRatio2.get체류_방문_비율() * 100));
        overallDataMap2.put("congestion", (int)(congestionRate2.get혼잡도_변화율() * 100));
        overallDataMap2.put("stayPerVisitor", (int)(stayPerVisitor2.get체류시간_대비_방문자_수() * 100));
        overallDataMap2.put("visitConcentration", (int)(visitConcentration2.get방문_집중도() * 100));
        overallDataMap2.put("stayTimeChange", (int)(stayDurationChange2.get평균_체류시간_변화율() * 100));

        List<Object> overallDataList1 = List.of(floatingPopulation1, stayVisitRatio1, congestionRate1, stayPerVisitor1, visitConcentration1, stayDurationChange1);
        List<Object> overallDataList2 = List.of(floatingPopulation2, stayVisitRatio2, congestionRate2, stayPerVisitor2, visitConcentration2, stayDurationChange2);

        List<Pair<String, Double>> topTwoPairs1 = RadarUtils.findTopTwo(overallDataList1);
        List<Pair<String, Double>> topTwoPairs2 = RadarUtils.findTopTwo(overallDataList2);

        Map<String, Object> topTwo1 = Map.of(
                "name", topTwoPairs1.get(0).getKey(),
                "value", (int) (topTwoPairs1.get(0).getValue() * 100)
        );

        Map<String, Object> topTwo2 = Map.of(
                "name", topTwoPairs2.get(0).getKey(),
                "value", (int) (topTwoPairs2.get(0).getValue() * 100)
        );

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

        Map<String, Object> district1Response = new LinkedHashMap<>();
        district1Response.put("districtName", district1Info.get("districtName"));
        district1Response.put("clusterName", district1Info.get("clusterName"));
        district1Response.put("top", topTwo1);
        district1Response.put("overallData", district1Overall);
        district1Response.put(customFeature.getFeatureName(), (int) result1);

        Map<String, Object> district2Response = new LinkedHashMap<>();
        district2Response.put("districtName", district2Info.get("districtName"));
        district2Response.put("clusterName", district2Info.get("clusterName"));
        district2Response.put("top", topTwo2);
        district2Response.put("overallData", district2Overall);
        district2Response.put(customFeature.getFeatureName(), (int) result2);

        Map<String, Object> comparisonResult = new LinkedHashMap<>();
        comparisonResult.put("district1", district1Response);
        comparisonResult.put("district2", district2Response);

        return ResponseEntity.ok(comparisonResult);
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

        List<CustomFeatureDto> customFeatureDtos = customFeatures.stream()
                .map(feature -> {
                    String formula = feature.getFormula();
                    for (Map.Entry<String, String> entry : reverseFieldMapping.entrySet()) {
                        formula = formula.replace(entry.getKey(), entry.getValue());
                    }
                    return new CustomFeatureDto(feature.getFeatureUuid(), feature.getFeatureName(), formula);
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(customFeatureDtos, HttpStatus.OK);
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