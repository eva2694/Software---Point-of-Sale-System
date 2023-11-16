package ee.ut.math.tvt.salessystem.ui.controllers;

import com.sun.javafx.collections.ObservableListWrapper;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.net.URL;
import java.util.ResourceBundle;

public class StockController implements Initializable {

    private static final Logger log = LogManager.getLogger(StockController.class);
    private final SalesSystemDAO dao;

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

        try{
            // get data from form
            Long barCode = Long.parseLong(barCodeField.getText());
            int amount = Integer.parseInt(quantityField.getText());
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());

            if (barCode < 0 || amount < 0 || price < 0) {
                showWarningDialog("Invalid Input", "Barcode, Quantity, and Price cannot be negative.");
                log.info("Displaying warning dialog for invalid input: negative values.");
                throw new IllegalArgumentException("Barcode, Quantity, and Price cannot be negative");
            }

            // CREATE NEW STOCKITEM
            StockItem newStockItem = new StockItem();

            if(dao.findStockItem(barCode) == null) {
                try {
                    // insert data into newStockItem
                    newStockItem.setId(barCode);
                    newStockItem.setQuantity(amount);
                    newStockItem.setName(name);
                    newStockItem.setPrice(price);

                    // save into sales system dao
                    log.debug("Adding new item to the warehouse: " + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    dao.saveStockItem(newStockItem);
                    log.info("New item added to the warehouse: " + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                }
                catch(Exception e) {
                    e.printStackTrace();
                    log.info("Display of informations failed");
                    log.debug("New item info => name:" + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    log.debug("List of items in warehouse:" + dao.findStockItems());
                }
            }
            else {
                try {
                    // update quantity in stockItemList
                    dao.findStockItem(barCode).setQuantity(dao.findStockItem(barCode).getQuantity() + amount);
                    log.info("Item quantity updated in the warehouse: " + name + " (ID: " + barCode + ") - New Quantity: " + dao.findStockItem(barCode).getQuantity());

                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("Display of informations failed");
                    log.error("Failed to update item quantity in the warehouse", e);
                    log.debug("New item info => name:" + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    log.debug("List of items in warehouse:" + dao.findStockItems());
                }
            }
            barCodeField.clear();
            quantityField.clear();
            nameField.clear();
            priceField.clear();
        }
        catch (NumberFormatException e) {
            showWarningDialog("Invalid Input", "Please enter valid numeric values for Barcode, Quantity, and Price.");
            log.info("Invalid Input, warning message shown: numeric values needed for Barcode, Quantity, and Price.");

        }
    }

    private void showWarningDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void refreshStockItems() {
        warehouseTableView.setItems(FXCollections.observableList(dao.findStockItems()));
        warehouseTableView.refresh();
        log.info("Warehouse view refreshed");
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

    @FXML
    public void addItemEventHandlerTest(String barCodetest, String quantitytest, String nametest, String pricetest) {
        // Method identical to addItemEventHandler but adapted for the test

        try{
            // get data from form
            Long barCode = Long.parseLong(barCodetest);
            int amount = Integer.parseInt(quantitytest);
            String name = nametest;
            double price = Double.parseDouble(pricetest);

            if (amount < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }

            // CREATE NEW STOCKITEM
            StockItem newStockItem = new StockItem();

            if(dao.findStockItem(barCode) == null) {
                try {
                    // insert data into newStockItem
                    newStockItem.setId(barCode);
                    newStockItem.setQuantity(amount);
                    newStockItem.setName(name);
                    newStockItem.setPrice(price);

                    // save into sales system dao
                    dao.saveStockItem(newStockItem);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    log.info("Display of informations failed");
                    log.debug("New item info => name:" + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    log.debug("List of items in warehouse:" + dao.findStockItems());
                }
            }
            else {
                try {
                    // update quantity in stockItemList
                    dao.findStockItem(barCode).setQuantity(dao.findStockItem(barCode).getQuantity() + amount);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("Display of informations failed");
                    log.debug("New item info => name:" + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    log.debug("List of items in warehouse:" + dao.findStockItems());
                }
            }
        }
        catch (NumberFormatException e) {
            showWarningDialog("Invalid Input", "Please enter valid numeric values for Barcode, Quantity, and Price.");
        }
    }

}
