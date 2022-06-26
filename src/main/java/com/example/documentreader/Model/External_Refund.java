package com.example.documentreader.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class External_Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "external_refund_gen")
    @SequenceGenerator(name="external_refund_gen", sequenceName = "external_refund_seq", allocationSize=1,initialValue = 1)
    private int id;
    private String bank_tran_id;
    private Double refund_amount;
    private String refund_remarks;
    private String refe_id;
    private String apiconnect;
    private String trans_id;
    private String refund_ref_id;
    private String status;
    private String errorReason;
    private String store_id;
    private String store_passwd;
    private final String format="json";
    private String card_type;





}
