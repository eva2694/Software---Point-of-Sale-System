package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

public class StockControllerTest {

    private final SalesSystemDAO dao = new InMemorySalesSystemDAO();
    private final StockController stockController = new StockController(dao);

    public StockControllerTest() throws IOException {
    }

    @Test
    public void testAddingExistingItem() throws Exception{
        int initialItemCount = dao.findStockItems().size();
        int initialQuantity = dao.findStockItem(1L).getQuantity();

        int newQuantity = 10;

        stockController.addItemEventHandlerTest("1",Integer.toString(newQuantity),"Lays chips","11.0");

        assertEquals(dao.findStockItem(1L).getQuantity(), initialQuantity + newQuantity);
        assertEquals(dao.findStockItems().size(), initialItemCount);
    }

    @Test
    public void testAddingItemWithNegativeQuantity() throws Exception{
        int initialItemCount = dao.findStockItems().size();

        int negativeQuantity = -10;

        assertThrows(IllegalArgumentException.class, () -> {
            stockController.addItemEventHandlerTest("1",Integer.toString(negativeQuantity),"Lays chips","11.0");
        });
        assertEquals(dao.findStockItems().size(), initialItemCount);
    }

}