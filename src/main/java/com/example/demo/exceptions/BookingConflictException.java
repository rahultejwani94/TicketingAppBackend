package com.example.demo.exceptions;

public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String msg) {
        super(msg);
    }
}
