module iuh.fit.hotelmanagementemployeeclient {
    requires iuh.fit;

    requires javafx.fxml;
    requires javafx.web;

    // JPA
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Khác
    requires static lombok;
    requires net.datafaker;
    requires com.dlsc.gemsfx;
    requires com.dlsc.unitfx;
    requires com.calendarfx.view;


    // xuat exel
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.rmi;
    requires java.naming;
    requires org.checkerframework.checker.qual;
    requires itextpdf;
    requires org.apache.pdfbox;

    // Opens
//    opens iuh.fit to javafx.fxml;

    // open excel
    requires java.desktop;
    requires com.fasterxml.jackson.databind;

//    opens iuh.fit.controller.features.statistics to javafx.fxml;
//
//    opens iuh.fit.devtools to javafx.fxml;
//
//
//    opens iuh.fit.models to jakarta.persistence, org.hibernate.orm.core;
//    opens iuh.fit.models.wrapper to javafx.fxml;
//    opens iuh.fit.models.enums to javafx.fxml;
//
//    opens iuh.fit.security to javafx.fxml;
//
//    opens iuh.fit.utils to javafx.fxml;
//
//    opens iuh.fit.dao.daoimpl to javafx.fxml;
//
//
//    // Exports
//    opens iuh.fit.dao.daointerface to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient to javafx.fxml;


    opens iuh.fit.hotelmanagementemployeeclient.controller to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.customer to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.employee to javafx.fxml;


    opens iuh.fit.hotelmanagementemployeeclient.controller.features.room to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_in_reservation_list_controllers to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.room.checking_out_controllers to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.room.creating_reservation_form_controllers to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.room.room_changing_controllers to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.room.service_ordering_controllers to javafx.fxml;

    opens iuh.fit.hotelmanagementemployeeclient.controller.features.invoice to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.service to javafx.fxml;
    opens iuh.fit.hotelmanagementemployeeclient.controller.features.statistics to javafx.fxml;


    exports iuh.fit.hotelmanagementemployeeclient;
}