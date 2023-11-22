package org.picmg.redfish_server_template.data_validation;

import org.picmg.redfish_server_template.RFmodels.AllModels.RedfishError;
import org.picmg.redfish_server_template.RFmodels.AllModels.RedfishErrorError;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@ControllerAdvice
public class RedfishValidationExceptionHandler {
    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    @ExceptionHandler(RedfishValidationException.class)
    public ResponseEntity<RedfishError> onRedfishValidationException(RedfishValidationException exception) {
        List<RedfishError> messages = exception.getErrorMessages();
        TreeMap<String,Object> errors = new TreeMap<>();
        RedfishError result;
        if (messages.size()>1) {
            result = redfishErrorResponseService.getErrorMessage(
                    "Base",
                    "GeneralError",
                    new ArrayList<String>(), new ArrayList<String>());
            result.getError().getAtMessageExtendedInfo().clear();
        } else {
            result = new RedfishError();
            result.setError(new RedfishErrorError());
            result.getError().setCode(messages.get(0).getError().getCode());
            result.getError().setMessage(messages.get(0).getError().getMessage());
            result.getError().setAtMessageExtendedInfo(new ArrayList<>());
        }
        for (RedfishError err: messages) {
            result.getError().getAtMessageExtendedInfo().addAll(err.getError().getAtMessageExtendedInfo());
        }
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(result);
    }
}