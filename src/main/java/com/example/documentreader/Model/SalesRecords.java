package com.example.documentreader.Model;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@Getter
@Setter
public class SalesRecords {

    @CsvBindByName(column = "Region")
    private String Region;
    @CsvBindByName(column = "Country")
    private String Country;
    @CsvBindByName(column = "Item Types")
    private String Item_Types;
    @CsvBindByName(column = "Sales Channel")
    private String Sales_Channel;
    @CsvBindByName(column = "Order Priority")
    private String Order_Priority;
    @CsvBindByName(column = "Order Date")
    private String Order_Date;
    @CsvBindByName(column = "Order ID")
    private String Order_Id;
    @CsvBindByName(column = "Ship Date")
    private String Ship_date;
    @CsvBindByName(column = "Units Sold")
    private String Units_Sold;
    @CsvBindByName(column = "Unit Price")
    private String Unit_Price;
    @CsvBindByName(column = "Unit Cost")
    private String Unit_Cost;
    @CsvBindByName(column = "Total Revenue")
    private String Total_Revenue;
    @CsvBindByName(column = "Total Price")
    private String Total_Price;
    @CsvBindByName(column = "Total Cost")
    private String Total_Cost;


}
