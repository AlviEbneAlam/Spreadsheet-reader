package com.example.documentreader.Model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@Getter
@Setter
public class merchantTransIDValidationAPIResponse {

    @JsonProperty("APIConnect")
    private String APIConnect;
    @JsonProperty("bank_tran_id")
    private String bank_tran_id;
    @JsonProperty("trans_id")
    private String trans_id;
    @JsonProperty("refund_ref_id")
    private String refund_ref_id;
    @JsonProperty("status")
    private String status;
    @JsonProperty("errorReason")
    private String errorReason;



}
