package com.xingray.java.server.spring.valid;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ValidUtil {
    private ValidUtil() {
    }

    public static Map<String, String> bindResultFieldErrorToMap(BindingResult bindingResult) {
        if (bindingResult == null) {
            return Collections.emptyMap();
        }
        if (!bindingResult.hasFieldErrors()) {
            return Collections.emptyMap();
        }
        HashMap<String, String> errors = new HashMap<>(bindingResult.getFieldErrorCount());
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String message = fieldError.getDefaultMessage();
            if (message == null) {
                message = "unknown error";
            }
            errors.put(fieldError.getField(), message);
        }
        return errors;
    }
}
