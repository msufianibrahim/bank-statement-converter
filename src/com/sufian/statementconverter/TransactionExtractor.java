package com.sufian.statementconverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionExtractor {
	public static List<Transaction> extractMaybankCreditTransactions(String text) {
        List<Transaction> transactions = new ArrayList<>();

        // Define pattern for transaction entries
        Pattern transactionPattern = Pattern.compile("(\\d{2}/\\d{2})\\s+(\\d{2}/\\d{2})\\s+(.*?)\\s+((?:-)?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})(?!%))\\s*(CR)?");
        // Create matcher for transaction entries
        Matcher matcher = transactionPattern.matcher(text);

        // Iterate through each match and extract transaction information
        while (matcher.find()) {
            String transactionDate = matcher.group(1);
            String postingDate = matcher.group(2);
            String description = matcher.group(3);
            String amountStr = matcher.group(4);
            String crIndicator = matcher.group(5);

            // Remove commas from the amount
            String amount = amountStr.replaceAll(",", "");

            // Add negative sign if CR indicator is present
            if (crIndicator != null && !crIndicator.isEmpty()) {
                amount = "-" + amount;
            }

            // Create Transaction object and add to list
            Transaction transaction = new Transaction(transactionDate, description, amount);
            transactions.add(transaction);
        }

        return transactions;
    }
    public static List<Transaction> extractMaybankDebitTransactions(String text) {
        List<Transaction> transactions = new ArrayList<>();

        // Define pattern for transaction entries
        Pattern transactionPattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{2})\\s+(.*?)\\s+((?:-)?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2}))\\s*(\\+|-)");

        // Create matcher for transaction entries
        Matcher matcher = transactionPattern.matcher(text);

        // Start index for extracting details
        int currentIndex = 0;
        int previousIndex = 0;

        // Find the first transaction
        if (matcher.find(currentIndex)) {
            do {
            	
            	currentIndex = matcher.start();
                
                // Extract transaction information
                String transactionDate = matcher.group(1);
                String description = matcher.group(2);
                String amountStr = matcher.group(3);
                String sign = matcher.group(4);
                String crIndicator = matcher.group(5);
                if(matcher.find(matcher.end())) {
                	previousIndex = currentIndex;
                	currentIndex = matcher.start();
                }
                
                // Get details for the current transaction
                String details = extractMaybankDebitDetails(text, previousIndex, currentIndex);

                // Remove commas from the amount
                String amount = amountStr.replaceAll(",", "");
                
                // Add negative sign if CR indicator is present
                if (crIndicator != null && !crIndicator.isEmpty()) {
                    amount = "-" + amount;
                }
                // Add negative sign if needed
                if ("-".equals(sign)) {
                    amount = "-" + amount;
                }

                // Create Transaction object and add to list
                Transaction transaction = new Transaction(transactionDate, description + " : " + details, amount);
                transactions.add(transaction);

                // Update start index for next iteration
            } while (matcher.find());
        }

        return transactions;
    }
    public static List<Transaction> extractCIMBCreditTransactions(String text) {
        List<Transaction> transactions = new ArrayList<>();

        // Define pattern for transaction entries
        Pattern pattern = Pattern.compile("(\\d{2} [A-Z]{3}) (\\d{2} [A-Z]{3}) ((?:.*?)(?:\\R\\s*.*?)*?)\\s*((?:-)?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2}))\\s+(CR)?");        // Create matcher for transaction entries
        Matcher matcher = pattern.matcher(text);
        

        // Iterate through each match and extract transaction information
        while (matcher.find()) {
        	
        	String transactionDate = matcher.group(2);
            String description = matcher.group(3);
            String amountStr = matcher.group(4);
            String crIndicator = matcher.group(5);
            
            // Remove commas from the amount and trim whitespace
            String amount = amountStr.replaceAll(",", "").trim();
            
            // Add negative sign if CR indicator is present
            if (crIndicator != null && !crIndicator.isEmpty()) {
                amount = "-" + amount;
            }
            // Create Transaction object and add to list
            Transaction transaction = new Transaction(transactionDate, description, amount);
            transactions.add(transaction);
        }

        return transactions;
    }
    public static List<Transaction> extractTouchNGoTransactions(String text) {
        List<Transaction> transactions = new ArrayList<>();

        // Define pattern for transaction entries
        Pattern transactionPattern = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s+(.*?)(?=\\s+RM\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2}))\\s+(RM\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2}))\\s+(RM\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2}))", Pattern.DOTALL);
        Matcher matcher = transactionPattern.matcher(text);

        // Iterate through each match and extract transaction information
        while (matcher.find()) {
            String transactionDate = matcher.group(1);
            String description = matcher.group(2);
            String amountStr = matcher.group(3);
            String walletBalance = matcher.group(4);

            // Remove commas from the amount
            String amount = amountStr.replaceAll(",", "");

            // Add negative sign if Reload is present
            if (description.contains("Reload")) {
                amount = "-" + amount;
            }
            // Skip transaction if GO+ is present
            if (description.contains("GO+ Daily Earnings") || description.contains("GO+ Cash In")) {
                continue;
            }

            // Create Transaction object and add to list
            Transaction transaction = new Transaction(transactionDate, description, amount);
            transactions.add(transaction);
        }

        return transactions;
    }
    private static String extractMaybankDebitDetails(String text, int startIndex, int endIndex) {
        // Extract details from text between startIndex and endIndex
        String details = text.substring(startIndex, endIndex).trim();
        // Remove empty lines and trim whitespace
        details = details.replaceAll("(?m)^[ \t]*\r?\n", "").trim();
        // Split details into lines
        String[] lines = details.split("\n");
        StringBuilder extractedDetails = new StringBuilder();
        // Iterate through lines and add those starting with three spaces
        for (String line : lines) {
            if (line.startsWith("   ")) {
                extractedDetails.append(line.trim()).append("\n");
            }
        }
        return extractedDetails.toString().trim();
    }

    public static void createExcel(List<Transaction> transactions, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Transaction Date");
            headerRow.createCell(1).setCellValue("Description");
            headerRow.createCell(2).setCellValue("Amount");
//            int longestDescriptionLength = 0;

            // Write transaction data
            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getTransactionDate());
                row.createCell(1).setCellValue(transaction.getDescription());
                sheet.autoSizeColumn(1);
                row.createCell(2).setCellValue(transaction.getAmount());
            }
            
            sheet.autoSizeColumn(0);

            // Write the workbook to a file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }

            System.out.println("Excel file created successfully.");
            JOptionPane.showMessageDialog(null, "Excel created successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Transaction {
        private String transactionDate;
        private String description;
        private String amount;

        public Transaction(String transactionDate, String description, String amount) {
            this.transactionDate = transactionDate;
            this.description = description;
            this.amount = amount;
        }

        public String getTransactionDate() {
            return transactionDate;
        }

        public String getDescription() {
            return description;
        }

        public String getAmount() {
            return amount;
        }
    }
}