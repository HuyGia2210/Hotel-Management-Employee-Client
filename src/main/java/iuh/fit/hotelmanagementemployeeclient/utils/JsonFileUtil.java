package iuh.fit.hotelmanagementemployeeclient.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class JsonFileUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void writeFile(String filePath, String key, String value) {
        try {
            File file = new File(filePath);
            ObjectNode json;

            // Đọc JSON hiện tại hoặc tạo mới
            if (file.exists()) {
                json = (ObjectNode) mapper.readTree(file);
            } else {
                json = mapper.createObjectNode();
            }

            // Cập nhật key-value
            json.put(key, value);

            // Ghi lại vào file
            mapper.writeValue(file, json);
        } catch (IOException e) {
            throw new RuntimeException("Không thể ghi file JSON: " + filePath, e);
        }
    }

    public static String readFile(String filePath, String key) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null; // Hoặc trả về đường dẫn mặc định nếu cần
            }

            ObjectNode json = (ObjectNode) mapper.readTree(file);
            return json.has(key) ? json.get(key).asText() : null;
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file JSON: " + filePath, e);
        }
    }
}