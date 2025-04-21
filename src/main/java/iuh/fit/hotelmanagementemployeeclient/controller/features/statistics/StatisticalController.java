package iuh.fit.hotelmanagementemployeeclient.controller.features.statistics;

import iuh.fit.hotelmanagementemployeeclient.utils.ExportFileHelper;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class StatisticalController implements Initializable {

    private static InvoiceRevenueStatisticsTabController invoiceRevenueStatisticsTabController;
    private static ServiceRevenueStatisticsTabController serviceRevenueStatisticsTabController;
    private static RoomRevenueStatisticsTabController roomRevenueStatisticsTabController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String invoiceUIResource = "/iuh/fit/hotelmanagementemployeeclient/view/features/statistics/InvoiceRevenueStatisticsTab.fxml";
        String serviceUIResource = "/iuh/fit/hotelmanagementemployeeclient/view/features/statistics/ServiceRevenueStatisticsTab.fxml";
        String roomUIResource = "/iuh/fit/hotelmanagementemployeeclient/view/features/statistics/RoomRevenueStatisticsTab.fxml";
        invoiceRevenueStatisticsTabController = loadUI(invoiceUIResource).getController();
        serviceRevenueStatisticsTabController = loadUI(serviceUIResource).getController();
        roomRevenueStatisticsTabController = loadUI(roomUIResource).getController();
    }

    private FXMLLoader loadUI(String resource){
        FXMLLoader fxmlLoader = new FXMLLoader(
                Objects.requireNonNull(
                        ExportFileHelper.class.getResource(resource)
                )
        );
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fxmlLoader;
    }

    public static InvoiceRevenueStatisticsTabController getInvoiceRevenueStatisticsTabController(){
        return invoiceRevenueStatisticsTabController;
    }

    public static ServiceRevenueStatisticsTabController getServiceRevenueStatisticsTabController(){
        return serviceRevenueStatisticsTabController;
    }

    public static RoomRevenueStatisticsTabController getRoomRevenueStatisticsTabController(){
        return roomRevenueStatisticsTabController;
    }
}
