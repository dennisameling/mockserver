package org.mockserver.matchers;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.StringUtils;
import org.mockserver.log.model.LogEntry;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.*;
import org.mockserver.serialization.ObjectMapperFactory;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.formatting.StringFormatter.formatLogMessage;
import static org.mockserver.log.model.LogEntry.LogMessageType.EXPECTATION_MATCHED;
import static org.mockserver.log.model.LogEntry.LogMessageType.EXPECTATION_NOT_MATCHED;
import static org.mockserver.matchers.OperationIdValidator.OPERATION_NO_MATCH_KEY;
import static org.mockserver.model.NottableString.serialiseNottableString;
import static org.mockserver.openapi.OpenAPIConverter.buildOpenAPI;
import static org.mockserver.openapi.OpenAPIConverter.retrieveOperation;
import static org.slf4j.event.Level.DEBUG;
import static org.slf4j.event.Level.ERROR;

public class OpenAPIMatcher extends AbstractHttpRequestMatcher {

    public static final ObjectWriter OBJECT_WRITER = ObjectMapperFactory.createObjectMapper().writerWithDefaultPrettyPrinter();
    public static final String OPEN_API_LOAD_ERROR = "Unable to load API spec from provided URL or payload because ";
    private static final String MATCHED = " matched ";
    private int hashCode;
    private OpenAPIDefinition openAPIDefinition;
    private OpenApiInteractionValidator openApiInteractionValidator;
    private String definitionAsString;

    public OpenAPIMatcher(MockServerLogger mockServerLogger) {
        super(mockServerLogger);
    }

    @Override
    public boolean apply(RequestDefinition requestDefinition) {
        OpenAPIDefinition openAPIDefinition = requestDefinition instanceof OpenAPIDefinition ? (OpenAPIDefinition) requestDefinition : null;
        if (this.openAPIDefinition == null || !this.openAPIDefinition.equals(openAPIDefinition)) {
            this.openAPIDefinition = openAPIDefinition;
            if (openAPIDefinition != null && isNotBlank(openAPIDefinition.getSpecUrlOrPayload())) {
                OpenApiInteractionValidator.Builder builder = buildValidator(openAPIDefinition);
                try {
                    this.openApiInteractionValidator = builder.build();
                } catch (Throwable throwable) {
                    String message = OPEN_API_LOAD_ERROR + throwable.getMessage();
                    if (throwable instanceof OpenApiInteractionValidator.ApiLoadException) {
                        OpenApiInteractionValidator.ApiLoadException apiLoadException = (OpenApiInteractionValidator.ApiLoadException) throwable;
                        if (!apiLoadException.getParseMessages().isEmpty()) {
                            message = OPEN_API_LOAD_ERROR + String.join(" and ", apiLoadException.getParseMessages()).trim();
                        }

                    }
                    throw new IllegalArgumentException(message);
                }
            } else {
                this.openApiInteractionValidator = null;
            }
            this.hashCode = 0;
            return true;
        } else {
            return false;
        }
    }

    private OpenApiInteractionValidator.Builder buildValidator(OpenAPIDefinition openAPIDefinition) {
        OpenApiInteractionValidator.Builder builder;
        String specUrlOrPayload = openAPIDefinition.getSpecUrlOrPayload();
        if (specUrlOrPayload.endsWith(".json") || specUrlOrPayload.endsWith(".yaml")) {
            builder = OpenApiInteractionValidator.createForSpecificationUrl(specUrlOrPayload);
        } else if (isJsonOrYaml(specUrlOrPayload)) {
            builder = OpenApiInteractionValidator.createForInlineApiSpecification(specUrlOrPayload);
        } else {
            builder = OpenApiInteractionValidator.createFor(specUrlOrPayload);
        }
        if (isNotBlank(openAPIDefinition.getOperationId())) {
            builder.withCustomRequestValidation(new OperationIdValidator(this.openAPIDefinition.getOperationId()));
        }
        return builder;
    }

    private String asString(OpenAPIDefinition openAPIDefinition) {
        try {
            if (StringUtils.isBlank(openAPIDefinition.getOperationId())) {
                return OBJECT_WRITER.writeValueAsString(buildOpenAPI(openAPIDefinition.getSpecUrlOrPayload()));
            } else {
                Optional<Operation> operation = retrieveOperation(openAPIDefinition.getSpecUrlOrPayload(), openAPIDefinition.getOperationId());
                if (operation.isPresent()) {
                    return openAPIDefinition.getOperationId() + ": " + OBJECT_WRITER.writeValueAsString(operation.get());
                }
            }
        } catch (Throwable throwable) {
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(ERROR)
                    .setMessageFormat("Exception while serialising specification for OpenAPIDefinition{}")
                    .setArguments(openAPIDefinition)
                    .setThrowable(throwable)
            );
        }
        return "";
    }

    private boolean isJsonOrYaml(String specUrlOrPayload) {
        try {
            new ObjectMapper(new YAMLFactory()).readTree(specUrlOrPayload);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    @Override
    public boolean matches(MatchDifference context, RequestDefinition requestDefinition) {
        boolean overallMatch = false;
        if (requestDefinition instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) requestDefinition;
            ValidationReport validationReport = matchesOpenAPI(request);
            StringBuilder becauseBuilder = new StringBuilder();
            overallMatch = buildBecause(context, becauseBuilder, validationReport);
            if (!controlPlaneMatcher) {
                HttpRequest httpRequest = context != null ? context.getHttpRequest() : null;
                if (overallMatch) {
                    mockServerLogger.logEvent(
                        new LogEntry()
                            .setType(EXPECTATION_MATCHED)
                            .setLogLevel(Level.INFO)
                            .setHttpRequest(httpRequest)
                            .setExpectation(this.expectation)
                            .setMessageFormat(this.expectation == null ? REQUEST_DID_MATCH : EXPECTATION_DID_MATCH)
                            .setArguments(httpRequest, (this.expectation == null ? this : this.expectation.clone()))
                    );
                } else {
                    becauseBuilder.replace(0, 1, "");
                    mockServerLogger.logEvent(
                        new LogEntry()
                            .setType(EXPECTATION_NOT_MATCHED)
                            .setLogLevel(Level.INFO)
                            .setHttpRequest(httpRequest)
                            .setExpectation(this.expectation)
                            .setMessageFormat(this.expectation == null ? REQUEST_DID_NOT_MATCH : becauseBuilder.length() > 0 ? EXPECTATION_DID_NOT_MATCH : EXPECTATION_DID_NOT_MATCH_WITHOUT_BECAUSE)
                            .setArguments(httpRequest, (this.expectation == null ? this : this.expectation.clone()), becauseBuilder.toString())
                    );
                }
            }
        } else if (requestDefinition instanceof OpenAPIDefinition) {
            overallMatch = openAPIDefinition.equals(requestDefinition);
        }
        return overallMatch;
    }

    private boolean buildBecause(MatchDifference context, StringBuilder becauseBuilder, ValidationReport validationReport) {
        boolean overallMatch = true;
        boolean operationMatch = true;
        boolean specificationMatch = true;
        for (ValidationReport.Message message : validationReport.getMessages()) {
            String messageFormat = StringUtils.removeEnd(message.getMessage(), ".");
            if (MockServerLogger.isEnabled(DEBUG)) {
                mockServerLogger.logEvent(
                    new LogEntry()
                        .setLogLevel(DEBUG)
                        .setMessageFormat(messageFormat)
                        .setArguments(message.getAdditionalInfo().toArray())
                );
            }
            if (message.getKey().equals(OPERATION_NO_MATCH_KEY)) {
                if (context != null) {
                    context.addDifference(MatchDifference.Field.OPERATION, message.getMessage(), message.getAdditionalInfo().toArray());
                }
                overallMatch = false;
                operationMatch = false;
            } else {
                if (context != null) {
                    context.addDifference(MatchDifference.Field.SPECIFICATION, messageFormat, message.getAdditionalInfo().toArray());
                }
                overallMatch = false;
                specificationMatch = false;
            }
        }
        fieldBecauseBuild(MatchDifference.Field.OPERATION, operationMatch, becauseBuilder, context);
        fieldBecauseBuild(MatchDifference.Field.SPECIFICATION, specificationMatch, becauseBuilder, context);
        return overallMatch;
    }

    private void fieldBecauseBuild(MatchDifference.Field operation, boolean operationMatch, StringBuilder becauseBuilder, MatchDifference context) {
        becauseBuilder
            .append(NEW_LINE)
            .append(operation.getName())
            .append(operationMatch ? MATCHED : DID_NOT_MATCH);
        if (context != null) {
            List<String> differences = context.getDifferences(operation);
            if (differences != null && !differences.isEmpty()) {
                becauseBuilder.append(COLON_NEW_LINE);
                for (String difference : differences) {
                    becauseBuilder.append(NEW_LINE).append(difference);
                }
            }
            if (operation == MatchDifference.Field.SPECIFICATION) {
                becauseBuilder.append(NEW_LINE).append(formatLogMessage(1, "for:{}", definitionAsString()));
            }
        }
    }

    public String definitionAsString() {
        if (definitionAsString == null) {
            definitionAsString = asString(openAPIDefinition);
        }
        return definitionAsString;
    }

    private ValidationReport matchesOpenAPI(HttpRequest request) {
        if (openApiInteractionValidator != null) {
            String method = request.getMethod() != null && isNotBlank(request.getMethod().getValue()) ? request.getMethod().getValue() : "GET";
            String path = request.getPath() != null ? request.getPath().getValue() : "";
            SimpleRequest.Builder builder = new SimpleRequest
                .Builder(method, path)
                .withBody(request.getBodyAsString());
            for (Header header : request.getHeaderList()) {
                String headerName = header.getName() != null ? header.getName().getValue() : null;
                if (isNotBlank(headerName)) {
                    builder.withHeader(headerName, serialiseNottableString(header.getValues()));
                }
            }
            for (Parameter parameter : request.getQueryStringParameterList()) {
                builder.withQueryParam(parameter.getName().getValue(), serialiseNottableString(parameter.getValues()));
            }
            return openApiInteractionValidator.validateRequest(builder.build());
        } else {
            return ValidationReport.empty();
        }
    }

    @Override
    public String toString() {
        try {
            return ObjectMapperFactory
                .createObjectMapper(true)
                .writeValueAsString(openAPIDefinition);
        } catch (Exception e) {
            return super.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (hashCode() != o.hashCode()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        OpenAPIMatcher that = (OpenAPIMatcher) o;
        return Objects.equals(openAPIDefinition, that.openAPIDefinition);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(super.hashCode(), openAPIDefinition);
        }
        return hashCode;
    }

}
