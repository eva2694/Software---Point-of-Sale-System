package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ShoppingCartTest {
    private final SalesSystemDAO dao = new InMemorySalesSystemDAO();
    private final ShoppingCart shoppingCart = new ShoppingCart(dao);

    // TODO make this test work
    @Test
    public void testAddingItemBeginsAndCommitsTransaction() throws Exception {
        shoppingCart.addItem(new SoldItem(dao.findStockItem(1L), 1));
        shoppingCart.submitCurrentPurchase();
        assertTrue(dao.getTestBeginTransaction());
        assertTrue(dao.getTestCommitTransaction());
    }

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


}