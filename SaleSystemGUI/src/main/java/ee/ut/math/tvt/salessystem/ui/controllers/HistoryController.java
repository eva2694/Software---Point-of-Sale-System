package ee.ut.math.tvt.salessystem.ui.controllers;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Sale;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.Initializable;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "History" in the menu).
 */
public class HistoryController implements Initializable {

    private static final Logger log = LogManager.getLogger(HistoryController.class);
    private final SalesSystemDAO dao;
    private List<Sale> salesList;

    @FXML
    private TableView<Sale> purchaseHistoryTableView;
    @FXML
    private TableView<SoldItem> historyDetailsTableView;
    @FXML
    private DatePicker start_date;
    @FXML
    private DatePicker end_date;

    public HistoryController(SalesSystemDAO dao) {this.dao = dao;}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: implement
        salesList = dao.findSales();
        refreshHistoryView(0);
        this.purchaseHistoryTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                Sale selectedSale = purchaseHistoryTableView.getSelectionModel().getSelectedItem();
                if (selectedSale != null) {
                    displaySaleDetails(selectedSale);
                    log.debug("Selected Sale: " + selectedSale);
                    log.debug("Sale items:" + selectedSale.getItems());
                }
            }
        });
    }

    @FXML
    public void showHistory() {
        refreshHistoryView(0);
        log.info("Button Show All was pressed in History Tab.");
    }
    @FXML
    public void showLastTen() {
        refreshHistoryView(1);
        log.info("Button Show Last 10 was pressed in History Tab.");
    }
    @FXML
    public void showBetweenDates() {
        refreshHistoryView(2);
        log.info("Button Show Between Dates was pressed in History Tab.");
    }


    private void refreshHistoryView(int choice) {
        salesList = dao.findSales();
        switch (choice) {
            case 0:
                purchaseHistoryTableView.setItems(FXCollections.observableList(salesList));
                break;
            case 1:
                List<Sale> lastTen = new ArrayList<>(salesList.subList(Math.max(salesList.size()-10, 0),salesList.size()));
                purchaseHistoryTableView.setItems(FXCollections.observableList(lastTen));
                break;
            case 2:
                try {
                    LocalDate start = start_date.getValue();
                    LocalDate end = end_date.getValue();
                    List<Sale> betweenDate = salesList.stream()
                            .filter(Sale -> isDateInRange(Sale.getSaleDate(), start, end)).collect(Collectors.toList());
                    purchaseHistoryTableView.setItems(FXCollections.observableList(betweenDate));
                } catch (Exception e) {
                    alert();
                    e.printStackTrace();
                    log.error("Date were not selected when button was pressed.");
                }
                break;
        }
        purchaseHistoryTableView.refresh();
    }

    private void alert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Date range not selected");
        alert.setHeaderText(null);
        alert.setContentText("You have to select date range to see sales between dates.");
        alert.showAndWait();
    }

    private boolean isDateInRange(LocalDate saleDate, LocalDate start, LocalDate end) {
        return !saleDate.isBefore(start) && !saleDate.isAfter(end);
    }

    private void displaySaleDetails(Sale sale){
        historyDetailsTableView.setItems(FXCollections.observableList(dao.findSale(sale.getId()).getItems()));
        historyDetailsTableView.refresh();
        }
}
