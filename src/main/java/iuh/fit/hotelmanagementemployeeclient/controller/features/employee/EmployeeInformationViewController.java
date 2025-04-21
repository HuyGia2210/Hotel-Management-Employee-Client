package iuh.fit.hotelmanagementemployeeclient.controller.features.employee;

import iuh.fit.models.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

public class EmployeeInformationViewController {
    @FXML
    private TextArea addressTextAria;

    @FXML
    private TextField employeeIDTextField, fullNameTextField,
            phoneNumberTextField, genderTextField,
            cardIDTextField, dobTextField,
            positionTextField;



    public void setEmployee(Employee employee) {
        employeeIDTextField.setText(employee.getEmployeeCode());
        fullNameTextField.setText(employee.getFullName());
        phoneNumberTextField.setText(employee.getPhoneNumber());
        addressTextAria.setText(employee.getAddress());
        cardIDTextField.setText(employee.getIdCardNumber());
        positionTextField.setText(employee.getPosition().name());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        dobTextField.setText(dateTimeFormatter.format(employee.getDob()));
        genderTextField.setText(employee.getGender().name());

    }

}
