package ee.ut.math.tvt.salessystem.ui.controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;


import java.net.URL;
import java.util.ResourceBundle;

public class StockController implements Initializable {

    private final SalesSystemDAO dao;

    @FXML
    private Button addItem;
    @FXML
    private TableView<StockItem> warehouseTableView;

    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;

    public StockController(SalesSystemDAO dao) {
        this.dao = dao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshStockItems();
        // TODO refresh view after adding new items
    }

    @FXML
    public void refreshButtonClicked() {
        refreshStockItems();
    }

    // adding a function to add items to the warehouse
    @FXML
    public void addItemEventHandler() {
        // Implement the functionality for adding a product here
        // Look if product is already in database to avoid creating duplicate

        // get data from form
        Long barCode = Long.parseLong(barCodeField.getText());
        int amount = Integer.parseInt(quantityField.getText());
        String name = nameField.getText();
        double price = Double.parseDouble(priceField.getText());

        // CREATE NEW STOCKITEM
        StockItem newStockItem = new StockItem();

        if(dao.findStockItem(barCode) == null) {

            // insert data into newStockItem
            newStockItem.setId(barCode);
            newStockItem.setQuantity(amount);
            newStockItem.setName(name);
            newStockItem.setPrice(price);

            // save into sales system dao
            dao.saveStockItem(newStockItem);

        }
        else {
            // insert data into newStockItem
            newStockItem.setId(barCode);
            newStockItem.setQuantity(amount + dao.findStockItem(barCode).getQuantity());
            newStockItem.setName(dao.findStockItem(barCode).getName());
            newStockItem.setPrice(dao.findStockItem(barCode).getPrice());

            // remove existing item
            dao.removeStockItem(dao.findStockItem(barCode));

            // save updated version of the item into sales system dao
            dao.saveStockItem(newStockItem);
        }

        barCodeField.clear();
        quantityField.clear();
        nameField.clear();
        priceField.clear();
    }
    private void refreshStockItems() {
        warehouseTableView.setItems(FXCollections.observableList(dao.findStockItems()));
        warehouseTableView.refresh();
    }
}
