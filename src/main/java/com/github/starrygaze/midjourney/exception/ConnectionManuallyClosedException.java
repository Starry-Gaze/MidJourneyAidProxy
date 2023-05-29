package com.github.starrygaze.midjourney.exception;

public class ConnectionManuallyClosedException extends Exception {
	public ConnectionManuallyClosedException(String message) {
		super(message);
	}
}