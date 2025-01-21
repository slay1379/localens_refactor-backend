package com.example.localens.customfeature.util;

import java.util.Map;
import java.util.Map.Entry;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Component;

@Component
public class FormulaEvaluator {
    public double evaluate(String formula, Map<String, Object> variables) {
        Expression expression = new ExpressionBuilder(formula)
                .variables(variables.keySet())
                .build();

        for (Entry<String, Object> entry : variables.entrySet()) {
            expression.setVariable(entry.getKey(), ((Number) entry.getValue()).doubleValue());
        }

        return expression.evaluate();
    }
}
