package com.example.localens.customfeature.controller;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.service.CustomFeatureService;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.member.service.MemberService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Value("${influxdb.measurement}")
    private String measurement;

    @Autowired
    public CustomFeatureController(CustomFeatureService customFeatureService, InfluxDBService influxDBService,MemberService memberService, TokenProvider tokenProvider) {
        this.customFeatureService = customFeatureService;
        this.influxDBService = influxDBService;
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }

    //현재 사용자 커스텀 피처 조회
    @GetMapping
    public ResponseEntity<List<CustomFeature>> listCustomFeatures(@RequestHeader("Authorization") String authorizationHeader) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<CustomFeature> customFeatures = customFeatureService.getCustomFeaturesByUserUuid(userUuid);
        return new ResponseEntity<>(customFeatures, HttpStatus.OK);
    }

    // 피처 생성 처리
    @PostMapping
    public ResponseEntity<?> createCustomFeature(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody CustomFeature customFeature) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        customFeature.setUserUuId(userUuid);

        if (!isValidFormula(customFeature.getFormula())) {
            return new ResponseEntity<>("Invalid formula", HttpStatus.BAD_REQUEST);
        }

        CustomFeature savedCustomFeature = customFeatureService.saveCustomFeature(customFeature);
        return new ResponseEntity<>(savedCustomFeature, HttpStatus.CREATED);
    }

    //피처 삭제
    @PostMapping("/{customFeatureId}")
    public ResponseEntity<?> deleteCustomFeature(@RequestHeader("Authorization") String authorizationHeader,
                                                 @PathVariable Long customFeatureId) {
        String token = tokenProvider.extractToken(authorizationHeader);
        if (token == null || !tokenProvider.validateToken(token)) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String userUuid = tokenProvider.getCurrentUuid(token);
        if (userUuid == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        CustomFeature customFeature = customFeatureService.getCustomFeatureById(customFeatureId);
        if (customFeature == null || !customFeature.getUserUuId().equals(userUuid)) {
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
