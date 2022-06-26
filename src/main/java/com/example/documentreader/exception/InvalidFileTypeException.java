package com.example.documentreader.exception;

public class InvalidFileTypeException extends RuntimeException{
    public InvalidFileTypeException(String message) {
        super(message);
    }
}
