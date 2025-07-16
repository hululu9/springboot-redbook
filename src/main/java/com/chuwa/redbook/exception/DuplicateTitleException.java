package com.chuwa.redbook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateTitleException extends RuntimeException {
    public DuplicateTitleException(String title) {
        super(String.format("Post with title '%s' already exists", title));
    }
}
