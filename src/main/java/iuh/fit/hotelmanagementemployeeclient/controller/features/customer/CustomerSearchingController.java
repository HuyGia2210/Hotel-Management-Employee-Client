package iuh.fit.hotelmanagementemployeeclient.controller.features.customer;

import iuh.fit.hotelmanagementemployeeclient.controller.MainController;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.Account;
import iuh.fit.models.Customer;
import iuh.fit.models.enums.Gender;
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

public class CustomerSearchingController {
    // Input Fields
    @FXML
    private TextField
            customerIDTextField, fullNameTextField,
            phoneNumberTextField, addressTextField,
            cardIDTextField;
    @FXML
    private DatePicker DOBPicker;
    @FXML
    private RadioButton male;
    @FXML
    private ToggleGroup gender;

    // Table
    @FXML
    private TableView<Customer> customerTableView;
    @FXML
    private TableColumn<Customer, String> customerIDColumn;
    @FXML
    private TableColumn<Customer, String> fullNameColumn;
    @FXML
    private TableColumn<Customer, String> phoneNumberColumn;
    @FXML
    private TableColumn<Customer, String> cardIDColumn;
    @FXML
    private TableColumn<Customer , Void> actionColumn;

    // Buttons
    @FXML
    private Button resetBtn, searchBtn;

    private ObservableList<Customer> items;

    private MainController mainController;
    private Account account;

    public CustomerSearchingController() throws RemoteException {
    }

    public void setupContext(MainController mainController, Account account) {
        this.mainController = mainController;
        this.account = account;
    }

    public void initialize() {
        loadData();
        setupTable();
        customerTableView.setFixedCellSize(40);
        searchBtn.setOnAction(e -> handleSearchAction());
        resetBtn.setOnAction(e -> handleResetAction());
    }

    private void loadData() {
        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws RemoteException {
                List<Customer> customerList = DAOProvider.dao.getCustomerDAO().findAll();
                items = FXCollections.observableArrayList(customerList);
                Platform.runLater(() -> {
                    customerTableView.setItems(items);
                    customerTableView.refresh();
                });
                return null;
            }
        };

        new Thread(loadDataTask).start();
    }

    private void setupTable() {
        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerCode"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        cardIDColumn.setCellValueFactory(new PropertyValueFactory<>("idCardNumber"));
        setupActionColumn();
        setupTableContextMenu();
    }

    private void setupTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editMenuItem = new MenuItem("Chỉnh sửa");

        editMenuItem.setOnAction(event -> {
            Customer employee = customerTableView.getSelectionModel().getSelectedItem();
            if (employee != null) {
                try {
                    handleEditCustomer(employee);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        contextMenu.getItems().addAll(editMenuItem);
        customerTableView.setContextMenu(contextMenu);
    }

    private void handleEditCustomer(Customer customer) throws IOException {
        mainController.loadPanelCustomerManagerController("/iuh/fit/hotelmanagementemployeeclient/view/features/customer/CustomerManagerPanel.fxml", customer);
    }

    private void setupActionColumn() {
        Callback<TableColumn<Customer, Void>, TableCell<Customer, Void>> cellFactory = param -> new TableCell<>() {
            private final Button showInfoButton = new Button("Thông tin");
            private final HBox hBox = new HBox(10);
            {
                // Thêm class CSS cho các button
                showInfoButton.getStyleClass().add("button-view");

                // Thêm file CSS vào HBox
                hBox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/styles/Button.css")).toExternalForm());

                showInfoButton.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    try {
                        handleShowCustomerInformation(customer);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                hBox.setAlignment(Pos.CENTER);
                hBox.getChildren().addAll(showInfoButton);
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

    private void handleShowCustomerInformation(Customer customer) throws IOException {
        String source = "/iuh/fit/hotelmanagementemployeeclient/view/features/customer/CustomerInformationView.fxml";

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(source)));
        AnchorPane layout = loader.load(); // Gọi load() trước khi getController()

        CustomerInformationViewController customerInformationViewController = loader.getController();
        customerInformationViewController.setCustomer(customer);

        Scene scene = new Scene(layout);

        Stage stage = new Stage();
        stage.setTitle("Thông tin khách hàng");
        stage.setScene(scene);

        String iconPath = "/iuh/fit/hotelmanagementemployeeclient/icons/menu_icons/ic_customer.png"; // Đường dẫn đến icon
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        stage.setTitle("Thông tin khách hàng");

        stage.show();
    }

    private void handleResetAction() {
        customerIDTextField.setText("");
        fullNameTextField.setText("");
        phoneNumberTextField.setText("");
        cardIDTextField.setText("");
        male.setSelected(true);
        DOBPicker.setValue(null);
        addressTextField.setText("");
        gender.getSelectedToggle().setSelected(false);

        loadData();
    }

    private void handleSearchAction() {

    }
}
