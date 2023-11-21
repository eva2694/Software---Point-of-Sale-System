package ee.ut.math.tvt.salessystem.dataobjects;
import ee.ut.math.tvt.salessystem.logic.Sale;

import javax.persistence.*;


/**
 * Already bought StockItem. SoldItem duplicates name and price for preserving history.
 */
@Entity
@Table(name = "sold_item")
public class SoldItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "stock_item_id")
    private StockItem stockItem;
    @Column(name = "name")
    private String name;
    @Column(name = "quantity")

    private Integer quantity;
    @Column(name = "price")
    private double price;
    @Transient // This field won't be persisted
    private double sum;

    public SoldItem() {
    }

    public SoldItem(StockItem stockItem, int quantity) {
        this.id = stockItem.getId();
        this.stockItem = stockItem;
        this.name = stockItem.getName();
        this.price = stockItem.getPrice();
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public double getSum() {
        return price * ((double) quantity);
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public StockItem getStockItem() {
        return stockItem;
    }

    public void setStockItem(StockItem stockItem) {
        this.stockItem = stockItem;
    }

    @Override
    public String toString() {
        return String.format("SoldItem{id=%d, name='%s'}", id, name);
    }

    public SoldItem copy() {
        SoldItem copyItem = new SoldItem();
        copyItem.setId(this.id); // Copie l'identifiant
        copyItem.setStockItem(this.stockItem.copy()); // Copie l'objet StockItem en profondeur
        copyItem.setName(this.name);
        copyItem.setQuantity(this.quantity);
        copyItem.setPrice(this.price);
        copyItem.setSum(this.sum);
        return copyItem;
    }

}
