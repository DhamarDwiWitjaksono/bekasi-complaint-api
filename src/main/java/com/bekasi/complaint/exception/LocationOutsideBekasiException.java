package com.bekasi.complaint.exception;

public class LocationOutsideBekasiException extends RuntimeException {
    public LocationOutsideBekasiException(String message) {
        super(message);
    }
}
