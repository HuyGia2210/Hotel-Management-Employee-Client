package iuh.fit.hotelmanagementemployeeclient.controller.features.room.service_ordering_controllers;

import com.dlsc.gemsfx.DialogPane;
import iuh.fit.hotelmanagementemployeeclient.controller.MainController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.RoomBookingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_in_reservation_list_controllers.ReservationListController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_out_controllers.CheckingOutReservationFormController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers.CreateReservationFormController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.room_changing_controllers.RoomChangingController;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.*;
import iuh.fit.models.wrapper.RoomWithReservation;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomChargesCalculate;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceOrderingController {
    // ==================================================================================================================
    // 1. Các biến
    // ==================================================================================================================

    @FXML
    private Button backBtn, bookingRoomNavigateLabel;

    @FXML
    private Button navigateToCreateReservationFormBtn,
            navigateToReservationListBtn, navigateToRoomChangingBtn,
            navigateToRoomCheckingOutBtn;


    @FXML
    private Label roomNumberLabel, roomCategoryLabel,
            checkInDateLabel, checkOutDateLabel,
            stayLengthLabel;

    @FXML
    private Label customerIDLabel, customerFullnameLabel,
            cusomerPhoneNumberLabel, customerIDCardNumberLabel;

    @FXML
    private ComboBox<String> serviceCategoryCBox;


    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm", Locale.forLanguageTag("vi-VN"));


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
    private DialogPane dialogPane;

    @FXML
    private TitledPane titledPane;

    @FXML
    private HBox emptyLabelContainer;
    @FXML
    private VBox serviceListContainer;
    @FXML
    private GridPane serviceGridPane;

    private MainController mainController;
    private RoomWithReservation roomWithReservation;
    private Employee employee;

    private List<HotelService> hotelServiceList;

    public ServiceOrderingController() throws RemoteException {
    }


    // ==================================================================================================================
    // 2. Khởi tạo và nạp dữ liệu vào giao diện
    // ==================================================================================================================
    public void initialize() {
        dialogPane.toFront();
        setupRoomUsageServiceTableView();
    }

    public void setupContext(MainController mainController, Employee employee,
                             RoomWithReservation roomWithReservation) throws RemoteException {
        this.mainController = mainController;
        this.employee = employee;
        this.roomWithReservation = roomWithReservation;

        titledPane.setText("Quản lý đặt phòng " + roomWithReservation.getRoom().getRoomNumber());

        if (handleValidTime()) {
            Platform.runLater(() -> navigateToRoomBookingPanel(true));
            return;
        }


        setupReservationForm();
        setupButtonActions();
        loadData();
    }

    private void setupButtonActions() {
        // Label Navigate Button
        backBtn.setOnAction(e -> navigateToRoomBookingPanel(false));
        bookingRoomNavigateLabel.setOnAction(e -> navigateToRoomBookingPanel(false));

        // Box Navigate Button
        navigateToReservationListBtn.setOnAction(e -> navigateToReservationListPanel());
        navigateToCreateReservationFormBtn.setOnAction(e -> navigateToCreateReservationFormPanel());
        navigateToRoomChangingBtn.setOnAction(e -> navigateToRoomChangingPanel());
        navigateToRoomCheckingOutBtn.setOnAction(e -> navigateToCheckingOutReservationFormPanel());

        // Current Panel Button
        serviceCategoryCBox.setOnAction(e -> filterServicesByCategory());
    }

    private void loadData() {
        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws RemoteException {
                hotelServiceList = DAOProvider.dao.getHotelServiceDAO().getHotelService();

                loadTable();

                ArrayList<String> categoryNames = (ArrayList<String>) DAOProvider.dao.getServiceCategoryDAO().getServiceCategoryNames();
                categoryNames.addFirst("TẤT CẢ");

                Platform.runLater(() -> {
                    serviceCategoryCBox.getItems().setAll(categoryNames);
                    serviceCategoryCBox.getSelectionModel().selectFirst();
                });

                Platform.runLater(() -> filterServicesByCategory());

                return null;
            }
        };

        loadDataTask.setOnFailed(e -> {
            dialogPane.showWarning("LỖI", "Lỗi khi tải dữ liệu");
            loadDataTask.getException().printStackTrace();
        });

        loadDataTask.setOnSucceeded(e -> displayServices(hotelServiceList));

        new Thread(loadDataTask).start();
    }

    private void loadTable() throws RemoteException {
        List<RoomUsageService> roomUsageServices = DAOProvider.dao.getRoomUsageServiceDAO().getByReservationFormID(roomWithReservation.getReservationForm().getReservationID());
        ObservableList<RoomUsageService> data = FXCollections.observableArrayList(roomUsageServices);

        Platform.runLater(() -> {
            roomUsageServiceTableView.setItems(data);
            roomUsageServiceTableView.refresh();
        });
    }


    // ==================================================================================================================
    // 3. Xử lý chức năng hiển thị panel khác
    // ==================================================================================================================
    private void navigateToRoomBookingPanel(boolean isError) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomBookingPanel.fxml"));
            AnchorPane layout = loader.load();

            RoomBookingController roomBookingController = loader.getController();
            roomBookingController.setupContext(mainController, employee);
            if (isError)
                roomBookingController
                        .getDialogPane()
                        .showInformation("LỖI", "Thời gian lưu trú đã kết thúc. Không thể thêm dịch vụ.");

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

    private void navigateToCheckingOutReservationFormPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/checking_out_panels/CheckingOutReservationFormPanel.fxml"));
            AnchorPane layout = loader.load();

            CheckingOutReservationFormController checkingOutReservationFormController = loader.getController();
            checkingOutReservationFormController.setupContext(
                    mainController, employee, roomWithReservation
            );

            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================================================================================
    // 4.  Đẩy dữ liệu lên giao diện
    // ==================================================================================================================
    private void setupReservationForm() throws RemoteException {
        ReservationForm reservationForm = roomWithReservation.getReservationForm();

        Room reservationFormRoom = roomWithReservation.getRoom();
        Customer reservationFormCustomer = roomWithReservation.getReservationForm().getCustomer();

        LocalDateTime actualCheckInDate = DAOProvider.dao.getHistoryCheckInDAO().getActualCheckInDate(reservationForm.getReservationID());

        roomNumberLabel.setText(reservationFormRoom.getRoomNumber());
        roomCategoryLabel.setText(reservationFormRoom.getRoomCategory().getRoomCategoryName());
        checkInDateLabel.setText(dateTimeFormatter.format(actualCheckInDate != null ? actualCheckInDate : reservationForm.getApproxcheckInDate()));
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

    private void displayServices(List<HotelService> services) {
        if (!services.isEmpty()) {
            serviceGridPane.getChildren().clear();

            int row = 0, col = 0;

            try {
                for (HotelService service : services) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/iuh/fit/hotelmanagementemployeeclient/view/features/room/ordering_services_panels/ServiceItem.fxml"));
                    Pane serviceItem = loader.load();
                    ServiceItemController controller = loader.getController();
                    controller.setupContext(service);
                    controller.getAddServiceBtn().setOnAction(e ->
                            handleServiceOrdering(service, controller.getAmountField().getValue(), controller.getAmountField())
                    );

                    serviceGridPane.add(serviceItem, col, row);

                    col++;
                    if (col == 4) {
                        col = 0;
                        row++;
                    }
                }

                serviceGridPane.setVisible(true);
                serviceGridPane.setManaged(true);
                emptyLabelContainer.setVisible(false);
                emptyLabelContainer.setManaged(false);
                serviceListContainer.setAlignment(Pos.TOP_CENTER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            serviceGridPane.setVisible(false);
            serviceGridPane.setManaged(false);

            if (emptyLabelContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Không có dịch vụ nào khả dụng.");
                emptyLabel.setStyle("-fx-font-size: 42px; -fx-text-fill: gray;");
                emptyLabelContainer.getChildren().add(emptyLabel);
            }

            emptyLabelContainer.setVisible(true);
            emptyLabelContainer.setManaged(true);

            serviceListContainer.setAlignment(Pos.CENTER);
        }
    }

    // ==================================================================================================================
    // 5.  Setup table lịch sử dùng dịch vụ
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

    // ==================================================================================================================
    // 6. Xử lý sự kiện thêm dịch vụ
    // ==================================================================================================================
    private void handleServiceOrdering(HotelService service, int amount, Spinner<Integer> amountField) {


        DialogPane.Dialog<ButtonType> dialog = dialogPane.showConfirmation(
                "XÁC NHẬN",
                "Bạn có chắc chắn muốn thêm dịch vụ: " + service.getServiceName() + " với số lượng: " + amount + " không?"
        );

        dialog.onClose(buttonType -> {
            if (buttonType == ButtonType.YES) {
                if (handleValidTime()) {
                    Platform.runLater(() -> navigateToRoomBookingPanel(true));
                    return;
                }

                handleServiceOrderingDAO(service, amount, amountField);
                dialogPane.showInformation("Thành Công", "Dịch vụ đã được thêm thành công!");
                try {
                    loadTable();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void handleServiceOrderingDAO(HotelService service, int quantity, Spinner<Integer> amountField) {
        try {
            RoomUsageService roomUsageService = new RoomUsageService();
            roomUsageService.setQuantity(quantity);
            roomUsageService.setUnitPrice(service.getServicePrice());
            roomUsageService.setDayAdded(LocalDateTime.now());
            roomUsageService.setHotelService(service);
            roomUsageService.setReservationForm(roomWithReservation.getReservationForm());

            String result = DAOProvider.dao.getRoomUsageServiceDAO().serviceOrdering(roomUsageService);

            if (result.equals("SERVICE_ORDERING_SUCCESS")) {
                amountField.getValueFactory().setValue(1);
            } else {
                dialogPane.showWarning("Lỗi", result);
            }

        } catch (Exception e) {
            dialogPane.showWarning("LỖI", e.getMessage());
        }
    }



    // ==================================================================================================================
    // 7. Chức năng tìm kiếm
    // ==================================================================================================================
    private void filterServicesByCategory() {
        Task<List<HotelService>> filterTask = new Task<>() {
            @Override
            protected List<HotelService> call() {
                String selectedCategory = serviceCategoryCBox.getSelectionModel().getSelectedItem();
                if ("TẤT CẢ".equals(selectedCategory)) {
                    return hotelServiceList;
                } else {
                    return hotelServiceList.stream()
                            .filter(service -> service.getServiceCategory().getServiceCategoryName().equalsIgnoreCase(selectedCategory))
                            .toList();
                }
            }
        };

        filterTask.setOnSucceeded(e -> displayServices(filterTask.getValue()));
        filterTask.setOnFailed(e -> dialogPane.showWarning("LỖI", "Lỗi khi lọc dịch vụ"));

        new Thread(filterTask).start();
    }

    // ==================================================================================================================
    // 9. Kiểm tra thời gian có phù hợp
    // ==================================================================================================================
    private boolean handleValidTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkOutDate = roomWithReservation.getReservationForm().getApproxcheckOutTime();
        return now.isAfter(checkOutDate);
    }


}
