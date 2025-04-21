package iuh.fit.hotelmanagementemployeeclient.utils;

import iuh.fit.models.wrapper.InvoiceDisplayOnTable;
import iuh.fit.models.wrapper.RoomDisplayOnTable;
import iuh.fit.models.wrapper.ServiceDisplayOnTable;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ExportFileHelper {

    // Export Invoice Excel
    public static void exportInvoiceExcelFile(TableView<InvoiceDisplayOnTable> tableView, String filePath,
                                              int numOfInvoice, double totalMoney) throws IOException {
        createInvoiceExcelFile(tableView, filePath, numOfInvoice, totalMoney);
    }

    public static void createInvoiceExcelFile(TableView<InvoiceDisplayOnTable> tableView, String filePath,
                                              int numOfInvoice, double totalMoney) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Invoice Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        List<String> headers = tableView.getColumns().stream()
                .map(col -> col.getText())
                .toList();

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        // Create data rows
        List<InvoiceDisplayOnTable> items = tableView.getItems();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", new Locale("vi", "VN"));
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        for (int i = 0; i < items.size(); i++) {
            Row row = sheet.createRow(i + 1);
            InvoiceDisplayOnTable item = items.get(i);

            row.createCell(0).setCellValue(item.getInvoiceID());
            row.createCell(1).setCellValue(item.getCusName());
            row.createCell(2).setCellValue(item.getRoomID());
            row.createCell(3).setCellValue(item.getEmpName());
            row.createCell(4).setCellValue(item.getCreateDate().format(formatter));
            row.createCell(5).setCellValue(decimalFormat.format(item.getDeposit()));
            row.createCell(6).setCellValue(decimalFormat.format(item.getServiceCharge()));
            row.createCell(7).setCellValue(decimalFormat.format(item.getRoomCharge()));
            row.createCell(8).setCellValue(decimalFormat.format(item.getNetDue()));
        }

        // Create summary row
        Row summaryRow = sheet.createRow(items.size() + 2);
        summaryRow.createCell(0).setCellValue("Số bản ghi:");
        summaryRow.createCell(1).setCellValue(numOfInvoice);
        summaryRow.createCell(2).setCellValue("Tổng tiền:");
        summaryRow.createCell(3).setCellValue(decimalFormat.format(totalMoney));

        // Auto-size columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    // Export Service Excel
    public static void exportServiceExcelFile(TableView<ServiceDisplayOnTable> tableView, String filePath,
                                              int numOfInvoice, double totalMoney) throws IOException {
        createServiceExcelFile(tableView, filePath, numOfInvoice, totalMoney);
    }

    public static void createServiceExcelFile(TableView<ServiceDisplayOnTable> tableView, String filePath,
                                              int numOfInvoice, double totalMoney) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Service Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        List<String> headers = tableView.getColumns().stream()
                .map(col -> col.getText())
                .toList();

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        // Create data rows
        List<ServiceDisplayOnTable> items = tableView.getItems();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", new Locale("vi", "VN"));
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        for (int i = 0; i < items.size(); i++) {
            Row row = sheet.createRow(i + 1);
            ServiceDisplayOnTable item = items.get(i);

            row.createCell(0).setCellValue(item.getServiceId());
            row.createCell(1).setCellValue(item.getServiceName());
            row.createCell(2).setCellValue(item.getServiceCategory());
            row.createCell(3).setCellValue(item.getEmployeeName());
            row.createCell(4).setCellValue(item.getDateAdded().format(formatter));
            row.createCell(5).setCellValue(item.getQuantity());
            row.createCell(6).setCellValue(decimalFormat.format(item.getUnitPrice()));
            row.createCell(7).setCellValue(decimalFormat.format(item.getTotalMoney()));
        }

        // Create summary row
        Row summaryRow = sheet.createRow(items.size() + 2);
        summaryRow.createCell(0).setCellValue("Số bản ghi:");
        summaryRow.createCell(1).setCellValue(numOfInvoice);
        summaryRow.createCell(2).setCellValue("Tổng tiền:");
        summaryRow.createCell(3).setCellValue(decimalFormat.format(totalMoney));

        // Auto-size columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    // Export Room Excel
    public static void exportRoomExcelFile(TableView<RoomDisplayOnTable> tableView, String filePath,
                                           int numOfInvoice, double totalMoney) throws IOException {
        createRoomExcelFile(tableView, filePath, numOfInvoice, totalMoney);
    }

    public static void createRoomExcelFile(TableView<RoomDisplayOnTable> tableView, String filePath,
                                           int numOfInvoice, double totalMoney) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Room Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        List<String> headers = tableView.getColumns().stream()
                .map(col -> col.getText())
                .toList();

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        // Create data rows
        List<RoomDisplayOnTable> items = tableView.getItems();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", new Locale("vi", "VN"));
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        for (int i = 0; i < items.size(); i++) {
            Row row = sheet.createRow(i + 1);
            RoomDisplayOnTable item = items.get(i);

            row.createCell(0).setCellValue(item.getRoomID());
            row.createCell(1).setCellValue(item.getRoomCategory());
            row.createCell(2).setCellValue(item.getNumOfPeople());
            row.createCell(3).setCellValue(item.getBookingDate().format(formatter));
            row.createCell(4).setCellValue(item.getCheckInDate().format(formatter));
            row.createCell(5).setCellValue(item.getCheckOutDate().format(formatter));
            row.createCell(6).setCellValue(decimalFormat.format(item.getTotalMoney()));
        }

        // Create summary row
        Row summaryRow = sheet.createRow(items.size() + 2);
        summaryRow.createCell(0).setCellValue("Số bản ghi:");
        summaryRow.createCell(1).setCellValue(numOfInvoice);
        summaryRow.createCell(2).setCellValue("Tổng tiền:");
        summaryRow.createCell(3).setCellValue(decimalFormat.format(totalMoney));

        // Auto-size columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public static void openExcelFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists() && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        } else {
            throw new IOException("File không tồn tại hoặc không thể mở.");
        }
    }
}