package com.example.documentreader.Model;

import lombok.*;
import org.springframework.stereotype.Component;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@Getter
@Setter
public class GetSingleRecordModel {

    private String status;
    private String message;
    private int success;
    private int failed;
    private int missing_values;
}
