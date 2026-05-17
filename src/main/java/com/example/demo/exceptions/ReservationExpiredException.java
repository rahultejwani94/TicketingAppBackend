package com.example.demo.exceptions;

public class ReservationExpiredException extends RuntimeException {
    public ReservationExpiredException(String msg) {
        super(msg);
    }
}
