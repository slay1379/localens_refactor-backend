package com.example.localens.customfeature.util;

import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FormulaEvaluator {
    public double evaluate(String formula, Map<String, Object> variables) {
        try {
            log.info("Formula to evaluate: {}", formula);
            log.info("Available variables: {}", variables);

            Expression expression = new ExpressionBuilder(formula)
                    .variables(variables.keySet())
                    .build();

            // 각 변수의 값 로깅
            for (Entry<String, Object> entry : variables.entrySet()) {
                double value = ((Number) entry.getValue()).doubleValue();
                log.info("Variable {} = {}", entry.getKey(), value);
                expression.setVariable(entry.getKey(), value);
            }

            double result = expression.evaluate();
            log.info("Evaluation result for formula '{}': {}", formula, result);
            return result;
        } catch (Exception e) {
            log.error("Error evaluating formula '{}': {}", formula, e.getMessage());
            throw e;
        }
    }
}
