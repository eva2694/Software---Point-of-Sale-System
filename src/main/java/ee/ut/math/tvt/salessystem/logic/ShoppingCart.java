package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        validateAndAddItem(soldItem);
        log.debug("Added " + soldItem.getName() + " quantity of " + soldItem.getQuantity());
    }

    private void validateAndAddItem(SoldItem soldItem) {
        StockItem stockItem = soldItem.getStockItem();
        int soldQuantity = soldItem.getQuantity();

        if (stockItem.getQuantity() < soldQuantity || soldQuantity < 1) {
            throw new SalesSystemException("Invalid product quantity");
        }

        boolean found = false;
        for (SoldItem item : items) {
            if (item.getId().equals(soldItem.getId())) {
                item.setQuantity(item.getQuantity() + soldItem.getQuantity());
                found = true;
                break;
            }
        }

        if (!found) {
            items.add(soldItem);
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
            if(items.isEmpty()) {
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
            } else {
                log.debug("Shopping cart is empty. Nothing to submit.");
            }
        } catch (Exception e) {
            log.error("Error submitting the purchase: " + e.getMessage());
            throw new SalesSystemException("Error submitting the purchase: " + e.getMessage(), e);
        }
    }



}
