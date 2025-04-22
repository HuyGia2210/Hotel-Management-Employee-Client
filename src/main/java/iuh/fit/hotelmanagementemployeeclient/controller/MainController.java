package iuh.fit.hotelmanagementemployeeclient.controller;

import iuh.fit.hotelmanagementemployeeclient.controller.features.DashboardController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.MenuController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.customer.CustomerManagerController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.customer.CustomerSearchingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.employee.EmployeeManagerController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.employee.EmployeeSearchingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.invoice.InvoiceManagerController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.RoomBookingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.RoomManagerController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.RoomSearchingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.service.HotelServiceManagerController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.service.HotelServiceSearchingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.TopController;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomManagementService;
import iuh.fit.models.*;
import iuh.fit.models.enums.Position;
import iuh.fit.hotelmanagementemployeeclient.utils.TimelineManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Locale;


@Getter
public class MainController {
    private Account account;

    @FXML
    private AnchorPane menuBar;
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private AnchorPane topPanel;
    private Stage mainStage;

    private Button informationBtn;

    private MenuController menuController;

    private static boolean ROOM_BOOKING_LOADED = true;

    // Không xóa
    public void initialize(Account account, Stage stage) {
        if (account == null) throw new IllegalArgumentException("Tài khoản không tồn tại");
        RoomManagementService.startAutoCheckoutScheduler(this);

        this.account = account;
        this.mainStage = stage;

        Locale locale = new Locale("vi", "VN");
        Locale.setDefault(locale);

        initializeTopBar();
        initializeDashboard();
        initializeMenuBar();
    }

    public void initializeDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/DashboardPanel.fxml"));
            AnchorPane dashboardLayout = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setupContext(account, this);

            mainPanel.getChildren().clear();
            mainPanel.getChildren().addAll(dashboardLayout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeMenuBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/MenuPanel.fxml"));
            AnchorPane menuLayout = loader.load();

            menuController = loader.getController();

            setupMenuButtons();

            menuBar.getChildren().clear();
            menuBar.getChildren().addAll(menuLayout.getChildren());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeTopBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/TopPanel.fxml"));
            AnchorPane topLayout = loader.load();

            TopController topController = loader.getController();
            topController.initialize(mainStage);

//            RoomManagementService.startAutoCheckoutScheduler(notificationButtonController, this);

            topPanel.getChildren().clear();
            topPanel.getChildren().addAll(topLayout.getChildren());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMenuButtons() {
        Position position = account.getEmployee().getPosition();
        informationBtn = menuController.getInformationBtn();
        handleTooltips();

        // Tắt các button menu không thuộc về lễ tân
        if (position.equals(Position.RECEPTIONIST)) {
            menuController.getEmployeeManagerButton().setDisable(true);
            menuController.getAccountOfEmployeeManagerButton().setDisable(true);
            menuController.getEmployeeSearchingButton().setDisable(true);
            menuController.getRoomCategoryManagerButton().setDisable(true);
            menuController.getRoomManagerButton().setDisable(true);
            menuController.getServiceCategoryManagerButton().setDisable(true);
            menuController.getHotelServiceManagerButton().setDisable(true);
        }

        // xử lý sự kiện hiện giao diện cho cả lễ tân và quản lý
        // Dashboard
        menuController.getDashBoardBtn().setOnAction(e -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/DashboardPanel.fxml", this, account));
        // Room
        menuController.getRoomSearchingButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomSearchingPanel.fxml", this, account));
        menuController.getRoomBookingButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomBookingPanel.fxml", this, account));
        // Invoice
        menuController.getInvoiceBtn().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/invoice/InvoiceManagerPanel.fxml", this, account));
        // Hotel Service
        menuController.getHotelServiceSearchingButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/service/HotelServiceSearchingPanel.fxml", this, account));
        // Customer
        menuController.getCustomerSearchingButton().setOnAction(e -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/customer/CustomerSearchingPanel.fxml", this, account));
        menuController.getCustomerManagerButton().setOnAction(e -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/customer/CustomerManagerPanel.fxml", this, account));

        // Employee Information
        menuController.getEmployeeInformationContainer().setOnMouseClicked(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/employee_information/EmployeeInformationPanel.fxml", this, account));

        // Thêm các sự kiện xử lý giao diện cho quản lý
        if (position.equals(Position.MANAGER)) {
            // Employee
            menuController.getEmployeeManagerButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/employee/EmployeeManagerPanel.fxml", this, account));
            menuController.getAccountOfEmployeeManagerButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/employee/AccountManagerPanel.fxml", this, account));
            menuController.getEmployeeSearchingButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/employee/EmployeeSearchingPanel.fxml", this, account));
            // Room
            menuController.getRoomCategoryManagerButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomCategoryManagerPanel.fxml", this, account));
            menuController.getRoomManagerButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomManagerPanel.fxml", this, account));
            // Hotel Service
            menuController.getServiceCategoryManagerButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/service/ServiceCategoryManagerPanel.fxml", this, account));
            menuController.getHotelServiceManagerButton().setOnAction(event -> loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/service/HotelServiceManagerPanel.fxml", this, account));
            // Statistics
            menuController.getRevenueStatisticsButton()
                    .setOnAction(e ->
                            loadPanel("/iuh/fit/hotelmanagementemployeeclient/view/features/statistics/RevenueStatisticalPanel.fxml",
                                    this,
                                    account)
                    );
        }
    }

    private void handleTooltips() {
        // Tạo Tooltip
        Tooltip tooltip = new Tooltip("Về phần mềm");
        Tooltip.install(informationBtn, tooltip); // Gắn Tooltip vào Button

        // Thêm Tooltip bằng cách setTooltip
        informationBtn.setTooltip(tooltip);
        tooltip.setShowDelay(javafx.util.Duration.millis(400));
    }

    public void loadPanel(String fxmlPath, MainController mainController, Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane layout = loader.load();

            Object controller = loader.getController();

            switch (controller) {
                case HotelServiceSearchingController hotelServiceSearchingController ->
                        hotelServiceSearchingController.setupContext(this, account);

                case CustomerSearchingController customerSearchingController ->
                        customerSearchingController.setupContext(this, account);


                case RoomBookingController roomBookingController ->
                        roomBookingController.setupContext(mainController, account.getEmployee());

                case InvoiceManagerController invoiceManagerController -> {
                    Employee employee = account.getEmployee();
                    invoiceManagerController.setupContext(mainController, employee);
                }

                case DashboardController dashboardController ->
                    dashboardController.setupContext(account, mainController);

                case EmployeeSearchingController employeeSearchingController ->
                        employeeSearchingController.setupContext(this);


                case RoomSearchingController roomSearchingController ->
                        roomSearchingController.setupContext(this, account);

                default -> {}
            }

            if (!fxmlPath.contains("RoomBookingPanel") && !fxmlPath.contains("DashBoardPanel")) {
                TimelineManager.getInstance().removeTimeline("REALTIME_DASHBOARD");
                TimelineManager.getInstance().stopAllTimelines();
            }

            ROOM_BOOKING_LOADED = fxmlPath.contains("RoomBookingPanel");

            mainPanel.getChildren().clear();
            mainPanel.getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPanelEmployeeManagerController(String fxmlPath, Employee emp){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane layout = loader.load();

            EmployeeManagerController controller = loader.getController();

            ROOM_BOOKING_LOADED = fxmlPath.contains("RoomBookingPanel");

            mainPanel.getChildren().clear();
            mainPanel.getChildren().addAll(layout.getChildren());

            Platform.runLater(() -> controller.setInformation(emp));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPanelRoomManagerController(String fxmlPath, Room room){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane layout = loader.load();

            RoomManagerController controller = loader.getController();

            ROOM_BOOKING_LOADED = fxmlPath.contains("RoomBookingPanel");

            mainPanel.getChildren().clear();
            mainPanel.getChildren().addAll(layout.getChildren());

            Platform.runLater(() -> controller.setInformation(room));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void loadPanelCreateReservationFormController(String fxmlPath, MainController mainController, Account account, RoomWithReservation room){
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//            AnchorPane layout = loader.load();
//
//            CreateReservationFormController controller = loader.getController();
//
//            ROOM_BOOKING_LOADED = fxmlPath.contains("RoomBookingPanel");
//
//            mainPanel.getChildren().clear();
//            mainPanel.getChildren().addAll(layout.getChildren());
//            MainController.setRoomBookingLoaded(false);
//            Platform.runLater(() -> controller.setupContext(
//                    mainController, account.getEmployee(), room,
//                    null, null, null
//            ));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void loadPanelHotelServiceManagerController(String fxmlPath, HotelService service){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane layout = loader.load();

            HotelServiceManagerController controller = loader.getController();

            ROOM_BOOKING_LOADED = fxmlPath.contains("RoomBookingPanel");
            mainPanel.getChildren().clear();
            mainPanel.getChildren().addAll(layout.getChildren());
            controller.setInformation(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPanelCustomerManagerController(String fxmlPath, Customer customer){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane layout = loader.load();

            CustomerManagerController controller = loader.getController();

            ROOM_BOOKING_LOADED = fxmlPath.contains("RoomBookingPanel");
            mainPanel.getChildren().clear();
            mainPanel.getChildren().addAll(layout.getChildren());
            Platform.runLater(() -> controller.setInformation(customer));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPanelInformation(String fxmlPath){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane layout = loader.load();

            mainPanel.getChildren().clear();
            mainPanel.getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isRoomBookingLoaded() {
        return ROOM_BOOKING_LOADED;
    }

    public static void setRoomBookingLoaded(boolean roomBookingLoaded) {
        ROOM_BOOKING_LOADED = roomBookingLoaded;
    }

}
