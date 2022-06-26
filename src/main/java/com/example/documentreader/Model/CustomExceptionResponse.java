package com.example.documentreader.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomExceptionResponse {
    private String reason;
    private String message;
    private String devMessage;
    private LocalDateTime timestamp;
    private String path;
}
