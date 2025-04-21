package iuh.fit.hotelmanagementemployeeclient.controller.features.invoice;

import com.dlsc.gemsfx.DialogPane;
import iuh.fit.hotelmanagementemployeeclient.controller.MainController;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.*;
import iuh.fit.hotelmanagementemployeeclient.utils.PDFHelper;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomChargesCalculate;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class InvoiceDetailsController {

    @FXML
    private Button backBtn, invoiceManagerNavigateBtn, exportPDFBtn, printPDFBtn;

    @FXML
    private Label roomNumberLabel, roomCategoryLabel, checkInDateLabel,
            checkOutDateLabel, stayLengthLabel;

    @FXML
    private Label customerIDLabel, customerFullnameLabel,
            cusomerPhoneNumberLabel, customerIDCardNumberLabel;

    @FXML
    private Text totalServiceChargeText, totalRoomChargeText,
            totalRoomDepositeText, totalDueText, taxText,
            netDueText, invoiceTitleText, remaningDueText;

    @FXML
    private TableView<RoomUsageService> roomUsageServiceTableView;
    @FXML
    private TableColumn<RoomUsageService, String> roomUsageServiceIDColumn;
    @FXML
    private TableColumn<RoomUsageService, String> serviceNameColumn;
    @FXML
    private TableColumn<RoomUsageService, Integer> quantityColumn;
    @FXML
    private TableColumn<RoomUsageService, Double> unitPriceColumn;
    @FXML
    private TableColumn<RoomUsageService, Double> totalPriceColumn;
    @FXML
    private TableColumn<RoomUsageService, String> dateAddedColumn;
    @FXML
    private TableColumn<RoomUsageService, String> employeeAddedColumn;

    @FXML
    private TitledPane titledPane;

    @FXML
    private DialogPane dialogPane;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm", Locale.forLanguageTag("vi-VN"));

    // Context
    private MainController mainController;
    private Employee employee;
    private Invoice invoice;

    public InvoiceDetailsController() throws RemoteException {
    }

    // ==================================================================================================================
    // 2. Khởi tạo và nạp dữ liệu vào giao diện
    // ==================================================================================================================
    public void initialize() {
        dialogPane.toFront();
        setupRoomUsageServiceTableView();
    }

    public void setupContext(MainController mainController, Employee employee,
                             Invoice invoice) throws RemoteException {
        this.mainController = mainController;
        this.employee = employee;
        this.invoice = invoice;

        titledPane.setText("Quản Lý Hóa Đơn " + invoice.getInvoiceID());

        setupButtonActions();
        setupReservationForm();
        loadData();
        setupPaymentSummary();
    }

    private void loadData() throws RemoteException {
        List<RoomUsageService> roomUsageServices = DAOProvider.dao.getRoomUsageServiceDAO().getByReservationFormID(invoice.getReservationForm().getReservationID());
        ObservableList<RoomUsageService> roomUsageServicesData = FXCollections.observableArrayList(roomUsageServices);
        roomUsageServiceTableView.setItems(roomUsageServicesData);
        roomUsageServiceTableView.refresh();
    }

    private void setupButtonActions() {
        // Label Navigate Button
        invoiceManagerNavigateBtn.setOnAction(e -> navigateToInvoiceManagerPanel());
        backBtn.setOnAction(e -> navigateToInvoiceManagerPanel());

        // Current Panel Button
        exportPDFBtn.setOnAction(e -> {
            try {
                PDFHelper.createAndOpenInvoicePDF(invoice);
            } catch (Exception exception) {
                dialogPane.showWarning("LỖI", exception.getMessage());
            }
        });

        printPDFBtn.setOnAction(e -> {
            try {
                PDFHelper.createAndPrintInvoicePDF(invoice);
            } catch (Exception exception) {
                dialogPane.showWarning("LỖI", exception.getMessage());
            }
        });


    }

    private void setupReservationForm() throws RemoteException {
        ReservationForm reservationForm = invoice.getReservationForm();

        Room reservationFormRoom = reservationForm.getRoom();
        Customer reservationFormCustomer = reservationForm.getCustomer();

        LocalDateTime actualCheckInDate = DAOProvider.dao.getHistoryCheckInDAO().getActualCheckInDate(reservationForm.getReservationID());
        LocalDateTime actualCheckOutDate = DAOProvider.dao.getHistoryCheckOutDAO().getActualCheckOutDate(reservationForm.getReservationID());

        roomNumberLabel.setText(reservationFormRoom.getRoomNumber());
        roomCategoryLabel.setText(reservationFormRoom.getRoomCategory().getRoomCategoryName());
        checkInDateLabel.setText(dateTimeFormatter.format(actualCheckInDate != null ? actualCheckInDate : reservationForm.getApproxcheckInDate()));
        checkOutDateLabel.setText(dateTimeFormatter.format(actualCheckOutDate != null ? actualCheckOutDate : reservationForm.getApproxcheckOutTime()));
        stayLengthLabel.setText(RoomChargesCalculate.calculateStayLengthToString(
                reservationForm.getApproxcheckInDate(),
                reservationForm.getApproxcheckOutTime()
        ));
        customerIDLabel.setText(reservationFormCustomer.getCustomerCode());
        customerFullnameLabel.setText(reservationFormCustomer.getFullName());
        cusomerPhoneNumberLabel.setText(reservationFormCustomer.getPhoneNumber());
        customerIDCardNumberLabel.setText(reservationFormCustomer.getIdCardNumber());
    }

    private void setupPaymentSummary() {
        totalServiceChargeText.setText(String.format("%,.0f", invoice.getServiceCharges()));
        totalRoomChargeText.setText(String.format("%,.0f", invoice.getRoomCharges()));
        double depositAmount = invoice.getReservationForm().getBookingDeposit();
        totalRoomDepositeText.setText("-" + String.format("%,.0f", depositAmount));
        totalDueText.setText(String.format("%,.0f", invoice.getSubTotal()));
        double taxAmount = invoice.getTotalDue() * 0.1;
        invoiceTitleText.setText("Thuế (10%)" );
        taxText.setText(String.format("%,.0f", taxAmount));
        netDueText.setText(String.format("%,.0f", invoice.getTotalDue()));
        remaningDueText.setText(String.format("%,.0f", invoice.getTotalDue() - depositAmount));
    }

    // ==================================================================================================================
    // 2. Xử lý chức năng hiển thị panel khác
    // ==================================================================================================================
    private void navigateToInvoiceManagerPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/invoice/InvoiceManagerPanel.fxml"));
            AnchorPane layout = loader.load();

            InvoiceManagerController invoiceManagerController = loader.getController();
            invoiceManagerController.setupContext(mainController, employee);


            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================================================================================
    // 3. Setup table lịch sử dùng dịch vụ
    // ==================================================================================================================
    private void setupRoomUsageServiceTableView() {
        roomUsageServiceIDColumn.setCellValueFactory(new PropertyValueFactory<>("roomUsageServiceID"));
        serviceNameColumn.setCellValueFactory(data -> {
            HotelService service = data.getValue().getHotelService();
            String serviceName = (service != null && service.getServiceName() != null) ? service.getServiceName() : "KHÔNG CÓ";
            return new SimpleStringProperty(serviceName);
        });
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        totalPriceColumn.setCellValueFactory(data -> {
            double totalPrice = data.getValue().getQuantity() * data.getValue().getUnitPrice();
            return new SimpleDoubleProperty(totalPrice).asObject();
        });
        dateAddedColumn.setCellValueFactory(data -> {
            LocalDateTime dateAdded = data.getValue().getDayAdded();
            String formattedDate = (dateAdded != null) ? dateAdded.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "Không có";
            return new SimpleStringProperty(formattedDate);
        });

        employeeAddedColumn.setCellValueFactory(data -> {
            Employee employee = data.getValue().getReservationForm().getEmployee();
            String employeeName = (employee != null && employee.getFullName() != null) ? employee.getFullName() : "Không có";
            return new SimpleStringProperty(employeeName);
        });
    }
}
