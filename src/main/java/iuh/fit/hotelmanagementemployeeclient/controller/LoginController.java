package iuh.fit.hotelmanagementemployeeclient.controller;

import com.dlsc.gemsfx.DialogPane;
import iuh.fit.hotelmanagementemployeeclient.service.DAOProvider;
import iuh.fit.models.Account;
import iuh.fit.models.enums.AccountStatus;
import iuh.fit.security.PasswordHashing;
import iuh.fit.hotelmanagementemployeeclient.utils.ErrorMessages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.Objects;

public class LoginController {


    @FXML private DialogPane dialogPane;


    // --- Các trường nhập liệu ---
    @FXML private TextField userNameField;             // Trường nhập tên đăng nhập
    @FXML private PasswordField hiddenPasswordField;   // Trường nhập mật khẩu (ẩn)
    @FXML private TextField visiblePasswordField;      // Trường nhập mật khẩu (hiện - kiểu text)

    // --- Thành phần điều khiển giao diện ---
    @FXML private Button signInButton;                 // Nút thực hiện đăng nhập
    @FXML private ImageView showPassButton;            // Nút chuyển đổi hiển thị mật khẩu
    @FXML private Text errorMessage;                   // Thông báo lỗi khi đăng nhập


    public LoginController() throws RemoteException {
    }

    @FXML
    public void initialize(Stage mainStage) {

        dialogPane.toFront();
        hiddenPasswordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
        signInButton.setOnMouseClicked(event -> {
            try {
                signIn(mainStage);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }



// =====================================================================================
// 🔐 Các hàm xử lý hiển thị / ẩn mật khẩu tại giao diện đăng nhập (Login Pane)
// =====================================================================================
    private boolean isDefaultIcon = true;

    @FXML
    private void changePasswordViewState() {
        if (isDefaultIcon) {
            Image newIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/iuh/fit/hotelmanagementemployeeclient/icons/login_panel_icons/ic_hidden_password.png")));
            showPassButton.setImage(newIcon);
        } else {
            Image defaultIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/iuh/fit/hotelmanagementemployeeclient/icons/login_panel_icons/ic_show_password.png")));
            showPassButton.setImage(defaultIcon);
        }

        PasswordVisibility();

        isDefaultIcon = !isDefaultIcon;
    }

    private void PasswordVisibility() {
        if (hiddenPasswordField.isVisible()) {
            hiddenPasswordField.setVisible(false);
            hiddenPasswordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
        } else {
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            hiddenPasswordField.setVisible(true);
            hiddenPasswordField.setManaged(true);
        }
    }

// =====================================================================================
// 🔑 Các hàm xử lý chức năng đăng nhập (Login)
// =====================================================================================
    private void signIn(Stage mainStage) throws RemoteException {
        String userName = userNameField.getText();
        String password = hiddenPasswordField.getText();

        if (userName.isEmpty()) {
            errorMessage.setText(ErrorMessages.LOGIN_INVALID_USERNAME);
            return;
        }

        if (password.isEmpty()) {
            errorMessage.setText(ErrorMessages.LOGIN_INVALID_PASSWORD);
            return;
        }


        Account account = DAOProvider.dao.getAccountDAO().getLogin(userName, PasswordHashing.hashPassword(password));
        if (account == null) {
            errorMessage.setText(ErrorMessages.LOGIN_INVALID_ACCOUNT);
            return;
        }


        if (account.getStatus().equals(AccountStatus.LOCKED)) {
            dialogPane.showInformation(
                    "Thông báo",
                    "Tài khoản bị khóa hoặc không có hiệu lực.\n" +
                            "Vui lòng báo người quản lý khách sạn để biết thêm thông tin."
            );
            return;
        }

        loadMainUI(account, mainStage);
    }

// =====================================================================================
// Chuyển sang giao diện chính (đăng nhập thành công)
// =====================================================================================
    private void loadMainUI(Account account, Stage mainStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/iuh/fit/hotelmanagementemployeeclient/view/ui/MainUI.fxml"));
            AnchorPane mainPanel = fxmlLoader.load();

            MainController mainController = fxmlLoader.getController();
            mainController.initialize(account, mainStage);

            Scene scene = new Scene(mainPanel);

            mainStage.setWidth(1200);
            mainStage.setHeight(680);
            mainStage.setScene(scene);
            mainStage.setResizable(true);
            mainStage.setMaximized(true);

            mainStage.centerOnScreen();

            mainStage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            errorMessage.setText(e.getMessage());
        }
    }

}
