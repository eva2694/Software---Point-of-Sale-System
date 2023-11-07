package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class InMemorySalesSystemDAOTest {

    private final SalesSystemDAO dao = new InMemorySalesSystemDAO();

    @Test
    public void testAddingNewItem() throws Exception {
        int initialItemCount = dao.findStockItems().size();

        StockItem newItem = new StockItem(12L, "test", "test", 10, 5);

        dao.saveStockItem(newItem);

        assertEquals(dao.findStockItems().size(), initialItemCount + 1);
        assertEquals(newItem, dao.findStockItems().get(dao.findStockItems().size() - 1));
    }
}



