package org.picmg.redfish_server_template.data_validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.picmg.redfish_server_template.RFmodels.AllModels.RedfishError;
import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.picmg.redfish_server_template.repository.CachedSchemaRepository;
import org.picmg.redfish_server_template.services.Helpers;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RedfishObjectHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private final ObjectMapper objectMapper;

    private final RedfishErrorResponseService redfishErrorResponseService;

    private final CachedSchemaRepository cachedSchemaRepository;

    public RedfishObjectHandlerMethodArgumentResolver(ObjectMapper objectMapper, RedfishErrorResponseService redfishErrorResponseService, CachedSchemaRepository cachedSchemaRepository) {
        this.objectMapper = objectMapper;
        this.redfishErrorResponseService = redfishErrorResponseService;
        this.cachedSchemaRepository = cachedSchemaRepository;
    }

    private boolean checkAgainstSchemaType(String type, JsonNode data) {
        switch (type) {
            case "array":
                if (!data.isArray()) return false;
                break;
            case "boolean":
                if (!data.isBoolean()) return false;
                break;
            case "integer":
                if (!data.isIntegralNumber()) return false;
                break;
            case "number":
                if ((!data.isFloatingPointNumber())&&(!data.isIntegralNumber())) return false;
                break;
            case "null":
                if (!data.isNull()) return false;
                break;
            case "object":
                if (!data.isObject()) return false;
                break;
            case "string":
                if (!data.isTextual()) return false;
                break;
            default:
                // unknown type
                return false;
        }
        return true;
    }

    private boolean checkAgainstSchemaFormats(String format, JsonNode data) {
        if (!data.isTextual()) return false;
        switch (format) {
            case "date":
                return data.textValue().matches("^(\\d{4}(-\\d\\d){2})$");
            case "date-time":
                return data.textValue().matches("^(\\d{4}(-\\d\\d){2})[tT]?((\\d\\d:){1,2}(\\d\\d)?(.\\d{3})?([zZ]|[+-](\\d\\d):(\\d\\d)))?$");
            case "time":
                return data.textValue().matches("^((\\d\\d:){1,2}(\\d\\d)?(.\\d{3})?([zZ]|[+-](\\d\\d):(\\d\\d)))?$");
            case "uri":
            case "uri-reference":
                try {
                    new URI(data.textValue());
                } catch (URISyntaxException e) {
                    return false;
                }
                return true;
            case "email":
                return data.textValue().matches("^([\\w!#$%&‘*+–/=?^_`{|}~]+(\\.[\\w!#$%&‘*+–/=?^_`{|}~])*)+@([\\w-]+\\.)*([\\w-])+$");
            case "hostname":
                return data.textValue().matches("^([\\w-]+\\.)*([\\w-])+$");
            case "ipv4":
                return data.textValue().matches("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            case "ipv6":
                return data.textValue().matches("^((([0-9A-Fa-f]{1,4}:){1,6}:)|(([0-9A-Fa-f]{1,4}:){7}))([0-9A-Fa-f]{1,4})$");
            case "uuid":
                return data.textValue().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
            default:
                // unknown type
                return false;
        }
    }

    private List<RedfishError> validateAgainstJsonSchema(
            JsonNode object,
            String schemaRef,
            String callingParam) throws JsonProcessingException {
        ArrayList<RedfishError> results = new ArrayList<>();

        // parse the schemaRef
        String schemakey = schemaRef.split("#")[0];
        String local_address = schemaRef.split("#")[1];

        // get the json schema value from the schema repository
        CachedSchema schemaRecord = cachedSchemaRepository.getFirstBySource(schemakey);
        String jsonSchemaStr = schemaRecord.getSchema();
        JsonNode schema = objectMapper.readTree(jsonSchemaStr);

        // attempt to find the referenced fragment of the schema
        if ((!schema.has("definitions"))||(schema.at(local_address).isMissingNode())) {
            results.add(redfishErrorResponseService.getErrorMessage(
                    "Base",
                    "OperationFailed",
                    new ArrayList<>(),
                    new ArrayList<>(Collections.singletonList(schemaRef))));
            return results;
        }

        for (Iterator<String> it = schema.at(local_address).fieldNames(); it.hasNext(); ) {
            String field = it.next();
            switch (field) {
                case "$ref":
                    String dbRefRef = Helpers.createDatabaseReffromRef(schema.get(local_address).get(field).asText(), schemakey);
                    List<RedfishError> r2 = validateAgainstJsonSchema(object, dbRefRef, callingParam);
                    results.addAll(r2);
                    break;
                case "type":
                    if (!checkAgainstSchemaType(schema.at(local_address).get(field).asText(),object)) {
                        results.add(redfishErrorResponseService.getErrorMessage(
                                "Base",
                                "PropertyValueTypeError",
                                new ArrayList<>(Arrays.asList(object.textValue(),callingParam)),
                                new ArrayList<>()));
                    }
                    break;
                case "format":
                    if (!checkAgainstSchemaFormats(schema.at(local_address).get(field).asText(),object)) {
                        results.add(redfishErrorResponseService.getErrorMessage(
                                "Base",
                                "PropertyValueIncorrect",
                                new ArrayList<>(Arrays.asList(object.textValue(),callingParam)),
                                new ArrayList<>()));
                    }
                    break;
                case "anyOf":
                    JsonNode anyOf = schema.at(local_address + "/anyOf");
                    if (!anyOf.isArray()) {
                        results.add(redfishErrorResponseService.getErrorMessage(
                                "Base",
                                "OperationFailed",
                                new ArrayList<>(),
                                new ArrayList<>(Collections.singletonList(local_address + "/anyOf"))));
                        return results;
                    }
                    int anyOfIndex = 0;
                    List<RedfishError> subResults = new ArrayList<>();
                    for (JsonNode element : anyOf) {
                        subResults.clear();

                        // the only elements within an anyOf should be a reference to the json definition to check against
                        // loop to find the first of the references that the data validates against
                        if (!element.has("$ref")) {
                            subResults.add(redfishErrorResponseService.getErrorMessage(
                                    "Base",
                                    "OperationFailed",
                                    new ArrayList<>(),
                                    new ArrayList<>(Collections.singletonList(local_address + "/anyOf["+Integer.toString(anyOfIndex)+"]"))));
                            continue;
                        }
                        // split the reference to get the database name and the definition part
                        String newDbRef = Helpers.createDatabaseReffromRef(element.get("$ref").asText(), schemakey);
                        subResults = validateAgainstJsonSchema(object, newDbRef, callingParam);
                        if (subResults.isEmpty()) {
                            // TODO: perform special checking if the database object is a reference to an object pure reference
                            // this checking should be performed if the ref is to #/definitions/idRef"
                            // Check to see if the object exists
                            // Check to see of the object matches against the schema
                            break;
                        }
                        anyOfIndex ++;
                    }
                    if (!subResults.isEmpty()) {
                        // add the results from the last anyOf compare and return the results
                        results.addAll(subResults);
                        return results;
                    }
                    break;
                case "additionalProperties":
                    // see if there are properties other than the required ones in the object
                    JsonNode allowAdditionalProps = schema.at(local_address + "/additionalProperties");
                    if ((allowAdditionalProps.isBoolean()) && (!allowAdditionalProps.asBoolean())) {
                        // get a list of all expected properties
                        JsonNode schemaPropertiesNode = schema.at(local_address + "/properties");
                        if ((schemaPropertiesNode.isMissingNode())||(!schemaPropertiesNode.isObject())) {
                            results.add(redfishErrorResponseService.getErrorMessage(
                                    "Base",
                                    "OperationFailed",
                                    new ArrayList<>(),
                                    new ArrayList<>(Collections.singletonList(local_address + "/additionalProperties"))));
                            return results;
                        }
                        // iterate through list of actual properties in the object to see if there are extras
                        for (Iterator<String> actualPropIt = object.fieldNames(); actualPropIt.hasNext(); ) {
                            String actualProp = actualPropIt.next();
                            if (!schemaPropertiesNode.has(actualProp)) {
                                results.add(redfishErrorResponseService.getErrorMessage(
                                        "Base",
                                        "PropertyUnknown",
                                        new ArrayList<>(Collections.singletonList(actualProp)),
                                        new ArrayList<>()));
                            }
                        }
                    }
                    break;
                case "patternProperties":
                    // TODO: make sure all properties match the given regex pattern
                    break;
                case "properties":
                    JsonNode schemaProperties = schema.at(local_address).get(field);
                    // check every property in the schema against properties in the data
                    for (Iterator<String> propertyIt = schemaProperties.fieldNames(); propertyIt.hasNext(); ) {
                        String propertyName = propertyIt.next();

                        // see if the field exists in the input data
                        if (object.has(propertyName)) {
                            // check the property
                            JsonNode dataField = object.get(propertyName);
                            List<RedfishError> propertyResults = new ArrayList<>();
                            for (Iterator<String> it2 = schemaProperties.get(propertyName).fieldNames(); it2.hasNext(); ) {
                                String propertyRequirement = it2.next();
                                switch (propertyRequirement) {
                                    case "$ref":
                                        String dbRef = Helpers.createDatabaseReffromRef(schemaProperties.get(propertyName).get(propertyRequirement).asText(), schemakey);
                                        results.addAll(validateAgainstJsonSchema(dataField, dbRef, propertyName));
                                        break;
                                    case "type":
                                        if (!checkAgainstSchemaType(schemaProperties.get(propertyName).get(propertyRequirement).asText(),dataField)) {
                                            results.add(redfishErrorResponseService.getErrorMessage(
                                                    "Base",
                                                    "PropertyValueTypeError",
                                                    new ArrayList<>(Arrays.asList(object.textValue(),propertyName)),
                                                    new ArrayList<>()));
                                        }
                                        break;
                                    case "format":
                                        if (!checkAgainstSchemaFormats(schemaProperties.get(propertyName).get(propertyRequirement).asText(),dataField)) {
                                            results.add(redfishErrorResponseService.getErrorMessage(
                                                    "Base",
                                                    "PropertyValueIncorrect",
                                                    new ArrayList<>(Arrays.asList(object.textValue(),propertyName)),
                                                    new ArrayList<>()));
                                        }
                                        break;
                                    case "readonly":
                                    case "description":
                                    case "longDescription":
                                    case "deletable":
                                    case "insertable":
                                    case "updatable":
                                    case "uris":
                                        // do nothing with this field - not used for validation
                                        break;
                                    default:
                                        results.add(redfishErrorResponseService.getErrorMessage(
                                                "Base",
                                                "OperationFailed",
                                                new ArrayList<>(),
                                                new ArrayList<>(Collections.singletonList(local_address + "/properites/"+propertyRequirement))));
                                        return results;
                                }
                            }
                        }
                    }
                    break;
                case "required":
                    // see if there are properties other than the required ones in the object
                    JsonNode requiredProps = schema.at(local_address + "/required");

                    // iterate through list of required properties to make sure they are all present
                    for (Iterator<String> requiredPropIt = requiredProps.fieldNames(); requiredPropIt.hasNext(); ) {
                        String requiredProp = requiredPropIt.next();
                        if (!object.has(requiredProp)) {
                            results.add(redfishErrorResponseService.getErrorMessage(
                                    "Base",
                                    "PropertyMissing",
                                    new ArrayList<>(Collections.singletonList(requiredProp)),
                                    new ArrayList<>()));
                        }
                    }
                    break;
                case "readonly":
                case "description":
                case "longDescription":
                case "deletable":
                case "insertable":
                case "updatable":
                case "uris":
                    // do nothing with this field - not used for validation
                    break;
                default:
                    results.add(redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "OperationFailed",
                            new ArrayList<>(),
                            new ArrayList<>(Collections.singletonList(local_address + "/"+field))));
                    break;
            }
        }
        return results;
    }
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // verify that the parameter is @ValidRedfishObject
        return parameter.getParameterAnnotation(ValidRedfishObject.class) != null;
    }

    @Override
    // This function overrides the default behavior to resolve in input parameter into an argument.
    // This code is invoked automatically by the spring framework to convert any paramters tagged with the
    // @ValidRedfishObject attribute.
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // get the Name of the JSON Schema from parameter annotation
        String schemaRef = Objects.requireNonNull(parameter.getParameterAnnotation(ValidRedfishObject.class)).value();

        // get the body from the request, interpreted as a UTF_8 string
        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        String body = StreamUtils.copyToString(req.getInputStream(), StandardCharsets.UTF_8);

        // convert the body string into a JsonNode object
        JsonNode jsonNode = objectMapper.readTree(body);

        // validate the Json object against the schema
        List<RedfishError> results = validateAgainstJsonSchema(
                jsonNode,
                schemaRef,
                "<unknown>");
        if (results.isEmpty()) {
            return objectMapper.treeToValue(jsonNode, parameter.getParameterType());
        }

        throw new RedfishValidationException(results);
    }
}
