package com.example.documentreader.Service;

import com.example.documentreader.Model.ExternalRefundResponse;
import com.example.documentreader.Model.External_Refund;
import com.example.documentreader.Model.merchantTransIDValidationAPIRequest;
import com.example.documentreader.Model.merchantTransIDValidationAPIResponse;
import com.example.documentreader.Repository.externalRefundRepository;
import com.example.documentreader.exception.*;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class readCSV {

    merchantTransIDValidationAPIResponse merchantTransIDValidationAPIResponse;
    merchantTransIDValidationAPIRequest merchantTransIDValidationAPIRequest;
    externalRefundRepository externalRefundRepository;
    External_Refund external_refund;
    FileInputStream fileInputStream;
    ExternalRefundResponse externalRefundResponse;
    File conFile;
    Workbook workbook=null;
    int success=0;
    int failed=0;
    int missingValue=0;
    String extension;
    String card_type;
    DataFormatter formatter = new DataFormatter();

    private final List<String> incomplete_transaction=Arrays.asList("FAILED","INVALID_REQUEST","INACTIVE");

    @Autowired
    public readCSV(
            externalRefundRepository externalRefundRepository,
            External_Refund external_refund,
            ExternalRefundResponse externalRefundResponse) {
        this.externalRefundRepository=externalRefundRepository;
        this.external_refund=external_refund;
        this.externalRefundResponse=externalRefundResponse;
    }

    public readCSV(){

    }

    RestTemplate restTemplate = new RestTemplate();
    List<External_Refund> csvRecords;

    public ExternalRefundResponse uploadFile(MultipartFile multipartFile) throws IOException, InvalidFormatException {

        if(multipartFile.isEmpty()){
            throw new BodyMissingFileException("Required file missing");
        }

        extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());

        if (extension.equals("csv")) {
            getCSVFormatData(multipartFile);

        } else if (extension.equals("xlsx")) {
            getXLSXFormatData(multipartFile);
        }
        else if (extension.equals("xls")) {
            getXLSFormatData(multipartFile);
        }
        else{
            throw new InvalidFileTypeException("File has to be either csv, xlsx or xls");
        }

        externalRefundRepository.saveAll(csvRecords);

        externalRefundResponse.setStatus("200");
        externalRefundResponse.setMessage("Excel sheet successfully extracted");
        externalRefundResponse.setSuccess(success);
        externalRefundResponse.setFailed(failed);
        externalRefundResponse.setMissing_values(missingValue);;

        return externalRefundResponse;

    }

    private void getXLSXFormatData(MultipartFile multipartFile) throws IOException, InvalidFormatException {

        csvRecords=new ArrayList<>();

        try{
            workbook = new XSSFWorkbook(multipartFile.getInputStream());
            extractDataFromWorkbook(workbook);
        }
        catch(IOException e){
            throw new WorkbookException("Exception while reading workbook");
        }
        finally {
            if(workbook!=null){
                try {
                    workbook.close();
                } catch (IOException e) {
                    throw new WorkbookException("Exception while reading workbook");
                }
            }
        }
    }

    private void getXLSFormatData(MultipartFile multipartFile) throws IOException, InvalidFormatException {

        csvRecords=new ArrayList<>();

        try{
            workbook = new HSSFWorkbook(multipartFile.getInputStream());
            extractDataFromWorkbook(workbook);
        }
        catch(IOException  e){
            throw new WorkbookException("Exception while reading workbook");
        }
        finally {
            if(workbook!=null){
                try {
                    workbook.close();
                } catch (IOException e) {
                    throw new WorkbookException("Exception while reading workbook");
                }
            }
        }
    }

    private void extractDataFromWorkbook(Workbook workbook){

        Sheet sheet = workbook.getSheetAt(0);

        success=0;
        failed=0;
        missingValue=0;

        for (Row row : sheet) {
            if (row.getRowNum() != 0) {
                external_refund=new External_Refund();

                external_refund.setBank_tran_id(formatter.formatCellValue(row.getCell(0,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
                external_refund.setRefund_amount(new Double(Objects.requireNonNull(getMerchantRequestObject(row.getCell(1,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL))).toString()));
                external_refund.setRefund_remarks(formatter.formatCellValue(row.getCell(2,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
                external_refund.setRefe_id("0"+formatter.formatCellValue(row.getCell(3,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
                external_refund.setCard_type(formatter.formatCellValue(row.getCell(4,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));

                callSSLApi();
            }
        }

    }


    private void callSSLApi(){

        if(external_refund.getErrorReason()==null ||
                !external_refund.getErrorReason().equals("Missing Value")){

            card_type=external_refund.getCard_type();
            determine_card_type();
            executeUrl();

            if(merchantTransIDValidationAPIResponse==null){
                external_refund.setApiconnect("");
                external_refund.setTrans_id("");
                external_refund.setRefund_ref_id("");
                external_refund.setStatus("");
                external_refund.setErrorReason("");
            }
            else{
                String conn_result=merchantTransIDValidationAPIResponse.getAPIConnect();

                if(conn_result.equals("FAILED") || conn_result.equals("INVALID_REQUEST") || conn_result.equals("INACTIVE")){
                    failed++;
                }
                else if(conn_result.equals("DONE")){
                    success++;
                }

                external_refund.setApiconnect(merchantTransIDValidationAPIResponse.getAPIConnect());
                external_refund.setTrans_id(merchantTransIDValidationAPIResponse.getTrans_id());
                external_refund.setRefund_ref_id(merchantTransIDValidationAPIResponse.getRefund_ref_id());
                external_refund.setStatus(merchantTransIDValidationAPIResponse.getStatus());
                external_refund.setErrorReason(merchantTransIDValidationAPIResponse.getErrorReason());
            }
        }
        else{
            missingValue++;

            external_refund.setApiconnect("");
            external_refund.setTrans_id("");
            external_refund.setRefund_ref_id("");
            external_refund.setStatus("");

        }
        csvRecords.add(external_refund);

    }

    private void getCSVFormatData(MultipartFile multipartFile) throws IOException {

        BufferedReader fileReader = new BufferedReader(new
                InputStreamReader(multipartFile.getInputStream(), "UTF-8"));

        /*CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withQuote(null));*/

        CsvToBean csvToBean = new CsvToBeanBuilder(fileReader).
                withType(merchantTransIDValidationAPIRequest.class).withIgnoreLeadingWhiteSpace(true).build();

        csvRecords = csvToBean.parse();

    }

    private Object getMerchantRequestObject(Cell cell){
        try{
            if (Objects.isNull(cell)) {
                return "";
            }
            switch (cell.getCellType()) {
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case STRING:
                    return cell.getRichStringCellValue().getString();
                case NUMERIC:
                    DataFormatter dataFormatter = new DataFormatter();
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return dataFormatter.formatCellValue(cell);
                    } else {
                        String cellValue = String.valueOf(cell.getNumericCellValue());
                        if (cellValue.contains("E")) {
                            cellValue = BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
                        }
                        return cellValue;
                    }
                case FORMULA:
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    DataFormatter formatter = new DataFormatter();
                    String strValue = formatter.formatCellValue(cell, evaluator);
                    return strValue;
                case ERROR:
                    return cell.getErrorCellValue();
                default:
                    return "";
            }
        }
        catch(Exception e){
            external_refund.setErrorReason("Missing cell value");
        }

        return "";
    }

    public List<External_Refund> getFailedTransactions(){


        return externalRefundRepository.findAllByApiconnect(incomplete_transaction);
    }

    public merchantTransIDValidationAPIResponse callApiForSingleTransaction(
            merchantTransIDValidationAPIRequest merchantTransIDValidationAPIRequest
    ){



        this.merchantTransIDValidationAPIRequest=merchantTransIDValidationAPIRequest;
        executeUrl();

        if(merchantTransIDValidationAPIResponse==null){
            throw new ResourceNotFoundException("Resource not found");
        }
        else{

            external_refund=new External_Refund();
            card_type=merchantTransIDValidationAPIRequest.getCard_type();
            determine_card_type();

            external_refund.setBank_tran_id(merchantTransIDValidationAPIRequest.getBank_tran_id());
            external_refund.setRefund_amount(new Double(merchantTransIDValidationAPIRequest.getRefund_amount()));
            external_refund.setRefund_remarks(merchantTransIDValidationAPIRequest.getRefund_remarks());
            external_refund.setRefe_id(merchantTransIDValidationAPIRequest.getRefe_id());
            external_refund.setCard_type(merchantTransIDValidationAPIRequest.getCard_type());

            external_refund.setApiconnect(merchantTransIDValidationAPIResponse.getAPIConnect());
            external_refund.setTrans_id(merchantTransIDValidationAPIResponse.getTrans_id());
            external_refund.setRefund_ref_id(merchantTransIDValidationAPIResponse.getRefund_ref_id());
            external_refund.setStatus(merchantTransIDValidationAPIResponse.getStatus());
            external_refund.setErrorReason(merchantTransIDValidationAPIResponse.getErrorReason());

            externalRefundRepository.save(external_refund);

            return merchantTransIDValidationAPIResponse;
        }
    }

    private void determine_card_type(){

        
        if(!card_type.isEmpty()){
            String [] split_parts=card_type.split("-");

            if(split_parts[0].equals("VISA")){
                external_refund.setStore_id("NagadVisalive");
                external_refund.setStore_passwd("5EFB0EE307FA559701");
            }
            else if(split_parts[0].equals("MASTERCARD")){
                external_refund.setStore_id("NagadMastercardlive");
                external_refund.setStore_passwd("5EFB0F0FD27C254210");
            }
            else{
                external_refund.setStore_id("");
                external_refund.setStore_passwd("");
            }
        }

    }

    private void executeUrl(){

        String sslcommerz_url = "https://sandbox.sslcommerz.com/validator/api/merchantTransIDvalidationAPI.php";
        String url= sslcommerz_url +"?" +
                "bank_tran_id={bank_tran_id}&" +
                "refund_amount={refund_amount}&" +
                "refund_remarks={refund_remarks}&" +
                "store_id={store_id}&" +
                "store_passwd={store_passwd}&v={v}" +
                "&format=json";

        try{
            merchantTransIDValidationAPIResponse = restTemplate.getForObject(url,
                    merchantTransIDValidationAPIResponse.class,
                    external_refund.getBank_tran_id(),
                    external_refund.getRefund_amount(),
                    external_refund.getRefund_remarks(),
                    external_refund.getStore_id(),
                    external_refund.getStore_passwd(),
                    external_refund.getRefe_id());

        }
        catch(Exception e){
            throw new SomethingWentWrongException("Something went wrong");
        }

    }
}
