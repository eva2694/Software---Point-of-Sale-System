package ee.ut.math.tvt.salessystem.ui.controllers;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Sale;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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

    public HistoryController(SalesSystemDAO dao) {this.dao = dao;}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: implement
        salesList = dao.findSales();
        refreshHistoryView();
    }

    @FXML
    public void showHistory() {refreshHistoryView(); }

    private void refreshHistoryView() {
        salesList = dao.findSales();
        purchaseHistoryTableView.setItems(FXCollections.observableList(salesList));
        purchaseHistoryTableView.refresh();
    }


}
