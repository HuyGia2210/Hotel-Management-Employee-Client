package iuh.fit.hotelmanagementemployeeclient.controller.features;

import iuh.fit.hotelmanagementemployeeclient.controller.LoginController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;


public class TopController {
    @FXML
    private Label clockLabel, dateLabel;
    @FXML
    private Button logoutBtn;

    @FXML
    private ImageView img;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");


    private Stage mainStage;

    @FXML
    public void initialize(Stage stage) {
        this.mainStage = stage;

        // clock
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateClock()));
        timeline.setCycleCount(Timeline.INDEFINITE); //
        timeline.play();


        handleTooltips();
        handleButtons();
    }


    private void updateClock() {
        String currentTime = LocalTime.now().format(timeFormatter);
        LocalDate localDate = LocalDate.now();
        String currentDate = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi", "VN"))+", "+
                localDate.getDayOfMonth() +"/"+localDate.getMonthValue()+"/"+localDate.getYear();
        clockLabel.setText(currentTime);
        dateLabel.setText("  "+currentDate);
    }

    private void logout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/ui/LoginUI.fxml"));
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);

        LoginController loginController = loader.getController();
        loginController.initialize(mainStage);

        mainStage.setTitle("Quản Lý Khách Sạn");
        mainStage.setScene(scene);
        mainStage.setResizable(false);
        mainStage.setWidth(610);
        mainStage.setHeight(400);
        mainStage.setMaximized(false);
        mainStage.centerOnScreen();

        mainStage.show();

        mainStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void handleTooltips() {
        Tooltip tooltip = new Tooltip("Đăng xuất");
        Tooltip.install(logoutBtn, tooltip);

        logoutBtn.setTooltip(tooltip);
        tooltip.setShowDelay(Duration.millis(400));
    }

    private void handleButtons() {
        // Tạo hiệu ứng khi hover
        ColorAdjust hoverEffect = new ColorAdjust();
        hoverEffect.setBrightness(-0.2); // Làm màu đậm hơn

        ColorAdjust hoverEffect2 = new ColorAdjust();
        hoverEffect2.setBrightness(-0.3); // Làm màu đậm hơn

        // Khi hover vào button
        logoutBtn.setOnMouseEntered(event -> img.setEffect(hoverEffect));

        // Khi rời chuột khỏi button
        logoutBtn.setOnMouseExited(event -> img.setEffect(null));

        logoutBtn.setOnMousePressed(event -> img.setEffect(hoverEffect2));

        logoutBtn.setOnMouseReleased(event -> img.setEffect(null));

        // logout
        logoutBtn.setOnAction(e -> {
            try {
                logout();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

    }

}
