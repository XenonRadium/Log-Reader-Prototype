package org.example;//package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
// Specify the log file path
        String logFilePath = "/Users/bernardlee/Desktop/NSSIT/Jaja Logs/1640132641/20231027_2-3.txt"; // Change this to your log file path

        // Create a list to store extracted data
        List<JsonNode> extractedData = new ArrayList<>();
        List<String> extractedHeaders = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath)) ) {
            String line;
            int rowNum = 0;  // Initialize the row number
            while ((line = reader.readLine()) != null) {
                if (line.contains("HttpsJsonOnlineBg")) {
                    StringBuilder logEntry = new StringBuilder();
//                    line = "\"payload\":{\"Transaction_OperatorID\":\"25\",\"Transaction_CorporateFlag\":\"N\",\"Transaction_CardMfgNo\":\"4027422383\",\"Transaction_TerminalID\":\"E01\",\"Transaction_OperationMode\":\"D\",\"Transaction_RequestDt\":\"2023-10-27 09:28:29\",\"Transaction_CardNo\":\"601464000587000153\",\"Transaction_JobType\":\"01\",\"Transaction_Type\":\"01\",\"Transaction_AppSector\":\"02\",\"Transaction_Amount\":\"0000000001\",\"Transaction_ExitMachineNo\":\"41424344\",\"Transaction_CardBalance\":\"0000111006\",\"Transaction_StanNo\":\"003477\",\"Transaction_BatchType\":\"FN\",\"Transaction_BatchNo\":\"163\",\"Transaction_SiteID\":\"020\",\"Transaction_AcgTransID\":\"3E01F1AF-2993-4EAD-B498-B0A8009C12E1\",\"Transaction_EntrySiteID\":\"020\",\"Transaction_TerminalType\":\"01\",\"Transaction_PurseFlag\":\"P\",\"Transaction_DCFlag\":\"D\",\"Transaction_EntryOperatorID\":\"25\",\"Transaction_UserCategory\":\"N\",\"Transaction_CardType\":\"R\",\"Transaction_FreeFormat\":\"25020\",\"Transaction_CardTranNo\":\"59152\",\"Transaction_CardIssuer\":\"50\",\"Transaction_AdditionalInfoType\":\"00\"}";
                    logEntry = new StringBuilder();
                    logEntry.append(line).append("\n");
                    if (line.contains("payload\":")) {
                        // Extract data from the log entry
                        System.out.println("Original String: " + line);
                        line = line.replace("\\", "");
                        System.out.println("Cleaned String: " + line);
                        String payload = extractPayload(line.toString());
                        payload = payload + "}";
                        System.out.println("1st Payload: " + payload);
                        extractedData.add(extractPayloadFields(payload));
                        extractedHeaders.addAll(extractPayloadHeaders(payload));
                        System.out.println("extractedData: " + extractedData.get(0));

                        // Increment the row number for the next data entry
                        rowNum++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create an Excel workbook and sheet
        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("LogData");
//
//            // Create headers for payload fields
//            List<String> headers = new ArrayList<>();
//            for (String[] rowData : extractedData) {
//                for (String field : rowData) {
//                    if (!headers.contains(field)) {
//                        headers.add(field);
//                    }
//                }
//            }
//
//            // Create headers in Excel sheet
//            Row headerRow = sheet.createRow(0);
//            for (int i = 0; i < headers.size(); i++) {
//                headerRow.createCell(i).setCellValue(headers.get(i));
//            }
//
//            // Populate the sheet with extracted data
//            for (int rowNum = 0; rowNum < extractedData.size(); rowNum++) {
//                Row row = sheet.createRow(rowNum + 1);
//                String[] rowData = extractedData.get(rowNum);
//                for (int cellNum = 0; cellNum < rowData.length; cellNum++) {
//                    row.createCell(cellNum).setCellValue(rowData[cellNum]);
//                }
//            }

            Sheet sheet = workbook.createSheet("LogData");

            // Create headers for the Excel sheet
            if (!extractedHeaders.isEmpty()) {
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < extractedHeaders.size(); i++) {
                    headerRow.createCell(i).setCellValue(extractedHeaders.get(i));
                }

                // Populate the sheet with extracted data
                for (int rowNum = 0; rowNum < extractedData.size(); rowNum++) {
                    Row row = sheet.createRow(rowNum + 1);
                    JsonNode rowData = extractedData.get(rowNum);
                    for (int cellNum = 0; cellNum < extractedHeaders.size(); cellNum++) {
                        String fieldName = extractedHeaders.get(cellNum);
                        String value = rowData.has(fieldName) ? rowData.get(fieldName).asText() : "";
                        row.createCell(cellNum).setCellValue(value);
                    }
                }

                // Save the Excel file
                try (FileOutputStream outputStream = new FileOutputStream("log_data.xlsx")) {
                    workbook.write(outputStream);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Excel file created with extracted payload fields.");
    }


    // Extract Payload
    private static String extractPayload(String logEntry) {
//        logEntry = logEntry.replace("\"", "");
        Pattern pattern = Pattern.compile("\"payload\":\"(.*?)}\"");
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    // Extract and return all fields within the payload
//    private static String[] extractPayloadFields(String payload) {
//        System.out.println("payload: " + payload);
//        payload = payload.replace("\"payload\":", "");
//        System.out.println("After Payload: " + payload);
//        ObjectMapper objectMapper = new ObjectMapper();
//        List<String> fields = new ArrayList<>();
//
//        try {
//            JsonNode jsonNode = objectMapper.readTree(payload);
//            Iterator<String> fieldNames = jsonNode.fieldNames();
//
//            while (fieldNames.hasNext()) {
//                fields.add(jsonNode.get(fieldNames.next()).asText());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return fields.toArray(new String[0]);
//    }
    private static JsonNode extractPayloadFields(String payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(payload);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Extract and return headers from the payload
    private static List<String> extractPayloadHeaders(String payload) {
        List<String> headers = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                headers.add(fieldNames.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return headers;
    }

    // Extract and return data from the payload, ensuring consistent headers
    private static Map<String, String> extractPayloadData(JsonNode payloadData, List<String> headers) {
        Map<String, String> rowData = new HashMap<>();
        for (String header : headers) {
            rowData.put(header, payloadData.has(header) ? payloadData.get(header).asText() : "");
        }
        return rowData;
    }
}


//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class Main {
//    public static void main(String[] args) {
//        // Specify the log file path
//        String logFilePath = "/Users/bernardlee/Desktop/NSSIT/Jaja Logs/1640132641/20231027_2-3.txt"; // Change this to your log file path
//
//        // Create a list to store extracted data
//        List<String[]> extractedData = new ArrayList<>();
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath)) ) {
//            String line;
//            StringBuilder logEntry = new StringBuilder();
//            while ((line = reader.readLine()) != null) {
//                if (line.contains("httpsJsonOnline")) {
//                    logEntry = new StringBuilder();
//                }
//                logEntry.append(line).append("\n");
//                if (line.contains("payload\":")) {
//                    // Extract data from the log entry
//                    String signature = extractSignature(logEntry.toString());
//                    String keyIndex = extractKeyIndex(logEntry.toString());
//                    String payload = extractPayload(logEntry.toString());
//
//                    // Add the extracted data to the list
//                    extractedData.add(new String[]{signature, keyIndex, payload});
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Create an Excel workbook and sheet
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("LogData");
//
//            // Create headers
//            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Signature");
//            headerRow.createCell(1).setCellValue("Key Index");
//            headerRow.createCell(2).setCellValue("Payload");
//
//            // Populate the sheet with extracted data
//            for (int rowNum = 0; rowNum < extractedData.size(); rowNum++) {
//                Row row = sheet.createRow(rowNum + 1);
//                String[] rowData = extractedData.get(rowNum);
//                for (int cellNum = 0; cellNum < rowData.length; cellNum++) {
//                    row.createCell(cellNum).setCellValue(rowData[cellNum]);
//                }
//            }
//
//            // Save the Excel file
//            try (FileOutputStream outputStream = new FileOutputStream("log_data.xlsx")) {
//                workbook.write(outputStream);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Excel file created with extracted data.");
//    }
//
//    // Extract Signature
//    private static String extractSignature(String logEntry) {
//        Pattern pattern = Pattern.compile("\"signature\":\"(.*?)\"");
//        Matcher matcher = pattern.matcher(logEntry);
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        return "";
//    }
//
//    // Extract Key Index
//    private static String extractKeyIndex(String logEntry) {
//        Pattern pattern = Pattern.compile("\"key_index\":\"(.*?)\"");
//        Matcher matcher = pattern.matcher(logEntry);
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        return "";
//    }
//
//    // Extract Payload
//    private static String extractPayload(String logEntry) {
//        Pattern pattern = Pattern.compile("\"payload\":\"(.*?)\"");
//        Matcher matcher = pattern.matcher(logEntry);
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        return "";
//    }
//}
//
//
