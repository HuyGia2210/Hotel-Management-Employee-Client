package iuh.fit.hotelmanagementemployeeclient.controller.features.customer;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.DialogPane;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.Customer;
import iuh.fit.models.enums.Gender;
import iuh.fit.models.enums.ObjectStatus;
import iuh.fit.hotelmanagementemployeeclient.utils.ErrorMessages;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class CustomerManagerController {

    // Input Fields
    @FXML
    private TextField customerAddressTextField;
    @FXML
    private TextField customerIDCardNumberTextField;
    @FXML
    private CalendarPicker customerDOBCalendarPicker;
    @FXML
    private TextField customerIDTextField;
    @FXML
    private TextField customerNameTextField;
    @FXML
    private TextField customerPhoneNumberTextField;
    @FXML
    private ToggleGroup genderToggleGroup;
    @FXML
    private RadioButton radFemale;
    @FXML
    private RadioButton radMale;

    // Search Fields
    @FXML
    private TextField customerNameSearchField, customerGenderSearchField,
            customerPhoneSearchField;
    @FXML
    private ComboBox<String> customerIDSearchField;

    // Table
    @FXML
    private TableView<Customer> customerTableView;

    @FXML
    private TableColumn<Customer, Gender> customerGenderColumn;
    @FXML
    private TableColumn<Customer, String> customerIDColumn;
    @FXML
    private TableColumn<Customer, String> customerNameColumn;
    @FXML
    private TableColumn<Customer, String> customerPhoneColumn;
    @FXML
    private TableColumn<Customer, Void> actionColumn;

    // Buttons
    @FXML
    private Button addBtn, updateBtn, resetBtn;

    // Dialog
    @FXML
    private DialogPane dialogPane;

    private ObservableList<Customer> items;

    public CustomerManagerController() throws RemoteException {
    }

    // Gọi mấy phương thức để gắn sự kiện và dữ liệu cho lúc đầu khởi tạo giao diện
    public void initialize() throws RemoteException {
        dialogPane.toFront();
        customerTableView.setFixedCellSize(40);

        loadData();
        setupTable();

        addBtn.setOnAction(e -> handleAddAction());
        updateBtn.setOnAction(e -> handleUpdateAction());
        resetBtn.setOnAction(e -> {
            try {
                handleResetAction();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        customerIDSearchField.setOnAction(e -> handleSearchAction());

        customerTableView.setRowFactory(x->{
            TableRow<Customer> customerRow = new TableRow<>();

            customerRow.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !customerRow.isEmpty()) { // Kiểm tra double-click và dòng không trống
                    Customer rowData = customerRow.getItem();
                    // Thực hiện hành động khi double-click
                    try {
                        handleShowCustomerInformation(rowData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return customerRow;
        });
    }

    // Phương thức load dữ liệu lên giao diện
    private void loadData() {
        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws RemoteException {
                List<Customer> customerList = DAOProvider.dao.getCustomerDAO().findAll();
                items = FXCollections.observableArrayList(customerList);

                Platform.runLater(() -> {
                    customerTableView.setItems(items);
                    customerTableView.refresh();
                    try {
                        customerIDTextField.setText(DAOProvider.dao.getGlobalSequenceDAO().getNextID("Customer"));
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        customerIDSearchField.getItems().setAll(DAOProvider.dao.getCustomerDAO().getTopThreeID());
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                });
                return null;
            }
        };

        new Thread(loadDataTask).start();
    }


    // Phương thức đổ dữ liệu vào bảng
    private void setupTable() throws RemoteException {
        List<Customer> list = DAOProvider.dao.getCustomerDAO().findAll();
        ObservableList<Customer> customers = FXCollections.observableList(list);

        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerCode"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        customerGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        customerTableView.setItems(customers);
        setupActionColumn();
    }

    // setup cho cột thao tác
    // THAM KHẢO
    private void setupActionColumn() {
        Callback<TableColumn<Customer, Void>, TableCell<Customer, Void>> cellFactory = param -> new TableCell<>() {
            private final Button updateButton = new Button("Cập nhật");
            private final Button deleteButton = new Button("Xóa");
            private final Button showInfoButton = new Button("Thông tin");
            private final HBox hBox = new HBox(10);
            {
                // Thêm class CSS cho các button
                updateButton.getStyleClass().add("button-update");
                deleteButton.getStyleClass().add("button-delete");
                showInfoButton.getStyleClass().add("button-view");

                // Thêm file CSS vào HBox
                hBox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/styles/Button.css")).toExternalForm());

                // Set hành động cho các button
                updateButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleUpdateBtn(customer);
                });

                deleteButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleDeleteAction(customer);
                });

                showInfoButton.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    try {
                        handleShowCustomerInformation(customer);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                hBox.setAlignment(Pos.CENTER);
                hBox.getChildren().addAll(updateButton, deleteButton, showInfoButton);
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hBox);
                }
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }

    // Chức năng 1: Làm mới
    public void handleResetAction() throws RemoteException {
        customerIDTextField.setText(DAOProvider.dao.getGlobalSequenceDAO().getNextID("Customer"));
        customerIDCardNumberTextField.setText("");
        customerNameTextField.setText("");
        customerPhoneNumberTextField.setText("");
        customerAddressTextField.setText("");
        Toggle rad =  genderToggleGroup.getSelectedToggle();
        if (rad != null) rad.setSelected(false);
        customerDOBCalendarPicker.setValue(null);
        customerNameTextField.requestFocus();

        switchButton(false, true);

    }

    // Chức năng 2: Thêm
    public void handleAddAction() {
        try{
            Customer customer = createCustomer();
            DAOProvider.dao.getCustomerDAO().create(customer);
            setupTable();
            handleResetAction();
            dialogPane.showInformation("Thành công", "Đã thêm khách hàng thành công");
        }catch (Exception e){
            dialogPane.showWarning("LỖI", e.getMessage());
        }
    }

    // Chức năng 3: Xóa
    private void handleDeleteAction(Customer customer) {
        DialogPane.Dialog<ButtonType> dialog = dialogPane.showConfirmation("XÁC NHẬN", "Bạn có chắc chắn muốn xóa?");

        dialog.onClose(buttonType -> {
            if (buttonType == ButtonType.YES) {
                try {
                    DAOProvider.dao.getCustomerDAO().delete(customer.getCustomerCode());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    handleResetAction();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    setupTable();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // Chức năng 4: Cập nhật
    // 4.1 Xử lý sự kiện khi kích hoạt chức năng cập nhật
    private void handleUpdateBtn(Customer customer) {
        customerIDTextField.setText(customer.getCustomerCode());
        customerNameTextField.setText(customer.getFullName());
        customerPhoneNumberTextField.setText(customer.getPhoneNumber());
        customerAddressTextField.setText(customer.getAddress());
        customerIDCardNumberTextField.setText(customer.getIdCardNumber());
        customerDOBCalendarPicker.setValue(customer.getDob());

        if (customer.getGender().equals(Gender.MALE)) radMale.setSelected(true);
        else radFemale.setSelected(true);

        switchButton(true, false);
    }

    // 4.2 Chức năng cập nhật
    public void handleUpdateAction() {
        try {
            Customer newInfor = createCustomer();
            DialogPane.Dialog<ButtonType> dialog = dialogPane.showConfirmation(
                    "XÁC NHẬN",
                    "Bạn có chắc chắn muốn cập nhật?"
            );

            dialog.onClose(buttonType -> {
                if (buttonType == ButtonType.YES) {
                    try {
                        DAOProvider.dao.getCustomerDAO().update(newInfor);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    loadData();
                    try {
                        handleResetAction();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            switchButton(false, true);
        } catch (Exception e){
            dialogPane.showWarning("LỖI", e.getMessage());
        }
    }

    // Chức năng 5: Tìm kiếm
    private void handleSearchAction() {
        customerNameSearchField.clear();
        customerPhoneSearchField.clear();
        customerGenderSearchField.clear();

        String searchText = customerIDSearchField.getValue();
        Task<ObservableList<Customer>> searchTask = new Task<>() {
            @Override
            protected ObservableList<Customer> call() throws RemoteException {
                List<Customer> customers = (searchText == null || searchText.isEmpty())
                        ? DAOProvider.dao.getCustomerDAO().findAll()
                        : DAOProvider.dao.getCustomerDAO().findDataByContainsId(searchText);
                return FXCollections.observableArrayList(customers);
            }
        };

        searchTask.setOnSucceeded(e -> {
            items = searchTask.getValue();
            customerTableView.setItems(items);
            if (items.size() == 1) {
                Customer customer = items.getFirst();
                customerNameSearchField.setText(customer.getFullName());
                customerPhoneSearchField.setText(customer.getPhoneNumber());
                customerGenderSearchField.setText(customer.getGender().name());
            }
        });

        new Thread(searchTask).start();
    }


    // Chức năng 6: Xem thông tin khách hàng
    private void handleShowCustomerInformation(Customer customer) throws IOException {
        String source = "/iuh/fit/hotelmanagementemployeeclient/view/features/customer/CustomerInformationView.fxml";

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(source)));
        AnchorPane layout = loader.load();

        CustomerInformationViewController customerInformationViewController = loader.getController();
        customerInformationViewController.setCustomer(customer);

        Scene scene = new Scene(layout);

        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setScene(scene);

        String iconPath = "/iuh/fit/hotelmanagementemployeeclient/icons/menu_icons/ic_customer.png";
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        stage.setTitle("Thông tin khách hàng");

        stage.show();
    }

    private Customer createCustomer() {
        String id = customerIDTextField.getText();
        String name = customerNameTextField.getText();
        String phone = customerPhoneNumberTextField.getText();
        String address = customerAddressTextField.getText();

        Gender gender;
        RadioButton rad =  (RadioButton) genderToggleGroup.getSelectedToggle();
        if (rad == null) throw new IllegalArgumentException(ErrorMessages.CUS_GENDER_NOT_SELECTED);
        else if (rad.getText().equalsIgnoreCase("NAM")) gender = Gender.MALE;
        else gender = Gender.FEMALE;

        String idCardNumber = customerIDCardNumberTextField.getText();
        LocalDate dob = customerDOBCalendarPicker.getValue();

        return new Customer(id, name, phone, address, gender, idCardNumber, dob, ObjectStatus.ACTIVE);
    }

    private void switchButton(boolean updateButton, boolean addButton){
        updateBtn.setManaged(updateButton);
        updateBtn.setVisible(updateButton);
        addBtn.setManaged(addButton);
        addBtn.setVisible(addButton);
    }

    public void setInformation(Customer customer){
        Platform.runLater(() -> {
            customerIDSearchField.setValue(customer.getCustomerCode());
        });
        Platform.runLater(() -> {
            handleUpdateBtn(customer);
        });
    }
}
