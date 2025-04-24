package iuh.fit.hotelmanagementemployeeclient.utils;

import iuh.fit.models.enums.*;
import iuh.fit.hotelmanagementemployeeclient.utils.ErrorMessages;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ConvertHelper {
    // ==================================================================================================================
    // Database to Application Converter
    // ==================================================================================================================
    public static Position positionConverter(String input) {
        if (!input.matches("(MANAGER|RECEPTIONIST)"))
            throw new IllegalArgumentException(ErrorMessages.CONVERT_HELPER_INVALID_POSITION);

        return input.equalsIgnoreCase("MANAGER")
                ? Position.MANAGER : Position.RECEPTIONIST;
    }

    public static AccountStatus accountStatusConverter(String input) {
        return switch (input.toUpperCase()) {
            case "ACTIVE" -> AccountStatus.ACTIVE;
            case "LOCKED" -> AccountStatus.LOCKED;
            default -> throw new IllegalArgumentException(ErrorMessages.CONVERT_HELPER_INVALID_ACCOUNT_STATUS);
        };
    }

    // ==================================================================================================================
    // Others
    // ==================================================================================================================
    public static double doubleConverter(String numbStr, String errorMessage) {
        try {
            return Double.parseDouble(numbStr);
        } catch (Exception exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
