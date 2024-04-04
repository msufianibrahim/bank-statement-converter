package com.sufian.statementconverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFReader {
    public static void readPDF(String fileType, String inputPath, String outputPath) {
        try {
            // Load the PDF document
            PDDocument document = Loader.loadPDF(new File(inputPath));

            // Create PDFTextStripper class
            PDFTextStripper pdfStripper = new PDFTextStripper();

            // Get text from the PDF document
            String text = pdfStripper.getText(document);

            // Print out the text
            System.out.println(text);

            // Close the document
            document.close();

            // Extract transactions and write to Excel
            if(fileType.equals(PDFConverterGUI.MAYBANK_CREDIT)) {
            	TransactionExtractor.createExcel(TransactionExtractor.extractMaybankCreditTransactions(text), outputPath);
            } else if(fileType.equals(PDFConverterGUI.MAYBANK_DEBIT)) {
            	TransactionExtractor.createExcel(TransactionExtractor.extractMaybankDebitTransactions(text), outputPath);
            } else if(fileType.equals(PDFConverterGUI.CIMB_CREDIT)) {
            	TransactionExtractor.createExcel(TransactionExtractor.extractCIMBCreditTransactions(text), outputPath);
            }

            System.out.println("Transactions extracted and written to Excel successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	
    }
}