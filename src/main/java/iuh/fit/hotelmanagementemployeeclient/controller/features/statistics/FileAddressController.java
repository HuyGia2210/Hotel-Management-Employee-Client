package iuh.fit.hotelmanagementemployeeclient.controller.features.statistics;

import iuh.fit.hotelmanagementemployeeclient.utils.JsonFileUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileAddressController {

    @FXML
    private TextField fileAddressText;

    private String filePath;
    private Stage stage;
    private String preferencesKey;
    private static final String SETTINGS_FILE_PATH = "settings.json"; // Đường dẫn file JSON

    public void initialize(String preferencesKey) {
        this.preferencesKey = preferencesKey;
        String path = JsonFileUtil.readFile(SETTINGS_FILE_PATH, preferencesKey);
        fileAddressText.setText(path != null ? path : "D://Thống kê doanh thu"); // Mặc định nếu null
    }

    @FXML
    void getFileAddress() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        if (file == null) return;
        filePath = file.getAbsolutePath();
        fileAddressText.setText(filePath);
    }

    @FXML
    void cancel() {
        stage.close();
    }

    @FXML
    void comfirm() {
        if (filePath != null && !filePath.isEmpty()) {
            JsonFileUtil.writeFile(SETTINGS_FILE_PATH, preferencesKey, filePath);
        }
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPreferencesKey(String preferencesKey) {
        this.preferencesKey = preferencesKey;
    }
}