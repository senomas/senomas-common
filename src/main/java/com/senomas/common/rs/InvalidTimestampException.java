package com.senomas.common.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Invalid timestamp")
public class InvalidTimestampException extends RuntimeException {
	private static final long serialVersionUID = 1676087038885165338L;

}
