package com.example.documentreader.Service;

import com.example.documentreader.Model.GetSingleRecordModel;
import com.example.documentreader.Model.StoreRequestResponseDTO;
import com.example.documentreader.Model.MockAPIRequest;
import com.example.documentreader.Model.MockAPIResponse;
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
public class RecordsService {

    MockAPIResponse MockAPIResponse;
    MockAPIRequest MockAPIRequest;
    externalRefundRepository externalRefundRepository;
    StoreRequestResponseDTO storeRequestResponseDTO;
    FileInputStream fileInputStream;
    GetSingleRecordModel getSingleRecordModel;
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
    public RecordsService(
            externalRefundRepository externalRefundRepository,
            StoreRequestResponseDTO storeRequestResponseDTO,
            GetSingleRecordModel getSingleRecordModel) {
        this.externalRefundRepository=externalRefundRepository;
        this.storeRequestResponseDTO = storeRequestResponseDTO;
        this.getSingleRecordModel = getSingleRecordModel;
    }

    public RecordsService(){

    }

    RestTemplate restTemplate = new RestTemplate();
    List<StoreRequestResponseDTO> csvRecords;

    public GetSingleRecordModel uploadFile(MultipartFile multipartFile) throws IOException, InvalidFormatException {

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

        getSingleRecordModel.setStatus("200");
        getSingleRecordModel.setMessage("Excel sheet successfully extracted");
        getSingleRecordModel.setSuccess(success);
        getSingleRecordModel.setFailed(failed);
        getSingleRecordModel.setMissing_values(missingValue);;

        return getSingleRecordModel;

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
                storeRequestResponseDTO =new StoreRequestResponseDTO();

                storeRequestResponseDTO.setBank_tran_id(formatter.formatCellValue(row.getCell(0,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
                storeRequestResponseDTO.setRefund_amount(new Double(Objects.requireNonNull(getMerchantRequestObject(row.getCell(1,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL))).toString()));
                storeRequestResponseDTO.setRefund_remarks(formatter.formatCellValue(row.getCell(2,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
                storeRequestResponseDTO.setRefe_id("0"+formatter.formatCellValue(row.getCell(3,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
                storeRequestResponseDTO.setCard_type(formatter.formatCellValue(row.getCell(4,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));

                callSSLApi();
            }
        }

    }


    private void callSSLApi(){

        if(storeRequestResponseDTO.getErrorReason()==null ||
                !storeRequestResponseDTO.getErrorReason().equals("Missing Value")){

            card_type= storeRequestResponseDTO.getCard_type();
            determine_card_type();
            executeUrl();

            if(MockAPIResponse ==null){
                storeRequestResponseDTO.setApiconnect("");
                storeRequestResponseDTO.setTrans_id("");
                storeRequestResponseDTO.setRefund_ref_id("");
                storeRequestResponseDTO.setStatus("");
                storeRequestResponseDTO.setErrorReason("");
            }
            else{
                String conn_result= MockAPIResponse.getAPIConnect();

                if(conn_result.equals("FAILED") || conn_result.equals("INVALID_REQUEST") || conn_result.equals("INACTIVE")){
                    failed++;
                }
                else if(conn_result.equals("DONE")){
                    success++;
                }

                storeRequestResponseDTO.setApiconnect(MockAPIResponse.getAPIConnect());
                storeRequestResponseDTO.setTrans_id(MockAPIResponse.getTrans_id());
                storeRequestResponseDTO.setRefund_ref_id(MockAPIResponse.getRefund_ref_id());
                storeRequestResponseDTO.setStatus(MockAPIResponse.getStatus());
                storeRequestResponseDTO.setErrorReason(MockAPIResponse.getErrorReason());
            }
        }
        else{
            missingValue++;

            storeRequestResponseDTO.setApiconnect("");
            storeRequestResponseDTO.setTrans_id("");
            storeRequestResponseDTO.setRefund_ref_id("");
            storeRequestResponseDTO.setStatus("");

        }
        csvRecords.add(storeRequestResponseDTO);

    }

    private void getCSVFormatData(MultipartFile multipartFile) throws IOException {

        BufferedReader fileReader = new BufferedReader(new
                InputStreamReader(multipartFile.getInputStream(), "UTF-8"));

        /*CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withQuote(null));*/

        CsvToBean csvToBean = new CsvToBeanBuilder(fileReader).
                withType(MockAPIRequest.class).withIgnoreLeadingWhiteSpace(true).build();

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
            storeRequestResponseDTO.setErrorReason("Missing cell value");
        }

        return "";
    }

    public List<StoreRequestResponseDTO> getFailedTransactions(){


        return externalRefundRepository.findAllByApiconnect(incomplete_transaction);
    }

    public MockAPIResponse callApiForSingleTransaction(
            MockAPIRequest MockAPIRequest
    ){



        this.MockAPIRequest = MockAPIRequest;
        executeUrl();

        if(MockAPIResponse ==null){
            throw new ResourceNotFoundException("Resource not found");
        }
        else{

            storeRequestResponseDTO =new StoreRequestResponseDTO();
            card_type= MockAPIRequest.getCard_type();
            determine_card_type();

            storeRequestResponseDTO.setBank_tran_id(MockAPIRequest.getBank_tran_id());
            storeRequestResponseDTO.setRefund_amount(new Double(MockAPIRequest.getRefund_amount()));
            storeRequestResponseDTO.setRefund_remarks(MockAPIRequest.getRefund_remarks());
            storeRequestResponseDTO.setRefe_id(MockAPIRequest.getRefe_id());
            storeRequestResponseDTO.setCard_type(MockAPIRequest.getCard_type());

            storeRequestResponseDTO.setApiconnect(MockAPIResponse.getAPIConnect());
            storeRequestResponseDTO.setTrans_id(MockAPIResponse.getTrans_id());
            storeRequestResponseDTO.setRefund_ref_id(MockAPIResponse.getRefund_ref_id());
            storeRequestResponseDTO.setStatus(MockAPIResponse.getStatus());
            storeRequestResponseDTO.setErrorReason(MockAPIResponse.getErrorReason());

            externalRefundRepository.save(storeRequestResponseDTO);

            return MockAPIResponse;
        }
    }

    private void determine_card_type(){

        
        if(!card_type.isEmpty()){
            String [] split_parts=card_type.split("-");

            if(split_parts[0].equals("VISA")){
                storeRequestResponseDTO.setStore_id("NagadVisalive");
                storeRequestResponseDTO.setStore_passwd("5EFB0EE307FA559701");
            }
            else if(split_parts[0].equals("MASTERCARD")){
                storeRequestResponseDTO.setStore_id("NagadMastercardlive");
                storeRequestResponseDTO.setStore_passwd("5EFB0F0FD27C254210");
            }
            else{
                storeRequestResponseDTO.setStore_id("");
                storeRequestResponseDTO.setStore_passwd("");
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
            MockAPIResponse = restTemplate.getForObject(url,
                    MockAPIResponse.class,
                    storeRequestResponseDTO.getBank_tran_id(),
                    storeRequestResponseDTO.getRefund_amount(),
                    storeRequestResponseDTO.getRefund_remarks(),
                    storeRequestResponseDTO.getStore_id(),
                    storeRequestResponseDTO.getStore_passwd(),
                    storeRequestResponseDTO.getRefe_id());

        }
        catch(Exception e){
            throw new SomethingWentWrongException("Something went wrong");
        }

    }
}
