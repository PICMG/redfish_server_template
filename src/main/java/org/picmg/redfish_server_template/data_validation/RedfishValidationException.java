package org.picmg.redfish_server_template.data_validation;

import org.picmg.redfish_server_template.RFmodels.AllModels.RedfishError;

import java.util.List;

public class RedfishValidationException extends RuntimeException {
    private final List<RedfishError> errors;

    public RedfishValidationException(List<RedfishError> errors) {
        super("Error(s) validating against json schema");
        this.errors = errors;
    }

    public List<RedfishError> getErrorMessages() {
        return errors;
    }
}
