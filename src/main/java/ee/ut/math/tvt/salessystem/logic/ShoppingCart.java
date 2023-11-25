package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;



public class ShoppingCart {

    private static final Logger log = LogManager.getLogger(ShoppingCart.class);
    private final SalesSystemDAO dao;
    private final List<SoldItem> items = new ArrayList<>();


    public ShoppingCart(SalesSystemDAO dao) {
        this.dao = dao;
    }

    /**
     * Add new SoldItem to table.
     */
    public void addItem(SoldItem soldItem) {
        // TODO verify that warehouse items' quantity remains at least zero or throw an exception
        StockItem stockItem = soldItem.getStockItem();
        int soldQuantity = soldItem.getQuantity();
        if(stockItem.getQuantity() < soldQuantity){
            // TODO throwing exeption is repeated here and in PurchaseController!
            SalesSystemException ex1 = new SalesSystemException("we dont have enough product");
            log.info("Item quantity exceeded available stock: " + stockItem.getName() + " (ID: " + stockItem.getId() + ") - Requested Quantity: " + soldQuantity + " - Available Quantity: " + stockItem.getQuantity());
            throw ex1;
        }
        if (soldQuantity < 1) {
            SalesSystemException ex2 = new SalesSystemException("Product quantity less then 1");
            throw ex2;
        } else {
            boolean flag = false;
            for (SoldItem item : items) {
                if (item.getId() == soldItem.getId()) {
                    item.setQuantity(item.getQuantity() + soldItem.getQuantity());
                    flag = true;
                }
            }

            /*
            * if () {

                    }
            * */

            if (!flag) {
                items.add(soldItem);
            }
            log.debug("Added " + soldItem.getName() + " quantity of " + soldItem.getQuantity());
        }
    }



    public List<SoldItem> getAll() {
        return items;
    }

    public void cancelCurrentPurchase() {
        log.debug("Current purchase canceled. Clearing the shopping cart.");
        items.clear();
    }

    public void submitCurrentPurchase() {
        log.debug("Submitting the current purchase.");

        try {
            // Decrease the stock quantity for each item in the shopping cart
            for (SoldItem soldItem : items) {
                StockItem stockItem = soldItem.getStockItem();
                int soldQuantity = soldItem.getQuantity();
                if (stockItem.getQuantity() < soldQuantity) {
                    SalesSystemException ex = new SalesSystemException("We don't have enough product");
                    log.info("Item quantity exceeded available stock: " + stockItem.getName() + " (ID: " + stockItem.getId() + ") - Requested Quantity: " + soldQuantity + " - Available Quantity: " + stockItem.getQuantity());
                    throw ex;
                } else {
                    stockItem.setQuantity(stockItem.getQuantity() - soldQuantity);
                    dao.saveStockItem(stockItem);
                }
            }

            dao.saveSoldItemsAndCreateSale(items);
            items.clear();
            log.debug("Shopping cart cleared after submitting the purchase.");
        } catch (Exception e) {
            log.error("Error submitting the purchase: " + e.getMessage());
            throw new SalesSystemException("Error submitting the purchase: " + e.getMessage(), e);
        }
    }



}
