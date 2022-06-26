package com.example.documentreader.Controller;

import com.example.documentreader.Model.GetSingleRecordModel;
import com.example.documentreader.Model.MockAPIRequest;
import com.example.documentreader.Model.MockAPIResponse;
import com.example.documentreader.Model.StoreRequestResponseDTO;
import com.example.documentreader.Service.RecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.url}")
public class RecordsController {

    private RecordsService recordsService;

    @Autowired
    public RecordsController(RecordsService recordsService){
        this.recordsService = recordsService;
    }

    @PostMapping(path = "externalRefundService/uploadCSV")
    public GetSingleRecordModel uploadFile(@RequestParam("file") MultipartFile multipartFile) throws Exception {
         return recordsService.uploadFile(multipartFile);
    }

    @GetMapping(path = "externalRefundService/getFailedTransactions")
    public List<StoreRequestResponseDTO> getFailedTransaction() throws Exception {
        return recordsService.getFailedTransactions();
    }

    @PostMapping(path = "externalRefundService/callApiForSingleTransaction",headers = "Accept=*/*",produces = "application/json", consumes="application/json")
    public MockAPIResponse callApiForSingleTransaction(@RequestBody MockAPIRequest
                                                                           merchantTransIDValidationAPIRequest){


        return recordsService.callApiForSingleTransaction(merchantTransIDValidationAPIRequest);
    }
}
