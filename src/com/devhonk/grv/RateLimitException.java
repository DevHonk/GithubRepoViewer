package com.devhonk.grv;

public class RateLimitException extends Exception {
    public RateLimitException() {
        super();
    }

    public RateLimitException(String msg) {
        super(msg);
    }
}
