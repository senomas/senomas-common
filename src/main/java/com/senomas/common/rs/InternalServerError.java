package com.senomas.common.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Internal server error")
public class InternalServerError extends RuntimeException {
	private static final long serialVersionUID = 1676087038885165338L;

	public InternalServerError(Throwable e) {
		super(e);
	}
}
