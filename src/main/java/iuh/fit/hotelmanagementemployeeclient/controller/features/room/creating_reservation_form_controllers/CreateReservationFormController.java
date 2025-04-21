package iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.TimePicker;
import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.daterange.DateRangePreset;
import iuh.fit.hotelmanagementemployeeclient.controller.MainController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.RoomBookingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_in_reservation_list_controllers.ReservationListController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_out_controllers.CheckingOutReservationFormController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.room_changing_controllers.RoomChangingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.service_ordering_controllers.ServiceOrderingController;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.Customer;
import iuh.fit.models.Employee;
import iuh.fit.models.ReservationForm;
import iuh.fit.models.Room;
import iuh.fit.models.enums.RoomStatus;
import iuh.fit.models.wrapper.RoomWithReservation;
import iuh.fit.hotelmanagementemployeeclient.utils.ErrorMessages;
import iuh.fit.hotelmanagementemployeeclient.utils.GlobalConstants;
import iuh.fit.hotelmanagementemployeeclient.utils.RegexChecker;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomChargesCalculate;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

public class CreateReservationFormController {
    // ==================================================================================================================
    // 1. Các biến
    // ==================================================================================================================

    @FXML private Button backBtn, bookingRoomNavigateLabel;

    @FXML
    private Button navigateToReservationListBtn, navigateToServiceOrderingBtn,
            navigateToRoomChangingBtn, navigateToRoomCheckingOutBtn;

    @FXML private Button addBtn, reservationCheckDateBtn;

    @FXML private Label roomNumberLabel, categoryNameLabel;
    @FXML private DateRangePicker bookDateRangePicker;
    @FXML private TimePicker checkInTimePicker, checkOutTimePicker;
    @FXML private TextField checkInDateTextField, checkOutDateTextField;
    @FXML private Label stayLengthLabel, bookingDepositLabel;

    @FXML private TextField customerIDCardNumberTextField, customerFullnameTextField,
            customerPhoneNumberTextField;

    @FXML private Label employeeIDLabel, employeeFullNameLabel,
            employeePositionLabel, employeePhoneNumberLabel;

    @FXML private DialogPane dialogPane;
    @FXML private TitledPane titledPane;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.forLanguageTag("vi-VN"));

    private MainController mainController;
    private Employee employee;
    private RoomWithReservation roomWithReservation;
    private Room room;

    private LocalDateTime checkInTime, checkOutTime;
    private Customer customer;

    public CreateReservationFormController() throws RemoteException {
    }


    // ==================================================================================================================
    // 2. Khởi tạo và nạp dữ liệu vào giao diện
    // ==================================================================================================================
    public void initialize() {
        dialogPane.toFront();
        setupTimeComponents();
        setupCustomerIDCardValidation();
    }

    public void setupContext(MainController mainController, Employee employee,
                             RoomWithReservation roomWithReservation, Customer customer,
                             LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.mainController = mainController;
        this.employee = employee;
        this.roomWithReservation = roomWithReservation;
        this.room = roomWithReservation.getRoom();

        titledPane.setText("Quản lý đặt phòng " + room.getRoomNumber());

        setupRoomInformation();
        setupEmployeeInformation();

        if (customer != null) customerIDCardNumberTextField.setText(customer.getIdCardNumber());
        if (checkInTime != null && checkOutTime != null) {
            this.checkInTime = checkInTime;
            this.checkOutTime = checkOutTime;
            setBookingDates(checkInTime, checkOutTime);
        }

        setupButtonActions();
    }

    private void setupButtonActions() {
        // Label Navigate Button
        backBtn.setOnAction(e -> navigateToRoomBookingPanel());
        bookingRoomNavigateLabel.setOnAction(e -> navigateToRoomBookingPanel());

        // Box Navigate Button
        navigateToReservationListBtn.setOnAction(e -> navigateToReservationListPanel());
        RoomStatus roomStatus = room.getRoomStatus();

        switch (roomStatus) {
            case AVAILABLE -> {
                navigateToServiceOrderingBtn.setDisable(true);
                navigateToRoomChangingBtn.setDisable(true);
                navigateToRoomCheckingOutBtn.setDisable(true);
            }
            case OVER_DUE -> {
                navigateToRoomCheckingOutBtn.setOnAction(e -> navigateToCheckingOutReservationFormPanel());
                navigateToServiceOrderingBtn.setDisable(true);
                navigateToRoomChangingBtn.setDisable(true);
            }
            case IN_USE -> {
                navigateToRoomChangingBtn.setOnAction(e -> navigateToRoomChangingPanel());
                navigateToServiceOrderingBtn.setOnAction(e -> navigateToServiceOrderingPanel());
                navigateToRoomCheckingOutBtn.setOnAction(e -> navigateToCheckingOutReservationFormPanel());
            }
        }

        // Current Panel Button
        addBtn.setOnAction(e -> handleCreateReservationRoom());
        reservationCheckDateBtn.setOnAction(e -> {
            try {
                openCalendarViewStage();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    // ==================================================================================================================
    // 3. Hiển thị thông tin phòng và nhân viên
    // ==================================================================================================================
    private void setupRoomInformation() {
        roomNumberLabel.setText(room.getRoomNumber());
        categoryNameLabel.setText(room.getRoomCategory().getRoomCategoryName());
    }

    private void setupEmployeeInformation() {
        employeeIDLabel.setText(employee.getEmployeeCode());
        employeeFullNameLabel.setText(employee.getFullName());
        employeePositionLabel.setText(employee.getPosition().toString());
        employeePhoneNumberLabel.setText(employee.getPhoneNumber());
    }

    // ==================================================================================================================
    // 4. Xử lý chức năng hiển thị panel khác
    // ==================================================================================================================
    private void navigateToRoomBookingPanel() {
        loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomBookingPanel.fxml", RoomBookingController.class);
    }

    private void navigateToReservationListPanel() {
        loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/checking_in_reservation_list_panels/ReservationListPanel.fxml", ReservationListController.class);
    }

    private void navigateToRoomChangingPanel() {
        loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/changing_room_panels/RoomChangingPanel.fxml", RoomChangingController.class);
    }

    private void navigateToServiceOrderingPanel() {
        loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/ordering_services_panels/ServiceOrderingPanel.fxml", ServiceOrderingController.class);
    }

    private void navigateToCheckingOutReservationFormPanel() {
        loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/checking_out_panels/CheckingOutReservationFormPanel.fxml", CheckingOutReservationFormController.class);
    }

    private <T> void loadPanel(String path, Class<T> ignoredControllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            AnchorPane layout = loader.load();
            T controller = loader.getController();

            if (controller instanceof RoomBookingController rbc)
                rbc.setupContext(mainController, employee);
            else if (controller instanceof  ReservationListController rlc)
                rlc.setupContext(mainController, employee, roomWithReservation);
            else if (controller instanceof RoomChangingController rcc)
                rcc.setupContext(mainController, employee, roomWithReservation);
            else if (controller instanceof ServiceOrderingController soc)
                soc.setupContext(mainController, employee, roomWithReservation);
            else if (controller instanceof CheckingOutReservationFormController corfc)
                corfc.setupContext(mainController, employee, roomWithReservation);

            mainController.getMainPanel().getChildren().setAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================================================================================
    // 5. Cài đặt các thành phần giao diện liên quan đến thời gian
    // ==================================================================================================================
    private void setupTimeComponents() {
        checkInTimePicker.setTime(null);
        checkInTimePicker.setStepRateInMinutes(5);
        checkOutTimePicker.setTime(null);
        checkOutTimePicker.setStepRateInMinutes(5);

        bookDateRangePicker.setValue(new DateRange("Chọn Lịch Đặt Phòng", LocalDate.now(), LocalDate.now().plusDays(7)));
        bookDateRangePicker.setFormatter(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));

        bookDateRangePicker.getDateRangeView().presetTitleProperty().set("Chọn Lịch Đặt Phòng");
        bookDateRangePicker.getDateRangeView().toTextProperty().set("Đến");
        bookDateRangePicker.getDateRangeView().cancelTextProperty().set("Hủy");
        bookDateRangePicker.getDateRangeView().applyTextProperty().set("Áp dụng");

        bookDateRangePicker.getDateRangeView().getPresets().setAll(
                createThreeDaysPreset(),
                createFiveDaysPreset(),
                createSevenDaysPreset()
        );

        ObjectProperty<DateRange> selectedRangeProperty = bookDateRangePicker.valueProperty();

        checkInDateTextField.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    DateRange range = bookDateRangePicker.getValue();
                    LocalTime checkInTime = checkInTimePicker.getTime();
                    if (range != null && checkInTime != null) {
                        return formatDateTime(range.getStartDate(), checkInTime);
                    }
                    return "";
                }, selectedRangeProperty, checkInTimePicker.timeProperty())
        );

        checkOutDateTextField.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    DateRange range = bookDateRangePicker.getValue();
                    LocalTime checkOutTime = checkOutTimePicker.getTime();
                    if (range != null && checkOutTime != null) {
                        return formatDateTime(range.getEndDate(), checkOutTime);
                    }
                    return "";
                }, selectedRangeProperty, checkOutTimePicker.timeProperty())
        );

        setupDateTimeListeners();
    }

    private void setupDateTimeListeners() {
        checkInDateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                checkInTime = parseDateTime(newValue);
                updateStayLengthAndCost();
            }
        });

        checkOutDateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                checkOutTime = parseDateTime(newValue);
                updateStayLengthAndCost();
            }
        });
    }

    private String formatDateTime(LocalDate date, LocalTime time) {
        return dateTimeFormatter.format(date) + " " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.forLanguageTag("vi-VN")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private DateRangePreset createThreeDaysPreset() {
        return new DateRangePreset("3 Ngày", () -> new DateRange("Chọn Lịch Đặt Phòng", LocalDate.now(), LocalDate.now().plusDays(3)));
    }

    private DateRangePreset createFiveDaysPreset() {
        return new DateRangePreset("5 Ngày", () -> new DateRange("Chọn Lịch Đặt Phòng", LocalDate.now(), LocalDate.now().plusDays(5)));
    }

    private DateRangePreset createSevenDaysPreset() {
        return new DateRangePreset("7 Ngày", () -> new DateRange("Chọn Lịch Đặt Phòng", LocalDate.now(), LocalDate.now().plusDays(7)));
    }

    // ==================================================================================================================
    // 6. Tính toán thời gian lưu trú và tiền đặt cọc
    // ==================================================================================================================
    private void updateStayLengthAndCost() {
        if (checkInTime != null && checkOutTime != null) {
            String stayLength = RoomChargesCalculate.calculateStayLengthToString(checkInTime, checkOutTime);
            stayLengthLabel.setText(stayLength);

            try {
                double totalCost = RoomChargesCalculate.calculateRoomCharges(checkInTime, checkOutTime, room) * 0.1;
                bookingDepositLabel.setText(String.format("%.2f VND", totalCost));
            } catch (IllegalArgumentException e) {
                bookingDepositLabel.setText(e.getMessage());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            stayLengthLabel.setText(GlobalConstants.STAY_LENGTH_EMPTY);
            bookingDepositLabel.setText(GlobalConstants.BOOKING_DEPOSIT_EMPTY);
        }
    }

    // ==================================================================================================================
    // 7. Cài đặt các thành phần giao diện liên quan đến Khách Hàng
    // ==================================================================================================================
    private void setupCustomerIDCardValidation() {
        customerIDCardNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) clearCustomerInfo();
            else if (newValue.length() > 12) {
                handleInputExceedsLimit(oldValue);
            } else if (newValue.length() == 12) {
                try {
                    validateIDCardNumber(newValue);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else if (customer != null) {
                clearCustomerInfo();
            }
        });
    }

    private void handleInputExceedsLimit(String oldValue) {
        Platform.runLater(() -> customerIDCardNumberTextField.setText(oldValue));
        dialogPane.showError("LỖI", ErrorMessages.ID_CARD_NUMBER_OVER_LIMIT);
    }

    private void validateIDCardNumber(String idCardNumber) throws RemoteException {
        if (!RegexChecker.isValidIDCardNumber(idCardNumber)) {
            dialogPane.showError("LỖI", ErrorMessages.INVALID_ID_CARD_NUMBER);
        } else {
            customer = DAOProvider.dao.getCustomerDAO().getDataByIDCardNumber(idCardNumber);
            if (customer == null) {
                dialogPane.showError("LỖI", ErrorMessages.CUS_NOT_FOUND);
                clearCustomerInfo();
            } else {
                setCustomerInfo();
            }
        }
    }

    private void clearCustomerInfo() {
        customer = null;
        customerFullnameTextField.clear();
        customerPhoneNumberTextField.clear();
        dialogPane.requestFocus();
    }

    private void setCustomerInfo() {
        customerFullnameTextField.setText(customer.getFullName());
        customerPhoneNumberTextField.setText(customer.getPhoneNumber());
    }

    // ==================================================================================================================
    // 7. Đẩy thời gian lên giao diện nếu checkInDate và checkOutDate không NULL
    // ==================================================================================================================
    private void setBookingDates(LocalDateTime checkInDate, LocalDateTime checkOutDate) {
            bookDateRangePicker.setValue(new DateRange("Chọn Lịch Đặt Phòng",
                    checkInDate.toLocalDate(), checkOutDate.toLocalDate()));

            checkInTimePicker.setTime(checkInDate.toLocalTime());
            checkOutTimePicker.setTime(checkOutDate.toLocalTime());
    }

    // ==================================================================================================================
    // 8. Đẩy thời gian lên giao diện nếu checkInDate và checkOutDate không NULL
    // ==================================================================================================================
    private void handleCreateReservationRoom() {
        try {
            if (checkInTime == null)
                throw new IllegalArgumentException(ErrorMessages.RESERVATION_FORM_INVALID_CHECKIN_DATE_ISNULL);

            if (checkOutTime == null)
                throw new IllegalArgumentException(ErrorMessages.RESERVATION_FORM_INVALID_CHECKOUT_DATE_ISNULL);

            if (RoomChargesCalculate.isStayDurationZeroOrNegative(checkInTime, checkOutTime))
                throw new IllegalArgumentException(ErrorMessages.RESERVATION_FORM_STAY_LENGTH_INVALID);

            if (customer == null)
                throw new IllegalArgumentException(ErrorMessages.CUS_NOT_FOUND);

            ReservationForm reservationForm = new ReservationForm(
                    LocalDateTime.now(), checkInTime, checkOutTime,
                    roomWithReservation.getRoom(), customer, employee
            );

            reservationForm.setBookingDeposit(
                    RoomChargesCalculate.calculateRoomCharges(checkInTime, checkOutTime, roomWithReservation.getRoom()) * 0.1
            );

            String result = DAOProvider.dao.getReservationFormDAO().createReservationForm(reservationForm);

            switch (result) {
                case "CREATING_RESERVATION_FORM_SUCCESS" -> {
                    dialogPane.showInformation("Thành công", "Đã thêm phiếu đặt phòng thành công");
                    handleResetAction();
                }
                case "CREATING_RESERVATION_FORM_CHECK_DATE_OVERLAP" ->
                        dialogPane.showWarning("Lỗi", ErrorMessages.CREATING_RESERVATION_FORM_CHECK_DATE_OVERLAP);
                case "CREATING_RESERVATION_FORM_ID_CARD_NUMBER_OVERLAP" ->
                        dialogPane.showWarning("Lỗi", ErrorMessages.CREATING_RESERVATION_FORM_ID_CARD_NUMBER_OVERLAP);
                default ->
                        dialogPane.showWarning("Lỗi", result);
            }
        } catch (Exception e) {
            dialogPane.showWarning("Lỗi", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleResetAction() {
        try {
            // Unbind các TextField trước khi reset giá trị
            checkInDateTextField.textProperty().unbind();
            checkOutDateTextField.textProperty().unbind();

            bookDateRangePicker.setValue(null);
            checkInTimePicker.setTime(null);
            checkOutTimePicker.setTime(null);
            customerIDCardNumberTextField.setText("");
            checkInDateTextField.setText("");
            checkOutDateTextField.setText("");
            stayLengthLabel.setText("Chưa Đặt Lịch");
            bookingDepositLabel.setText("0 VND");

            customer = null;
            checkOutTime = null;
            checkInTime = null;

            setupTimeComponents();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================================================================================
    // 9. Chức năng xem lịch phòng
    // ==================================================================================================================
    private void openCalendarViewStage() throws RemoteException {
        CalendarView calendarView = new CalendarView();
        List<ReservationForm> reservations = DAOProvider.dao.getReservationFormDAO().getReservationsWithinLastMonth(room.getRoomID());
        Calendar<String> calendar = new Calendar<>("Lịch Đặt Phòng");

        reservations.forEach(res -> {
            Entry<String> entry = new Entry<>(res.getReservationID());
            entry.changeStartDate(res.getApproxcheckInDate().toLocalDate());
            entry.changeEndDate(res.getApproxcheckOutTime().toLocalDate());
            calendar.addEntry(entry);
        });

        calendarView.getCalendarSources().add(new com.calendarfx.model.CalendarSource("Nguồn") {{
            getCalendars().add(calendar);
        }});

        Stage stage = new Stage();
        stage.setScene(new Scene(calendarView, 800, 800));
        stage.show();
    }




}
