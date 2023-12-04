package ee.ut.math.tvt.salessystem.logic;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SoldItem> items = new ArrayList<>();
    @Column(name = "sale_time")
    private LocalTime saleTime;
    @Column(name = "sale_date")
    private LocalDate saleDate;
    @Column(name = "sale_total")
    private float saleTotal;

    private static final Logger log = LogManager.getLogger(ShoppingCart.class);

    public Sale(List<SoldItem> items) {
        this.items = items;
        LocalTime time = LocalTime.now();
        saleTime = LocalTime.of(time.getHour(), time.getMinute(), time.getSecond());
        saleDate = LocalDate.now();
        saleTotal = getSaleTotal(items);
    }

    public Sale() {

    }

    private float getSaleTotal(List<SoldItem> items) {
        float total = 0;
        for(SoldItem item : items){
            total = (float) (total + item.getPrice()*item.getQuantity());
        }
        return total;
    }

    public LocalTime getSaleTime() {
        return saleTime;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public float getSaleTotal() {
        return saleTotal;
    }

    public List<SoldItem> getItems() {
        return items;
    }

    public Long getId() {
        return id;
    }

    /*public void newSale(ShoppingCart shoppingCart){
        this.shoppingCart = shoppingCart;
        this.saleDate = LocalDate.now();
        this.saleTime = LocalTime.now();
    }*/

}