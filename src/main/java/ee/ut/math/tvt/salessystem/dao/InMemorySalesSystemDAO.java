package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Sale;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class InMemorySalesSystemDAO implements SalesSystemDAO {

    private List<StockItem> stockItemList;
    private final List<SoldItem> soldItemList;
    private final List<Sale> salesList;

    boolean testBeginTransaction = false;
    boolean testCommitTransaction = false;


    public InMemorySalesSystemDAO() {
        List<StockItem> items = new ArrayList<StockItem>();
        items.add(new StockItem(1L, "Lays chips", "Potato chips", 11.0, 5));
        items.add(new StockItem(2L, "Chupa-chups", "Sweets", 8.0, 8));
        items.add(new StockItem(3L, "Frankfurters", "Beer sauseges", 15.0, 12));
        items.add(new StockItem(4L, "Free Beer", "Student's delight", 0.0, 100));
        this.stockItemList = items;
        this.soldItemList = new ArrayList<>();
        this.salesList = new ArrayList<>();
    }

    @Override
    public List<StockItem> findStockItems() {
        return stockItemList;
    }

    public List<SoldItem> findSoldItems() {
        return soldItemList;
    }
    public List<Sale> findSales() {return salesList; }

    @Override
    public StockItem findStockItem(long id) {
        for (StockItem item : stockItemList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    public List<String> NameList() {
        List<String> names = new ArrayList<>();
        for (StockItem item : stockItemList) {
            names.add(item.getName());
        }
        return names;
    }

    public StockItem findStockItem_Name(String name) {
        for (StockItem item : stockItemList) {
            if (item.getName() == name)
                return item;
        }
        return null;
    }

    @Override
    public void saveSoldItem(SoldItem item) {
        soldItemList.add(item);
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
            stockItemList.add(stockItem);
        }

    @Override
    public void removeStockItem(StockItem stockItem) {
        List<StockItem> updatedList = new ArrayList<>(stockItemList);
        for (StockItem item : stockItemList) {
            if (item.getId().equals(stockItem.getId()))
            {
                updatedList.remove(stockItem);
            }
        }
        stockItemList = updatedList;
    }

    @Override
    public void beginTransaction() {
        testBeginTransaction = true;
    }

    @Override
    public void rollbackTransaction() {
    }

    @Override
    public void commitTransaction() {
        testCommitTransaction = true;
        List<SoldItem> shoppingCart = new ArrayList<>();
        for (SoldItem item : soldItemList) {
            shoppingCart.add(item.copy());
        }
        Sale sale = new Sale(shoppingCart);
        salesList.add(sale);
        soldItemList.clear();
    }
    @Override
    public boolean getTestBeginTransaction() {
        return testBeginTransaction;
    }
    public boolean getTestCommitTransaction() {
        return testCommitTransaction;
    }

}
