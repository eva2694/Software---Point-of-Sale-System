package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class ShoppingCartTest {
    private final SalesSystemDAO dao = new InMemorySalesSystemDAO();
    private final ShoppingCart shoppingCart = new ShoppingCart(dao);

    @Test
    public void testAddingExistingItem() throws Exception {
        SoldItem newItem = new SoldItem(
                dao.findStockItem(1L),
                3
        );

        shoppingCart.addItem(newItem);

        SoldItem existingItem = new SoldItem(
                dao.findStockItem(1L),
                2
        );

        shoppingCart.addItem(existingItem);

        List<SoldItem> cartItems = shoppingCart.getAll();
        assertEquals(1, cartItems.size());
        assertEquals(5, cartItems.get(0).getQuantity().intValue());
    }

    @Test
    public void testAddingNewItem() throws Exception {
        SoldItem newItem = new SoldItem(
                dao.findStockItem(2L),
                4
        );

        shoppingCart.addItem(newItem);

        List<SoldItem> cartItems = shoppingCart.getAll();
        assertEquals(1, cartItems.size());
        assertEquals(4, cartItems.get(0).getQuantity().intValue());
    }

    @Test(expected = SalesSystemException.class)
    public void testAddingItemWithNegativeQuantity() throws Exception {
        SoldItem newItem = new SoldItem(
                dao.findStockItem(3L),
                -1
        );

        shoppingCart.addItem(newItem);
    }

    @Test(expected = SalesSystemException.class)
    public void testAddingItemWithQuantityTooLarge() throws Exception {
        SoldItem newItem = new SoldItem(
                dao.findStockItem(4L),
                9999999 // Larger quantity than available in the warehouse
        );

        shoppingCart.addItem(newItem);
    }

    @Test(expected = SalesSystemException.class)
    public void testAddingItemWithQuantitySumTooLarge() throws Exception {

        SoldItem newItem = new SoldItem(
                dao.findStockItem(1L),
                3
        );

        shoppingCart.addItem(newItem);

        SoldItem existingItem = new SoldItem(
                dao.findStockItem(1L),
                99999
        );

        shoppingCart.addItem(existingItem);
    }


    @Test
    public void testSubmittingCurrentPurchaseDecreasesStockItemQuantity() throws Exception {
        StockItem newStockItem = new StockItem(5L, "New Item", "test", 10.0, 20);
        dao.saveStockItem(newStockItem);

        SoldItem existingItem = new SoldItem(
                dao.findStockItem(1L),
                3
        );
        int expectedQuantity1 = dao.findStockItem(1L).getQuantity() - 3;
        SoldItem newItem = new SoldItem(
                dao.findStockItem(5L),
                5
        );
        int expectedQuantity2 = dao.findStockItem(5L).getQuantity() - 5;

        shoppingCart.addItem(existingItem);
        shoppingCart.addItem(newItem);
        shoppingCart.submitCurrentPurchase();

        StockItem stockItem1 = existingItem.getStockItem();
        StockItem stockItem2 = newItem.getStockItem();
        assertEquals(expectedQuantity1, stockItem1.getQuantity());
        assertEquals(expectedQuantity2, stockItem2.getQuantity());

    }


    @Test
    public void testSubmittingCurrentPurchaseBeginsAndCommitsTransaction() {
        SalesSystemDAO daoTest = new HibernateSalesSystemDAO();
        ShoppingCart shoppingCartTest = new ShoppingCart(daoTest);

        StockItem newStockItem = new StockItem(5L, "test", "test", 10.0, 20);
        daoTest.saveStockItem(newStockItem);

        SoldItem newItem = new SoldItem(
                daoTest.findStockItem(5L),
                5
        );
        shoppingCartTest.addItem(newItem);
        shoppingCartTest.submitCurrentPurchase();

        assertTrue(daoTest.getTestBeginTransaction());
        assertTrue(daoTest.getTestCommitTransaction());
    }


    @Test
    public void testSubmittingCurrentOrderCreatesSaleItem() {
        SalesSystemDAO daoTest = new HibernateSalesSystemDAO();
        ShoppingCart shoppingCartTest = new ShoppingCart(daoTest);

        StockItem stockItem = new StockItem(1L, "test", "test", 100.0, 32);
        daoTest.saveStockItem(stockItem);

        SoldItem soldItem = new SoldItem(stockItem, 5);
        shoppingCartTest.addItem(soldItem);

        shoppingCartTest.submitCurrentPurchase();

        List<Sale> sales = daoTest.findSales();
        System.out.println(sales.size());

        // Check that a sale was created
        assertFalse(sales.isEmpty());

        // Retrieve the first sale (assuming there is at least one)
        Sale createdSale = sales.get(sales.size()-1);

        // Check that the sale contains the correct sold items
        List<SoldItem> soldItemsInSale = createdSale.getItems();
        assertEquals(1, soldItemsInSale.size());

        SoldItem soldItemInSale = soldItemsInSale.get(0);

        assertEquals(stockItem.getId(), soldItemInSale.getStockItem().getId());
        assertEquals(soldItem.getQuantity(), soldItemInSale.getQuantity());
    }

    @Test
    public void testSubmittingCurrentOrderSavesCorrectTime() {
        SalesSystemDAO daoTest = new HibernateSalesSystemDAO();
        ShoppingCart shoppingCartTest = new ShoppingCart(daoTest);

        SoldItem newItem = new SoldItem(dao.findStockItem(1L), 2);
        shoppingCartTest.addItem(newItem);

        shoppingCartTest.submitCurrentPurchase();

        List<Sale> sales = daoTest.findSales();
        Sale createdSale = sales.get(sales.size()-1);

        // Check that the sale time is set correctly
        assertNotNull(createdSale.getSaleTime());

        // Check that the sale time is close to the current time
        LocalTime currentTime = LocalTime.now();
        Duration timeDifference = Duration.between(currentTime, createdSale.getSaleTime());
        assertTrue(timeDifference.getSeconds() < 10);
    }

    @Test
    public void testCancellingOrder() {
        SalesSystemDAO daoTest = new HibernateSalesSystemDAO();
        ShoppingCart shoppingCartTest = new ShoppingCart(daoTest);

        List<Sale> previousSales = daoTest.findSales();

        // Create the first order
        StockItem item1 = new StockItem(1L, "Item 1", "test", 10.0, 10);
        StockItem item2 = new StockItem(2L, "Item 2", "test", 15.0, 15);

        // Add items to the shopping cart
        SoldItem soldItem1 = new SoldItem(item1, 2);
        SoldItem soldItem2 = new SoldItem(item2, 3);

        shoppingCartTest.addItem(soldItem1);
        shoppingCartTest.addItem(soldItem2);
        shoppingCartTest.cancelCurrentPurchase();

        // Cancel the first purchase
        shoppingCartTest.cancelCurrentPurchase();

        // Create and submit a new order with different items
        StockItem newItem1 = new StockItem(3L, "New Item 1", "test", 20.0, 5);
        StockItem newItem2 = new StockItem(4L, "New Item 2", "test", 25.0, 8);

        // Add new items to the shopping cart and submit the new order
        SoldItem newSoldItem1 = new SoldItem(newItem1, 1);
        SoldItem newSoldItem2 = new SoldItem(newItem2, 2);

        shoppingCartTest.addItem(newSoldItem1);
        shoppingCartTest.addItem(newSoldItem2);
        shoppingCartTest.submitCurrentPurchase();

        // Check that just the new order is saved
        List<Sale> sales = daoTest.findSales();
        assertEquals(previousSales.size()+1, sales.size());

        // Retrieve the items from the new order and ensure they are correct
        Sale newOrder = sales.get(sales.size()-1);
        List<SoldItem> newOrderItems = newOrder.getItems();

        // Ensure that the items from the second order are present in the items of the new order
        assertEquals(2, newOrderItems.size());
        assertEquals(newItem1.getId(), newOrderItems.get(0).getStockItem().getId());
        assertEquals(newItem2.getId(), newOrderItems.get(1).getStockItem().getId());


    }

    @Test
    public void testCancellingOrderQuantitiesUnchanged() {
        SalesSystemDAO daoTest = new HibernateSalesSystemDAO();
        ShoppingCart shoppingCartTest = new ShoppingCart(daoTest);

        StockItem item1 = new StockItem(1L, "Item 1", "test", 10.0, 10);
        StockItem item2 = new StockItem(2L, "Item 2", "test", 15.0, 15);

        SoldItem soldItem1 = new SoldItem(item1, 2);
        SoldItem soldItem2 = new SoldItem(item2, 3);

        shoppingCartTest.addItem(soldItem1);
        shoppingCartTest.addItem(soldItem2);

        shoppingCartTest.cancelCurrentPurchase();

        // Check that the quantities of the related StockItems are unchanged
        assertEquals(10, item1.getQuantity());
        assertEquals(15, item2.getQuantity());
    }


}