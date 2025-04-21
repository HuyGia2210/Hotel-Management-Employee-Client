package iuh.fit.hotelmanagementemployeeclient.controller.features.room;

import com.dlsc.gemsfx.DialogPane;
import iuh.fit.hotelmanagementemployeeclient.controller.MainController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers.RoomAvailableItemController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers.RoomOnUseItemController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers.RoomOverDueController;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.Employee;
import iuh.fit.models.Room;
import iuh.fit.models.enums.RoomStatus;
import iuh.fit.models.wrapper.RoomWithReservation;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomManagementService;
import iuh.fit.hotelmanagementemployeeclient.utils.TimelineManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static iuh.fit.hotelmanagementemployeeclient.utils.GlobalConstants.*;

public class RoomBookingController {

    @FXML
    private GridPane roomGridPane;

    @FXML
    private ComboBox<String> roomCategoryCBox, roomFloorNumberCBox;

    @FXML
    private Button allBtn, availableBtn,
            onUseBtn, overDueBtn;

    @Getter
    @FXML
    private DialogPane dialogPane;

    private List<RoomWithReservation> roomWithReservations;
    private MainController mainController;
    private Employee employee;

    private Button activeButton;
    private RoomStatus selectedStatus = null;

    public RoomBookingController() throws RemoteException {
    }


    public void initialize() {
        dialogPane.toFront();
        activeButton = allBtn;
        setActiveButtonStyle(allBtn);
        MainController.setRoomBookingLoaded(true);

    }

    public void setupContext(MainController mainController, Employee employee) {
        this.mainController = mainController;
        this.employee = employee;
        loadData();
        setupEventHandlers();

    }

    private List<String> getRoomCategories() throws RemoteException {
        List<String> roomCategoryList = DAOProvider.dao.getRoomCategoryDAO().getRoomCategory().stream()
                .map(rc -> rc.getRoomCategoryID() + " " + rc.getRoomCategoryName())
                .collect(Collectors.toList());
        roomCategoryList.addFirst("TẤT CẢ");
        return roomCategoryList;
    }

    private List<String> getFloorNumbers() {
        return List.of("TẤT CẢ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    }

    private void loadData() {
        Task<List<RoomWithReservation>> loadDataTask = new Task<>() {
            @Override
            protected List<RoomWithReservation> call() throws RemoteException {
                List<String> roomCategories = getRoomCategories();
                List<String> floorNumbers = getFloorNumbers();

                Platform.runLater(() -> {
                    roomCategoryCBox.getItems().setAll(roomCategories);
                    roomCategoryCBox.getSelectionModel().selectFirst();

                    roomFloorNumberCBox.getItems().setAll(floorNumbers);
                    roomFloorNumberCBox.getSelectionModel().selectFirst();
                });

                RoomManagementService.autoCheckoutOverdueRooms(mainController);
                return DAOProvider.dao.getRoomWithReservationDAO().getRoomWithReservation().stream()
                        .sorted(Comparator.comparing(r -> r.getRoom().getRoomNumber()))
                        .toList();
            }
        };

        loadDataTask.setOnSucceeded(event -> {
            roomWithReservations = loadDataTask.getValue();
            displayFilteredRooms(roomWithReservations);
            try {
                loadDataForBtn();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            TimelineManager.getInstance().printAllTimelines();
        });

        loadDataTask.setOnFailed(event -> {
            throw new IllegalArgumentException(loadDataTask.getException().getMessage());
        });

        new Thread(loadDataTask).start();
    }

    private void loadDataForBtn() throws RemoteException {
        Map<RoomStatus, Long> roomStatusCount = DAOProvider.dao.getRoomDAO().getRoomStatusCount();

        availableBtn.setText(ROOM_BOOKING_AVAIL_BTN + " (" + roomStatusCount.getOrDefault(RoomStatus.AVAILABLE, 0L) + ")");
        onUseBtn.setText(ROOM_BOOKING_ON_USE_BTN + " (" + roomStatusCount.getOrDefault(RoomStatus.IN_USE, 0L) + ")");
        overDueBtn.setText(ROOM_BOOKING_OVER_DUE_BTN + " (" + roomStatusCount.getOrDefault(RoomStatus.OVER_DUE, 0L) + ")");
        allBtn.setText(ROOM_BOOKING_ALL_BTN + " (" + roomWithReservations.size() + ")");
    }


    private void displayFilteredRooms(List<RoomWithReservation> roomsWithReservations) {
        roomGridPane.getChildren().clear();

        int row = 0, col = 0;

        try {
            for (RoomWithReservation roomWithReservation : roomsWithReservations) {
                Pane roomItem = loadRoomItem(roomWithReservation);

                roomGridPane.add(roomItem, col, row);

                col++;
                if (col == 3) {
                    col = 0;
                    row++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Pane loadRoomItem(RoomWithReservation roomWithReservation) throws IOException {
        FXMLLoader loader;
        Pane roomItem;

        Room room = roomWithReservation.getRoom();


        switch (room.getRoomStatus()) {
            case AVAILABLE -> {
                loader = new FXMLLoader(getClass().getResource(
                        "/iuh/fit/hotelmanagementemployeeclient/view/features/room/creating_reservation_form_panels/RoomAvailableItem.fxml"));
                roomItem = loader.load();

                RoomAvailableItemController controller = loader.getController();
                controller.setupContext(mainController, employee, roomWithReservation);
            }
            case IN_USE -> {
                loader = new FXMLLoader(getClass().getResource(
                        "/iuh/fit/hotelmanagementemployeeclient/view/features/room/creating_reservation_form_panels/RoomOnUseItem.fxml"));
                roomItem = loader.load();

                RoomOnUseItemController controller = loader.getController();
                controller.setupContext(mainController, employee, roomWithReservation);
            }
            case OVER_DUE -> {
                loader = new FXMLLoader(getClass().getResource(
                        "/iuh/fit/hotelmanagementemployeeclient/view/features/room/creating_reservation_form_panels/RoomOverDueItem.fxml"));
                roomItem = loader.load();

                RoomOverDueController controller = loader.getController();
                controller.setupContext(mainController, employee, roomWithReservation);
            }
            default -> throw new IllegalStateException("Unexpected value: " + room.getRoomStatus());
        }
        return roomItem;
    }

    private void handleSearch() throws RemoteException {
        if (roomWithReservations == null) {
            return;
        }

        List<RoomWithReservation> filteredRooms = roomWithReservations;

        String selectedCategory = roomCategoryCBox.getSelectionModel().getSelectedItem();
        if (selectedCategory != null && !selectedCategory.equals("TẤT CẢ")) {
            String categoryID = selectedCategory.split(" ")[0];
            filteredRooms = filteredRooms.stream()
                    .filter(r -> r.getRoom().getRoomCategory().getRoomCategoryID().equals(categoryID))
                    .toList();
        }

        String selectedFloor = roomFloorNumberCBox.getSelectionModel().getSelectedItem();
        if (selectedFloor != null && !selectedFloor.equals("TẤT CẢ")) {
            filteredRooms = filteredRooms.stream()
                    .filter(r -> r.getRoom().getRoomID().charAt(2) == selectedFloor.charAt(0))
                    .toList();
        }

        if (selectedStatus != null) {
            filteredRooms = filteredRooms.stream()
                    .filter(r -> r.getRoom().getRoomStatus() == selectedStatus)
                    .toList();
        }

        filteredRooms = filteredRooms.stream()
                .sorted(Comparator.comparing(r -> r.getRoom().getRoomNumber()))
                .toList();

        displayFilteredRooms(filteredRooms);
        loadDataForBtn();
    }

    private void setupEventHandlers() {
        setupButtonAction(allBtn, null);
        setupButtonAction(availableBtn, RoomStatus.AVAILABLE);
        setupButtonAction(onUseBtn, RoomStatus.IN_USE);
        setupButtonAction(overDueBtn, RoomStatus.OVER_DUE);

        roomCategoryCBox.setOnAction(e -> {
            try {
                handleSearch();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        roomFloorNumberCBox.setOnAction(e -> {
            try {
                handleSearch();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void setupButtonAction(Button button, RoomStatus status) {
        button.setOnAction(e -> {
            if (activeButton == button) {
                resetButtonStyle(button);
                setActiveButtonStyle(allBtn);
                activeButton = allBtn;
                selectedStatus = null;
            } else {
                if (activeButton != null) resetButtonStyle(activeButton);
                setActiveButtonStyle(button);
                activeButton = button;
                selectedStatus = status;
            }
            try {
                handleSearch();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void setActiveButtonStyle(Button button) {
        switch (button.getId()) {
            case "allBtn" -> button.getStyleClass().add("button-All-selected");
            case "availableBtn" -> button.getStyleClass().add("button-Available-selected");
            case "onUseBtn" -> button.getStyleClass().add("button-OnUse-selected");
            case "overDueBtn" -> button.getStyleClass().add("button-OverDue-selected");
            default -> throw new IllegalArgumentException("Không tìm thấy button ID");
        }
    }

    private void resetButtonStyle(Button button) {
        switch (button.getId()) {
            case "allBtn" -> button.getStyleClass().remove("button-All-selected");
            case "availableBtn" -> button.getStyleClass().remove("button-Available-selected");
            case "onUseBtn" -> button.getStyleClass().remove("button-OnUse-selected");
            case "overDueBtn" -> button.getStyleClass().remove("button-OverDue-selected");
            default -> throw new IllegalArgumentException("Không tìm thấy button ID");
        }
    }


}
