package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


import java.net.URL;
import java.util.ResourceBundle;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "Point-of-sale" in the menu). Consists of the purchase menu,
 * current purchase dialog and shopping cart table.
 */
public class PurchaseController implements Initializable {

    private static final Logger log = LogManager.getLogger(PurchaseController.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart shoppingCart;

    @FXML
    private Button newPurchase;
    @FXML
    private Button submitPurchase;
    @FXML
    private Button cancelPurchase;
    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private ChoiceBox<String> nameChoiceBox;
    @FXML
    private TextField priceField;
    @FXML
    private Button addItemButton;
    @FXML
    private TableView<SoldItem> purchaseTableView;

    public PurchaseController(SalesSystemDAO dao, ShoppingCart shoppingCart) {
        this.dao = dao;
        this.shoppingCart = shoppingCart;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       //
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        purchaseTableView.setItems(FXCollections.observableList(shoppingCart.getAll()));
        disableProductField(true);

        this.barCodeField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    fillInputsBySelectedStockItem();
                }
            }
        });

        List<String> nameList = dao.NameList();
        nameChoiceBox.getItems().addAll(nameList);
    }

    /** Event handler for the <code>new purchase</code> event. */
    @FXML
    protected void newPurchaseButtonClicked() {
        System.out.println("1");
        log.info("New sale process started");
        try {
            enableInputs();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the <code>cancel purchase</code> event.
     */
    @FXML
    protected void cancelPurchaseButtonClicked() {
        System.out.println("2");
        log.info("Sale cancelled");
        try {
            shoppingCart.cancelCurrentPurchase();
            disableInputs();
            purchaseTableView.refresh();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the <code>submit purchase</code> event.
     */
    @FXML
    protected void submitPurchaseButtonClicked() {
        log.info("Sale complete");
        try {
            log.info("Contents of the current basket:\n" + shoppingCart.getAll());

            // Decrease the stock quantity for each item in the shopping cart
            for (SoldItem soldItem : shoppingCart.getAll()) {
                StockItem stockItem = soldItem.getStockItem();
                int soldQuantity = soldItem.getQuantity();
                if (stockItem.getQuantity() < soldQuantity) {
                    SalesSystemException ex = new SalesSystemException("we dont have enough product");
                    log.info("Item quantity exceeded available stock: " + stockItem.getName() + " (ID: " + stockItem.getId() + ") - Requested Quantity: " + soldQuantity + " - Available Quantity: " + stockItem.getQuantity());
                    alert(stockItem);
                    throw ex;
                }
                else {
                    stockItem.setQuantity(stockItem.getQuantity() - soldQuantity);
                }
            }

            shoppingCart.submitCurrentPurchase();
            disableInputs();
            purchaseTableView.refresh();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    // switch UI to the state that allows to proceed with the purchase
    private void enableInputs() {
        resetProductField();
        disableProductField(false);
        cancelPurchase.setDisable(false);
        submitPurchase.setDisable(false);
        newPurchase.setDisable(true);
    }

    // switch UI to the state that allows to initiate new purchase
    private void disableInputs() {
        resetProductField();
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        newPurchase.setDisable(false);
        disableProductField(true);
    }

    private void fillInputsBySelectedStockItem() {
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            nameChoiceBox.setValue(stockItem.getName());
            priceField.setText(String.valueOf(stockItem.getPrice()));
        } else {
            resetProductField();
        }
    }

    // Search the warehouse for a StockItem with the bar code entered
    // to the barCode textfield.
    private StockItem getStockItemByBarcode() {
        try {
            long code = Long.parseLong(barCodeField.getText());
            return dao.findStockItem(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Add new item to the cart.
     */
    @FXML
    public void addItemEventHandler() {
        // add chosen item to the shopping cart.
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText());
                if (stockItem.getQuantity() < quantity) {
                    SalesSystemException ex = new SalesSystemException("we dont have enough product");
                    log.info("Item quantity exceeded available stock: " + stockItem.getName() + " (ID: " + stockItem.getId() + ") - Requested Quantity: " + quantity + " - Available Quantity: " + stockItem.getQuantity());
                    alert(stockItem);
                    throw ex;
                }
                log.debug("Adding item: " + stockItem.getName() + " (ID: " + stockItem.getId() + ") - Quantity: " + quantity);
                shoppingCart.addItem(new SoldItem(stockItem, quantity));

            } catch (NumberFormatException e) {
                quantity = 1;
                log.debug("Adding item: " + stockItem.getName() + " (ID: " + stockItem.getId() + ") - Quantity: " + quantity);
                shoppingCart.addItem(new SoldItem(stockItem, quantity));
            } catch (SalesSystemException e){
                System.out.println("Item quantity exceeded");
            }

            purchaseTableView.refresh();
        }
    }

    /*
    * alert
    * */
    private void alert( StockItem stockItem) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Product Quantity Exceeded");
        alert.setHeaderText(null);
        alert.setContentText("We don't have enough product. Available quantity: " + stockItem.getQuantity());
        alert.showAndWait();
    }

    /**
     * Sets whether or not the product component is enabled.
     */
    private void disableProductField(boolean disable) {
        this.addItemButton.setDisable(disable);
        this.barCodeField.setDisable(disable);
        this.quantityField.setDisable(disable);
        this.nameChoiceBox.setDisable(disable);
        this.priceField.setDisable(disable);
    }

    /**
     * Reset dialog fields.
     */
    private void resetProductField() {
        barCodeField.setText("");
        quantityField.setText("1");
        //nameField.setText("");
        priceField.setText("");
    }


    public void changeNameChoiceBox() {
        String selectedName = nameChoiceBox.getValue();
        StockItem item = dao.findStockItem_Name(selectedName);
        barCodeField.setText(String.valueOf(item.getId()));
        priceField.setText(String.valueOf(item.getPrice()));
        log.debug("User changed selected name in ChoiceBox");
    }
}
