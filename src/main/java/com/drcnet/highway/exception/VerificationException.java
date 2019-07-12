package com.drcnet.highway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ml on 2018/11/9.
 */
public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}
