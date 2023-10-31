package ee.ut.math.tvt.salessystem.logic;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private static final Logger log = LogManager.getLogger(ShoppingCart.class);

    private List<SoldItem> items = new ArrayList<>();
    private LocalTime saleTime;
    private LocalDate saleDate;



    public Sale(List<SoldItem> items) {
        this.items = items;
        LocalTime time = LocalTime.now();
        saleTime = LocalTime.of(time.getHour(), time.getMinute(), time.getSecond());
        saleDate = LocalDate.now();
    }

    public LocalTime getSaleTime() {
        return saleTime;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    /*public void newSale(ShoppingCart shoppingCart){
        this.shoppingCart = shoppingCart;
        this.saleDate = LocalDate.now();
        this.saleTime = LocalTime.now();
    }*/

}
