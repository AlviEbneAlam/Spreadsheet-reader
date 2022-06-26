package com.example.documentreader.Handler;

import com.nagad.externalrefund.Model.ExceptionResponse;
import com.nagad.externalrefund.exception.BodyMissingFileException;
import com.nagad.externalrefund.exception.InvalidFileTypeException;
import com.nagad.externalrefund.exception.ResourceNotFoundException;
import com.nagad.externalrefund.exception.SomethingWentWrongException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(BodyMissingFileException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(BodyMissingFileException bodyMissingFileException,
                                                                             HttpServletRequest request) {
        ExceptionResponse resource_not_found = new ExceptionResponse("11_00_403",
                "File not added in body",
                bodyMissingFileException.getMessage(), LocalDateTime.now(), request.getServletPath());
        return new ResponseEntity<>(resource_not_found, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        List<String> stringList = Pattern.compile(":")
                .splitAsStream(ex.getMessage())
                .collect(Collectors.toList());

        ExceptionResponse missing_payload = new ExceptionResponse("11_00_405",
                "Please verify provided data and try again.",
                stringList.get(0), LocalDateTime.now(), ((ServletWebRequest)request).getRequest().getRequestURI());
        return new ResponseEntity<>(missing_payload, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatus status, WebRequest request) {
        ExceptionResponse missing_payload = new ExceptionResponse("11_00_406",
                "Please verify provided data and try again.",
                ex.getMessage(), LocalDateTime.now(), ((ServletWebRequest)request).getRequest().getRequestURI());
        return new ResponseEntity<>(missing_payload, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers, HttpStatus status,
                                                                     WebRequest request) {

        ExceptionResponse media_not_supported = new ExceptionResponse("11_00_408",
                "Please verify provided data and try again.",
                ex.getLocalizedMessage(), LocalDateTime.now(),((ServletWebRequest)request).getRequest().getRequestURI());
        return new ResponseEntity<>(media_not_supported, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatus status, WebRequest request) {

        ExceptionResponse method_not_supported = new ExceptionResponse("11_00_409",
                "Please verify provided data and try again.",
                ex.getLocalizedMessage(), LocalDateTime.now(),((ServletWebRequest)request).getRequest().getRequestURI());
        return new ResponseEntity<>(method_not_supported, HttpStatus.METHOD_NOT_ALLOWED);
    }



    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        String defaultMessages="";

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        for (Map.Entry<String,String> entry : errors.entrySet()){
            defaultMessages=defaultMessages+ entry.getValue()+" ";
        }

        ExceptionResponse argument_not_valid = new ExceptionResponse("11_00_412",
                "Please verify provided data and try again.",
                defaultMessages, LocalDateTime.now(),((ServletWebRequest)request).getRequest().getRequestURI());
        return new ResponseEntity<>(argument_not_valid, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ExceptionResponse> resourceNotFoundException(ResourceNotFoundException resourceNotFoundException,
                                                                       HttpServletRequest request) {
        ExceptionResponse resource_not_found = new ExceptionResponse("11_00_414",
                "Resource not found",
                resourceNotFoundException.getMessage(), LocalDateTime.now(), request.getServletPath());
        return new ResponseEntity<>(resource_not_found, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(SomethingWentWrongException.class)
    public ResponseEntity<ExceptionResponse> handleInValidAccessException(SomethingWentWrongException somethingWentWrongException,
                                                                          HttpServletRequest request) {
        ExceptionResponse invalid_access = new ExceptionResponse("11_00_419",
                "Please verify provided data and try again.",
                somethingWentWrongException.getMessage(), LocalDateTime.now(), request.getServletPath());
        return new ResponseEntity<>(invalid_access, HttpStatus.FORBIDDEN);
    }

}
