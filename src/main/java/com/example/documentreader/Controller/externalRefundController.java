package com.example.documentreader.Controller;

import com.nagad.externalrefund.Model.ExternalRefundResponse;
import com.nagad.externalrefund.Model.External_Refund;
import com.nagad.externalrefund.Model.merchantTransIDValidationAPIRequest;
import com.nagad.externalrefund.Model.merchantTransIDValidationAPIResponse;
import com.nagad.externalrefund.Service.readCSV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.url}")
public class externalRefundController {

    private readCSV readCSV;

    @Autowired
    public externalRefundController(readCSV readCSV){
        this.readCSV=readCSV;
    }

    @PostMapping(path = "externalRefundService/uploadCSV")
    public ExternalRefundResponse uploadFile(@RequestParam("file") MultipartFile multipartFile) throws Exception {
         return readCSV.uploadFile(multipartFile);
    }

    @GetMapping(path = "externalRefundService/getFailedTransactions")
    public List<External_Refund> getFailedTransaction() throws Exception {
        return readCSV.getFailedTransactions();
    }

    @PostMapping(path = "externalRefundService/callApiForSingleTransaction",headers = "Accept=*/*",produces = "application/json", consumes="application/json")
    public merchantTransIDValidationAPIResponse callApiForSingleTransaction(@RequestBody merchantTransIDValidationAPIRequest
                                                                           merchantTransIDValidationAPIRequest){


        return readCSV.callApiForSingleTransaction(merchantTransIDValidationAPIRequest);
    }
}
