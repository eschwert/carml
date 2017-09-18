package com.taxonic.rml.engine;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.taxonic.rml.engine.template.Template;
import com.taxonic.rml.engine.template.Template.Expression;

class GetTemplateValue implements Function<EvaluateExpression, Optional<Object>> {

	private Template template;
	private Set<Expression> expressions;
	private Function<String, String> transformValue;
	private Function<Object, String> createNaturalRdfLexicalForm;

	GetTemplateValue(
		Template template,
		Set<Expression> expressions,
		Function<String, String> transformValue,
		Function<Object, String> createNaturalRdfLexicalForm
	) {
		this.template = template;
		this.expressions = expressions;
		this.transformValue = transformValue;
		this.createNaturalRdfLexicalForm = createNaturalRdfLexicalForm;
	}

	@Override
	public Optional<Object> apply(EvaluateExpression evaluateExpression) {
		Template.Builder templateBuilder = template.newBuilder();
		expressions.forEach(e -> bindTemplateExpression(e, evaluateExpression, templateBuilder));
		return templateBuilder.create();
	}
	
	private Template.Builder bindTemplateExpression(
		Expression expression, 
		EvaluateExpression evaluateExpression, 
		Template.Builder templateBuilder
	) {
		return templateBuilder.bind(
			expression,
			e -> evaluateExpression
				.apply(e.getValue())
				.map(this::prepareValueForTemplate)
		);
	}
	
	/**
	 * See https://www.w3.org/TR/r2rml/#from-template
	 * @param raw
	 * @return
	 */
	private String prepareValueForTemplate(Object raw) {
		Objects.requireNonNull(raw);
		String value = createNaturalRdfLexicalForm.apply(raw);
		return transformValue.apply(value);
	}

}
