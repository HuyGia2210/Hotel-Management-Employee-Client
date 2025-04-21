package iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers;

import iuh.fit.hotelmanagementemployeeclient.controller.MainController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.RoomBookingController;
import iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_out_controllers.CheckingOutReservationFormController;
import iuh.fit.models.Customer;
import iuh.fit.models.Employee;
import iuh.fit.models.ReservationForm;
import iuh.fit.models.Room;
import iuh.fit.models.enums.RoomStatus;
import iuh.fit.models.wrapper.RoomWithReservation;
import iuh.fit.hotelmanagementemployeeclient.utils.RoomManagementService;
import iuh.fit.hotelmanagementemployeeclient.utils.TimelineManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RoomOverDueController {
    // ==================================================================================================================
    // 1. Các biến
    // ==================================================================================================================
    @FXML
    private Text roomNumberText, checkOutDateText, lateDuration;
    @FXML
    private Label roomCategoryNameLabel, customerFullNameLabel;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm", Locale.forLanguageTag("vi-VN"));

    private MainController mainController;
    private Employee employee;
    private RoomWithReservation roomWithReservation;

    private java.time.Duration duration;
    private Timeline timeline;


    // ==================================================================================================================
    // 2. Khởi tạo và nạp dữ liệu vào giao diện
    // ==================================================================================================================
    public void setupContext(MainController mainController, Employee employee,
                             RoomWithReservation roomWithReservation) {
        this.mainController = mainController;
        this.employee = employee;
        this.roomWithReservation = roomWithReservation;

        Room room = roomWithReservation.getRoom();
        Customer customer = roomWithReservation.getReservationForm().getCustomer();
        ReservationForm reservationForm = roomWithReservation.getReservationForm();

        roomCategoryNameLabel.setText(room.getRoomCategory().getRoomCategoryName());
        roomNumberText.setText(room.getRoomNumber());
        customerFullNameLabel.setText(customer.getFullName());
        checkOutDateText.setText(dateTimeFormatter.format(reservationForm.getApproxcheckOutTime()));

        initializeOverdueTimeTracking(roomWithReservation.getReservationForm().getApproxcheckOutTime());
    }

    private void initializeOverdueTimeTracking(LocalDateTime checkOutDate) {
        String timelineKey = roomWithReservation.getRoom().getRoomID() + RoomStatus.OVER_DUE.name();

        duration = java.time.Duration.between(checkOutDate, LocalDateTime.now());

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        lateDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        if (TimelineManager.getInstance().containsTimeline(timelineKey)) {
            TimelineManager.getInstance().removeTimeline(timelineKey);
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            try {
                refreshLateDurationDisplay(checkOutDate);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        TimelineManager.getInstance().addTimeline(timelineKey, timeline);
    }

    private void refreshLateDurationDisplay(LocalDateTime checkOutDate) throws RemoteException {
        duration = java.time.Duration.between(checkOutDate, LocalDateTime.now());

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        lateDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        if (hours >= 2) {
            if (timeline != null) {
                if (MainController.isRoomBookingLoaded()) navigateToRoomBookingPanel(false);
                else RoomManagementService.autoCheckoutOverdueRooms(mainController);
                TimelineManager.getInstance().removeTimeline(roomWithReservation.getRoom().getRoomID() + RoomStatus.OVER_DUE.name());
            }

        }
    }

    // ==================================================================================================================
    // 3. Xử lý chức năng hiển thị panel khác
    // ==================================================================================================================
    @FXML
    private void navigateToCheckingOutReservationForm() {
        if (duration != null && duration.toHours() >= 2) {
            navigateToRoomBookingPanel(true);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/iuh/fit/hotelmanagementemployeeclient/view/features/room/checking_out_panels/CheckingOutReservationFormPanel.fxml"));
            AnchorPane layout = loader.load();

            CheckingOutReservationFormController checkingOutReservationFormController = loader.getController();
            checkingOutReservationFormController.setupContext(
                    mainController, employee, roomWithReservation
            );


            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());
            MainController.setRoomBookingLoaded(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToRoomBookingPanel(boolean isError) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/features/room/RoomBookingPanel.fxml"));
            AnchorPane layout = loader.load();

            RoomBookingController roomBookingController = loader.getController();
            roomBookingController.setupContext(mainController, employee);
            if (isError)
                roomBookingController.getDialogPane().showInformation(
                        "Không thể thực hiện thao tác",
                        "Phòng này đã quá hạn Checkout. Hệ thống đã tự động thực hiện Checkout và xuất hóa đơn."
                );


            mainController.getMainPanel().getChildren().clear();
            mainController.getMainPanel().getChildren().addAll(layout.getChildren());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
