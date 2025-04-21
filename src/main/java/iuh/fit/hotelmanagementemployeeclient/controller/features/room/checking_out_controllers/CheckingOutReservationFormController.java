package iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_out_controllers;

import com.dlsc.gemsfx.DialogPane;
import iuh.fit.hotelmanagementemployeeclient.controller.MainController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.RoomBookingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_in_reservation_list_controllers.ReservationListController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers.CreateReservationFormController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.room_changing_controllers.RoomChangingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.service_ordering_controllers.ServiceOrderingController;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.*;
import iuh.fit.models.enums.RoomStatus;
import iuh.fit.models.wrapper.RoomWithReservation;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomChargesCalculate;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomManagementService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


public class CheckingOutReservationFormController {
    // ==================================================================================================================
    // 1. Các biến
    // ==================================================================================================================


    @FXML
    private Button backBtn, bookingRoomNavigateLabel;

    @FXML
    private Button navigateToCreateReservationFormBtn,
            navigateToReservationListBtn, navigateToRoomChangingBtn,
            navigateToServiceOrderingBtn;

    @FXML
    private Button checkOutBtn, checkOutEarlyBtn;

    @FXML
    private Label roomNumberLabel, roomCategoryLabel, checkInDateLabel,
            checkOutDateLabel, stayLengthLabel;

    @FXML
    private Label customerIDLabel, customerFullnameLabel,
            cusomerPhoneNumberLabel, customerIDCardNumberLabel;

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
    private TableView<ReservationRoomDetail> roomReservationDetailTableView;
    @FXML
    private TableColumn<ReservationRoomDetail, String> roomReservationDetailID;
    @FXML
    private TableColumn<ReservationRoomDetail, String> roomReservationDetailDateChanged;
    @FXML
    private TableColumn<ReservationRoomDetail, String> roomReservationDetailRoomNumber;
    @FXML
    private TableColumn<ReservationRoomDetail, String> roomReservationEmployeeFullname;

    @FXML
    private TitledPane titledPane;

    @FXML
    private DialogPane dialogPane;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm", Locale.forLanguageTag("vi-VN"));

    private MainController mainController;
    private RoomWithReservation roomWithReservation;
    private Employee employee;

    public CheckingOutReservationFormController() throws RemoteException {
    }

    // ==================================================================================================================
    // 2. Khởi tạo và nạp dữ liệu vào giao diện
    // ==================================================================================================================
    public void initialize() {
        dialogPane.toFront();
        setupRoomReservationDetailTableView();
        setupRoomUsageServiceTableView();
    }

    public void setupContext(MainController mainController, Employee employee,
                             RoomWithReservation roomWithReservation) throws RemoteException {
        this.mainController = mainController;
        this.employee = employee;
        this.roomWithReservation = roomWithReservation;
        titledPane.setText("Quản lý đặt phòng " + roomWithReservation.getRoom().getRoomNumber());

        setupReservationForm();
        setupButtonActions();
        loadData();
    }

    private void loadData() throws RemoteException {
        List<ReservationRoomDetail> roomReservationDetails = DAOProvider.dao.getReservationRoomDetailDAO().getByReservationFormID(
                roomWithReservation.getReservationForm().getReservationID()
        );
        ObservableList<ReservationRoomDetail> roomReservationDetailsData = FXCollections.observableArrayList(roomReservationDetails);
        roomReservationDetailTableView.setItems(roomReservationDetailsData);
        roomReservationDetailTableView.refresh();

        List<RoomUsageService> roomUsageServices = DAOProvider.dao.getRoomUsageServiceDAO().getByReservationFormID(roomWithReservation.getReservationForm().getReservationID());
        ObservableList<RoomUsageService> roomUsageServicesData = FXCollections.observableArrayList(roomUsageServices);
        roomUsageServiceTableView.setItems(roomUsageServicesData);
        roomUsageServiceTableView.refresh();
    }

    private void setupButtonActions() {
        // Label Navigate Button
        backBtn.setOnAction(e -> navigateToRoomBookingPanel());
        bookingRoomNavigateLabel.setOnAction(e -> navigateToRoomBookingPanel());

        // Box Navigate Button
        navigateToReservationListBtn.setOnAction(e -> navigateToReservationListPanel());
        navigateToCreateReservationFormBtn.setOnAction(e -> navigateToCreateReservationFormPanel());
        RoomStatus roomStatus = roomWithReservation.getRoom().getRoomStatus();

        switch (roomStatus) {
            case OVER_DUE -> {
                navigateToServiceOrderingBtn.setDisable(true);
                navigateToRoomChangingBtn.setDisable(true);
            }
            case IN_USE -> {
                navigateToRoomChangingBtn.setOnAction(e -> navigateToRoomChangingPanel());
                navigateToServiceOrderingBtn.setOnAction(e -> navigateToServiceOrderingPanel());
            }
        }
        navigateToRoomChangingBtn.setOnAction(e -> navigateToRoomChangingPanel());
        navigateToServiceOrderingBtn.setOnAction(e -> navigateToServiceOrderingPanel());

        // Current Panel Button
        if (!isOverdueTime()) {
            checkOutBtn.setDisable(true);
            checkOutEarlyBtn.setOnAction(e -> handleCheckOutEarly());
        } else {
            checkOutEarlyBtn.setDisable(true);
            checkOutBtn.setOnAction(e -> handleCheckOut());

        }
    }

    private void setupReservationForm() throws RemoteException {
        ReservationForm reservationForm = roomWithReservation.getReservationForm();

        Room reservationFormRoom = roomWithReservation.getRoom();
        Customer reservationFormCustomer = roomWithReservation.getReservationForm().getCustomer();

        LocalDateTime actualCheckInDate = DAOProvider.dao.getHistoryCheckInDAO().getActualCheckInDate(reservationForm.getReservationID());

        roomNumberLabel.setText(reservationFormRoom.getRoomNumber());
        roomCategoryLabel.setText(reservationFormRoom.getRoomCategory().getRoomCategoryName());
        checkInDateLabel.setText(dateTimeFormatter.format(actualCheckInDate != null ? actualCheckInDate : reservationForm.getApproxcheckOutTime()));
        checkOutDateLabel.setText(dateTimeFormatter.format(reservationForm.getApproxcheckOutTime()));
        stayLengthLabel.setText(RoomChargesCalculate.calculateStayLengthToString(
                reservationForm.getApproxcheckInDate(),
                reservationForm.getApproxcheckOutTime()
        ));
        customerIDLabel.setText(reservationFormCustomer.getCustomerCode());
        customerFullnameLabel.setText(reservationFormCustomer.getFullName());
        cusomerPhoneNumberLabel.setText(reservationFormCustomer.getPhoneNumber());
        customerIDCardNumberLabel.setText(reservationFormCustomer.getIdCardNumber());
    }

    // ==================================================================================================================
    // 3. Xử lý chức năng hiển thị panel khác
    // ==================================================================================================================
    private void navigateToRoomBookingPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomBookingPanel.fxml"));
            AnchorPane layout = loader.load();

            RoomBookingController roomBookingController = loader.getController();
            roomBookingController.setupContext(mainController, employee);


            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToCreateReservationFormPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/creating_reservation_form_panels/CreateReservationFormPanel.fxml"));
            AnchorPane layout = loader.load();

            CreateReservationFormController createReservationFormController = loader.getController();
            createReservationFormController.setupContext(
                    mainController, employee, roomWithReservation,
                    null,
                    null,
                    null
            );

            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToRoomChangingPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/changing_room_panels/RoomChangingPanel.fxml"));
            AnchorPane layout = loader.load();

            RoomChangingController roomChangingController = loader.getController();
            roomChangingController.setupContext(
                    mainController, employee, roomWithReservation
            );

            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToReservationListPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/checking_in_reservation_list_panels/ReservationListPanel.fxml"));
            AnchorPane layout = loader.load();

            ReservationListController reservationListController = loader.getController();
            reservationListController.setupContext(
                    mainController, employee, roomWithReservation
            );

            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToServiceOrderingPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/ordering_services_panels/ServiceOrderingPanel.fxml"));
            AnchorPane layout = loader.load();

            ServiceOrderingController serviceOrderingController = loader.getController();
            serviceOrderingController.setupContext(
                    mainController, employee, roomWithReservation
            );

            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================================================================================
    // 4.  Setup 2 table lịch sử dùng phòng và lịch sử dùng dịch vụ
    // ==================================================================================================================
    private void setupRoomReservationDetailTableView() {
        roomReservationDetailID.setCellValueFactory(new PropertyValueFactory<>("reservationRoomDetailID"));
        roomReservationDetailDateChanged.setCellValueFactory(data -> {
            LocalDateTime dateChanged = data.getValue().getDateChanged();
            String formattedDate = dateChanged != null ? dateChanged.format(dateTimeFormatter) : "KHÔNG CÓ";
            return new SimpleStringProperty(formattedDate);
        });
        roomReservationDetailRoomNumber.setCellValueFactory(data -> {
            Room room = data.getValue().getRoom();
            String roomNumber = (room != null && room.getRoomNumber() != null) ? room.getRoomNumber() : "KHÔNG CÓ";
            return new SimpleStringProperty(roomNumber);
        });
        roomReservationEmployeeFullname.setCellValueFactory(data -> {
            Employee employee = data.getValue().getReservationForm().getEmployee();
            String fullName = (employee != null && employee.getFullName() != null) ? employee.getFullName() : "KHÔNG CÓ";
            return new SimpleStringProperty(fullName);
        });

    }

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

    // ==================================================================================================================
    // 5. Xử lý sự kiện checkout
    // ==================================================================================================================
    private void handleCheckOut() {
        try {
            DialogPane.Dialog<ButtonType> confirmDialog = dialogPane.showConfirmation(
                    "XÁC NHẬN CHECK-OUT",
                    "Bạn có chắc chắn muốn thực hiện check-out cho phòng này không?"
            );

            confirmDialog.onClose(buttonType -> {
                if (buttonType == ButtonType.YES) {
                    try {
                        RoomManagementService.handleCheckOut(roomWithReservation, employee);

                        dialogPane.showInformation("THÀNH CÔNG", "Check-out và tạo hóa đơn thành công!");

                        navigateToRoomBookingPanel();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        dialogPane.showInformation("LỖI", ex.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            dialogPane.showInformation("LỖI", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại!");
        }
    }

    private void handleCheckOutEarly() {
        try {
            DialogPane.Dialog<ButtonType> confirmDialog = dialogPane.showConfirmation(
                    "XÁC NHẬN CHECK-OUT",
                    "Bạn có chắc chắn muốn thực hiện check-out SỚM cho phòng này không?"
            );

            confirmDialog.onClose(buttonType -> {
                if (buttonType == ButtonType.YES) {
                    try {
                        RoomManagementService.handleCheckoutEarly(roomWithReservation, employee);

                        dialogPane.showInformation("THÀNH CÔNG", "Check-out và tạo hóa đơn thành công!");

                        navigateToRoomBookingPanel();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        dialogPane.showInformation("LỖI", ex.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            dialogPane.showInformation("LỖI", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại!");
        }
    }

    // ==================================================================================================================
    // 6. Kiểm tra thời gian có phù hợp
    // ==================================================================================================================
    private boolean isOverdueTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkOutDate = roomWithReservation.getReservationForm().getApproxcheckOutTime();
        return now.isAfter(checkOutDate);
    }
}
