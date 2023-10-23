package ee.ut.math.tvt.salessystem.ui.controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
        this.barCodeField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    fillInputsBySelectedStockItem();
                }
            }
        });
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


    private void fillInputsBySelectedStockItem() {
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            nameField.setText(stockItem.getName());
            priceField.setText(String.valueOf(stockItem.getPrice()));
        } else {
            String barcodeText = barCodeField.getText();
            try {
                int code = Integer.parseInt(barcodeText);
            } catch (NumberFormatException e) {
                resetProductField();
            }
        }
    }

    private StockItem getStockItemByBarcode() {
        try {
            long code = Long.parseLong(barCodeField.getText());
            return dao.findStockItem(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void resetProductField() {
        barCodeField.setText("");
        quantityField.setText("1");
        nameField.setText("");
        priceField.setText("");
    }
}
