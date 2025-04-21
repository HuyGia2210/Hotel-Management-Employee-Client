package iuh.fit.hotelmanagementemployeeclient.controller.features.room;

import com.dlsc.gemsfx.DialogPane;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.Room;
import iuh.fit.models.RoomCategory;
import iuh.fit.models.enums.ObjectStatus;
import iuh.fit.models.enums.RoomStatus;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RoomManagerController {

    // Input Fields
    @FXML
    private TextField roomIDTextField;
    @FXML
    private Spinner<Integer> floorNumbSpinner;
    @FXML
    private ComboBox<RoomCategory> roomCategoryComboBox;
    @FXML
    private ComboBox<RoomStatus> roomStateComboBox;

    // Search Fields
    @FXML
    private ComboBox<String> roomIDSearchField;
    @FXML
    private TextField roomStateSearchField, numberOfBedSearchField;

    // Table
    @FXML
    private TableView<Room> roomTableView;
    @FXML
    private TableColumn<Room, String> roomIDColumn;
    @FXML
    private TableColumn<Room, Double> roomStateColumn;
    @FXML
    private TableColumn<Room, Integer> numberOfBedColumn;
    @FXML
    private TableColumn<Room, Void> actionColumn;

    // Buttons
    @FXML
    private Button resetBtn, addBtn, updateBtn;

    // Dialog Pane
    @FXML
    private DialogPane dialogPane;

    private String flagCategory = null;
    private ObservableList<Room> items;

    public RoomManagerController() throws RemoteException {
    }

    // Gọi mấy phương thức để gắn sự kiện và dữ liệu cho lúc đầu khởi tạo giao diện
    public void initialize() throws RemoteException {
        dialogPane.toFront();
        roomTableView.setFixedCellSize(40);

        loadData();
        setupTable();

        resetBtn.setOnAction(e -> handleResetAction());
        addBtn.setOnAction(e -> handleAddAction());
        updateBtn.setOnAction(e -> handleUpdateAction());
        roomIDSearchField.setOnKeyReleased((keyEvent) -> {
            try {
                handleSearchAction();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        roomIDSearchField.setOnAction(event -> {
            try {
                handleSearchAction();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,1); // min=1, max=10, default=1
        floorNumbSpinner.setValueFactory(valueFactory);
    }

    // Phương thức load dữ liệu lên giao diện
    private void loadData() throws RemoteException {
        List<Room> rooms = DAOProvider.dao.getRoomDAO().getRoom();
        List<String> roomsID = rooms.stream().map(Room::getRoomID).collect(Collectors.toList());

        roomIDSearchField.getItems().setAll(roomsID);

        items = FXCollections.observableArrayList(rooms);
        roomTableView.setItems(items);
        roomTableView.refresh();

        List<RoomStatus> roomStatusList = List.of(RoomStatus.AVAILABLE, RoomStatus.UNAVAILABLE);
        roomStateComboBox.getItems().setAll(roomStatusList);
        roomStateComboBox.getSelectionModel().selectFirst();

        List<RoomCategory> roomCategoryList = DAOProvider.dao.getRoomCategoryDAO().getRoomCategory();
        roomCategoryComboBox.getItems().setAll(roomCategoryList);
        roomCategoryComboBox.getSelectionModel().selectFirst();

    }

    // Phương thức đổ dữ liệu vào bảng
    private void setupTable() {
        roomIDColumn.setCellValueFactory(new PropertyValueFactory<>("roomID"));
        roomStateColumn.setCellValueFactory(new PropertyValueFactory<>("roomStatus"));
        numberOfBedColumn.setCellValueFactory(cell ->{
            Room room = cell.getValue();
            return new SimpleIntegerProperty(room.getRoomCategory() != null ? room.getRoomCategory().getNumberOfBed() : 0).asObject();
        });

        setupActionColumn();
        roomTableView.setItems(items);
    }

    // setup cho cột thao tác
    // THAM KHẢO
    private void setupActionColumn() {
        Callback<TableColumn<Room, Void>, TableCell<Room, Void>> cellFactory = param -> new TableCell<>() {
            private final Button updateButton = new Button("Cập nhật");
            private final Button deleteButton = new Button("Xóa");

            private final HBox hBox = new HBox(10);

            {
                updateButton.getStyleClass().add("button-update");
                deleteButton.getStyleClass().add("button-delete");
                hBox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/styles/Button.css")).toExternalForm());

                updateButton.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    handleUpdateBtn(room);
                });

                deleteButton.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    handleDeleteAction(room);
                });


                hBox.setAlignment(Pos.CENTER);
                hBox.getChildren().addAll(updateButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Room room = getTableView().getItems().get(getIndex());

                    if (room.getRoomStatus() != RoomStatus.AVAILABLE && room.getRoomStatus() != RoomStatus.UNAVAILABLE) {
                        updateButton.setDisable(true);
                        deleteButton.setDisable(true);
                    } else {
                        updateButton.setDisable(false);
                        deleteButton.setDisable(false);
                    }

                    setGraphic(hBox);
                }
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    // Chức năng 1: Làm mới
    private void handleResetAction() {
        roomIDTextField.clear();
        floorNumbSpinner.getValueFactory().setValue(1);
        floorNumbSpinner.setDisable(false);
        roomCategoryComboBox.getSelectionModel().selectFirst();
        roomStateComboBox.getSelectionModel().selectFirst();

        roomTableView.getSelectionModel().clearSelection();

        addBtn.setManaged(true);
        addBtn.setVisible(true);
        updateBtn.setManaged(false);
        updateBtn.setVisible(false);

        floorNumbSpinner.requestFocus();
    }

    // Chức năng 2: Thêm
    private void handleAddAction() {
        try {
            String newRoomID = DAOProvider.dao.getRoomDAO().roomIDGenerate(floorNumbSpinner.getValue(), roomCategoryComboBox.getValue());
            Room room = new Room(newRoomID, roomStateComboBox.getValue(), LocalDateTime.now(), ObjectStatus.ACTIVE, roomCategoryComboBox.getValue());
            DAOProvider.dao.getRoomDAO().createData(room);
            dialogPane.showInformation("Thông báo", "Tạo phòng mới thành công với mã: " + newRoomID);
            loadData();
        } catch (Exception e) {
            dialogPane.showWarning("LỖI", e.getMessage());
        }
    }

    private String handleRoomIDGenerate() throws RemoteException {
        RoomCategory roomCategorySelected = roomCategoryComboBox.getSelectionModel().getSelectedItem();
        int floorNumb = floorNumbSpinner.getValue();

        return DAOProvider.dao.getRoomDAO().roomIDGenerate(floorNumb, roomCategorySelected);
    }

    private void handleUpdateBtn(Room room) {
        roomIDTextField.setText(room.getRoomID());
        roomCategoryComboBox.setValue(room.getRoomCategory());
        flagCategory = room.getRoomCategory().getRoomCategoryID();
        floorNumbSpinner.getValueFactory().setValue(Integer.parseInt(room.getRoomID().substring(2, 3)));
        floorNumbSpinner.setDisable(true);
        roomStateComboBox.setValue(room.getRoomStatus());


        addBtn.setManaged(false);
        addBtn.setVisible(false);
        updateBtn.setManaged(true);
        updateBtn.setVisible(true);
    }

    private void handleUpdateAction() {
        String oldRoomID = roomIDTextField.getText();
        String roomIDWithoutCategory = oldRoomID.substring(2);
        RoomCategory newCategory = roomCategoryComboBox.getValue();
        String prefix = newCategory.getRoomCategoryName().contains("Thường") ? "T" : "V";
        String newRoomID = prefix + newCategory.getNumberOfBed() + roomIDWithoutCategory;
        RoomStatus newStatus = roomStateComboBox.getValue();

        Room updatedRoom = new Room(newRoomID, newStatus, LocalDateTime.now(), ObjectStatus.ACTIVE, newCategory);
        dialogPane.showConfirmation("XÁC NHẬN", "Bạn có chắc chắn muốn cập nhật?")
                .onClose(buttonType -> {
                    if (buttonType == ButtonType.YES) {
                        try {
                            DAOProvider.dao.getRoomDAO().updateData(oldRoomID, flagCategory, updatedRoom);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                        handleResetAction();
                        try {
                            loadData();
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                        dialogPane.showInformation("Thành công", "Phòng đã được cập nhật.");
                    }
                });
    }

    private void handleDeleteAction(Room room){
        try{
            System.out.println(room.getRoomID());
            DialogPane.Dialog<ButtonType> dialog = dialogPane.showConfirmation(
                    "XÁC NHẬN",
                    "Bạn có chắc chắn muốn xóa phòng này?"
            );

            dialog.onClose(buttonType -> {
                if (buttonType == ButtonType.YES) {
                    try {
                        DAOProvider.dao.getRoomDAO().deleteData(room.getRoomID());
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    handleResetAction();
                    try {
                        loadData();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    dialogPane.showInformation("Thành công", "Xóa phòng thành công");
                }
            });

        }catch(Exception e){
            dialogPane.showWarning("LỖI", e.getMessage());
        }
    }

    private void handleSearchAction() throws RemoteException {
        roomStateSearchField.setText("");
        numberOfBedSearchField.setText("");

        String searchText = roomIDSearchField.getEditor().getText();
        List<Room> room;

        if (searchText == null || searchText.isEmpty()) {
            room = DAOProvider.dao.getRoomDAO().getRoom();
        } else {
            room = DAOProvider.dao.getRoomDAO().findDataByAnyContainsId(searchText);
            if (!room.isEmpty()) {
                if(room.size()==1){
                    roomStateSearchField.setText(room.getFirst().getRoomStatus().toString());
                    numberOfBedSearchField.setText(String.valueOf(room.getFirst().getRoomCategory().getNumberOfBed()));
                }
            }else{
                roomStateSearchField.setText("rỗng");
                numberOfBedSearchField.setText("rỗng");
            }
        }

        items.setAll(room);
        roomTableView.setItems(items);
    }


    public void setInformation(Room room){
        Platform.runLater(() -> {
            roomIDSearchField.setValue(room.getRoomID());
            handleUpdateBtn(room);
        });
    }
}
