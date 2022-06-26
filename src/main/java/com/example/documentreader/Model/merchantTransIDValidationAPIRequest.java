package com.example.documentreader.Model;

import com.opencsv.bean.CsvBindByName;
import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class merchantTransIDValidationAPIRequest {

    @CsvBindByName
    @NotNull
    private String bank_tran_id;
    private String store_id;
    private String store_passwd;
    @CsvBindByName
    @NotNull
    private String refund_amount;
    @CsvBindByName
    @NotNull
    private String refund_remarks;
    @CsvBindByName
    @NotNull
    private String refe_id;
    @CsvBindByName
    @NotNull
    private String card_type;
    private final String format="json";

}
