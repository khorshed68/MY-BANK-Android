package com.khorshed.mybank.utils;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.khorshed.mybank.models.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    public static File generateTransactionReport(
        Context context,
        String accountNumber,
        double currentBalance,
        List<Transaction> transactions
    ) throws DocumentException, IOException {
        
        // Create directory if it doesn't exist
        File directory = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS), "MY_BANK");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create PDF file
        String fileName = "Transaction_Report_" + System.currentTimeMillis() + ".pdf";
        File file = new File(directory, fileName);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        
        document.open();

        // Add title
        Paragraph title = new Paragraph("MY BANK", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Paragraph subtitle = new Paragraph("Transaction Report", HEADER_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Add account info
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        document.add(new Paragraph("Account Number: " + accountNumber, NORMAL_FONT));
        document.add(new Paragraph("Current Balance: ৳" + String.format("%.2f", currentBalance), NORMAL_FONT));
        document.add(new Paragraph("Report Generated: " + dateFormat.format(new Date()), NORMAL_FONT));
        document.add(new Paragraph(" "));

        // Add transactions table
        if (transactions != null && !transactions.isEmpty()) {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            // Add headers
            addTableHeader(table, "Date");
            addTableHeader(table, "Type");
            addTableHeader(table, "Amount");
            addTableHeader(table, "Balance");

            // Add data
            for (Transaction transaction : transactions) {
                addTableCell(table, dateFormat.format(transaction.getCreatedAt()));
                addTableCell(table, transaction.getType());
                addTableCell(table, "৳" + String.format("%.2f", transaction.getAmount()));
                addTableCell(table, "৳" + String.format("%.2f", transaction.getBalanceAfter()));
            }

            document.add(table);
        } else {
            document.add(new Paragraph("No transactions found.", NORMAL_FONT));
        }

        document.close();
        return file;
    }

    private static void addTableHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(text, HEADER_FONT));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(5);
        table.addCell(header);
    }

    private static void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
