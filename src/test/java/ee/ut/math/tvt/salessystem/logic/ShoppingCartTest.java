package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShoppingCartTest {
    private final SalesSystemDAO dao = new InMemorySalesSystemDAO();
    private final ShoppingCart shoppingCart = new ShoppingCart(dao);

    @Test
    public void testAddingItemBeginsAndCommitsTransaction() throws Exception {

        assertThrows(IllegalArgumentException.class, () -> {
            shoppingCart.submitCurrentPurchase();
        });
        assertTrue(dao.getTestBeginTransaction());
        assertTrue(dao.getTestCommitTransaction());
    }
}