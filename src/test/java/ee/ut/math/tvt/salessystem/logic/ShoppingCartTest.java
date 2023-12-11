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
        SoldItem newItem = new SoldItem(
                dao.findStockItem(1L),
                5
        );
        shoppingCart.addItem(newItem);
        shoppingCart.submitCurrentPurchase();

        assertTrue(dao.getTestBeginTransaction());
        assertTrue(dao.getTestCommitTransaction());
    }


    @Test
    public void testSubmittingCurrentOrderCreatesSaleItem() {
        SoldItem soldItem = new SoldItem(
                dao.findStockItem(1L),
                3
        );
        shoppingCart.addItem(soldItem);

        shoppingCart.submitCurrentPurchase();

        List<Sale> sales = dao.findSales();
        System.out.println(sales.size());

        // Check that a sale was created
        assertFalse(sales.isEmpty());

        // Retrieve the first sale (assuming there is at least one)
        Sale createdSale = sales.get(sales.size()-1);

        // Check that the sale contains the correct sold items
        List<SoldItem> soldItemsInSale = createdSale.getItems();
        assertEquals(1, soldItemsInSale.size());

        SoldItem soldItemInSale = soldItemsInSale.get(0);

        assertEquals(soldItem.getId(), soldItemInSale.getStockItem().getId());
        assertEquals(soldItem.getQuantity(), soldItemInSale.getQuantity());
    }

    @Test
    public void testSubmittingCurrentOrderSavesCorrectTime() {
        SoldItem newItem = new SoldItem(dao.findStockItem(1L), 2);
        shoppingCart.addItem(newItem);

        shoppingCart.submitCurrentPurchase();

        List<Sale> sales = dao.findSales();
        Sale createdSale = sales.get(sales.size()-1);

        // Check that the sale time is set correctly
        assertNotNull(createdSale.getSaleTime());

        // Check that the sale time is close to the current time
        LocalTime currentTime = LocalTime.now();
        Duration timeDifference = Duration.between(currentTime, createdSale.getSaleTime());
        assertTrue(timeDifference.getSeconds() < 1);
    }

    @Test
    public void testCancellingOrder() {
        // Create the first order
        SoldItem soldItem1 = new SoldItem(dao.findStockItem(1L), 2);
        SoldItem soldItem2 = new SoldItem(dao.findStockItem(2L), 3);

        shoppingCart.addItem(soldItem1);
        shoppingCart.addItem(soldItem2);

        // Cancel the first purchase
        shoppingCart.cancelCurrentPurchase();

        // Create and submit a new order with different items
        SoldItem newSoldItem1 = new SoldItem(dao.findStockItem(3L), 1);
        SoldItem newSoldItem2 = new SoldItem(dao.findStockItem(4L), 2);

        shoppingCart.addItem(newSoldItem1);
        shoppingCart.addItem(newSoldItem2);
        shoppingCart.submitCurrentPurchase();

        // Check that just the new order is saved
        List<Sale> sales = dao.findSales();
        System.out.println(sales.size());

        assertEquals(1, sales.size());

        // Retrieve the items from the new order and ensure they are correct
        Sale newOrder = sales.get(sales.size()-1);
        List<SoldItem> newOrderItems = newOrder.getItems();

        // Ensure that the items from the second order are present in the items of the new order
        assertEquals(2, newOrderItems.size());
        assertEquals(newSoldItem1.getId(), newOrderItems.get(0).getStockItem().getId());
        assertEquals(newSoldItem2.getId(), newOrderItems.get(1).getStockItem().getId());
    }

    @Test
    public void testCancellingOrderQuantitiesUnchanged() {
        SoldItem soldItem1 = new SoldItem(dao.findStockItem(1L), 2);
        int expectedQuantity1 = dao.findStockItem(1L).getQuantity();
        SoldItem soldItem2 = new SoldItem(dao.findStockItem(2L), 3);
        int expectedQuantity2 = dao.findStockItem(2L).getQuantity();

        shoppingCart.addItem(soldItem1);
        shoppingCart.addItem(soldItem2);

        shoppingCart.cancelCurrentPurchase();

        // Check that the quantities of the related StockItems are unchanged
        assertEquals(expectedQuantity1, dao.findStockItem(1L).getQuantity());
        assertEquals(expectedQuantity2, dao.findStockItem(2L).getQuantity());
    }


}