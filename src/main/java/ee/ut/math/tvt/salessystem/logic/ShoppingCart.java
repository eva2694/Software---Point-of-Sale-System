package ee.ut.math.tvt.salessystem.logic;

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
        boolean flag = false;
        for (SoldItem item : items) {
            if (item.getId() == soldItem.getId()) {
                item.setQuantity(item.getQuantity()+soldItem.getQuantity());
                flag=true;
            }
        }
        if(!flag) {
            items.add(soldItem);
        }
        log.debug("Added " + soldItem.getName() + " quantity of " + soldItem.getQuantity());
    }

    public List<SoldItem> getAll() {
        return items;
    }

    public void cancelCurrentPurchase() {
        log.debug("Current purchase canceled. Clearing the shopping cart.");
        items.clear();
    }

    public void submitCurrentPurchase() {
        // TODO decrease quantities of the warehouse stock

        // note the use of transactions. InMemorySalesSystemDAO ignores transactions
        // but when you start using hibernate in lab5, then it will become relevant.
        // what is a transaction? https://stackoverflow.com/q/974596
        log.debug("Submitting the current purchase.");
        dao.beginTransaction();
        if (!dao.getTestBeginTransaction()) {
            throw new IllegalArgumentException("beginTransaction() wasn't called");
        }
        try {
            for (SoldItem item : items) {
                dao.saveSoldItem(item);
            }
            dao.commitTransaction();
            if (!dao.getTestCommitTransaction()) {
                throw new IllegalArgumentException("CommitTransaction() wasn't called");
            }
            items.clear();
            log.debug("Transaction committed. Shopping cart cleared.");
        } catch (Exception e) {
            log.error("Error submitting the purchase: " + e.getMessage());
            dao.rollbackTransaction();
            throw e;
        }
    }
}
