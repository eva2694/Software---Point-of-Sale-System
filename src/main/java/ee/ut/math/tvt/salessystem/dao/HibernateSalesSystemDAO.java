package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Sale;

import javax.persistence.*;
import java.util.List;

public class HibernateSalesSystemDAO implements SalesSystemDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;
    public HibernateSalesSystemDAO() {
        // if you get ConnectException / JDBCConnectionException then you
        // probably forgot to start the database before starting the application
        emf = Persistence.createEntityManagerFactory("pos");
        em = emf.createEntityManager();
    }
    // TODO implement missing methods
    public void close() {
        em.close();
        emf.close();
    }

    @Override
    public List<StockItem> findStockItems() {
        return null;
    }

    @Override
    public List<SoldItem> findSoldItems() {
        return null;
    }

    @Override
    public List<Sale> findSales() {
        return null;
    }

    @Override
    public StockItem findStockItem(long id) {
        return null;
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        try {
            beginTransaction();
            em.persist(stockItem);
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            e.printStackTrace();
        } finally {
            close();
        }
    }

    @Override
    public void saveSoldItem(SoldItem item) {
        try {
            beginTransaction();
            em.persist(item);
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            e.printStackTrace();
        } finally {
            close();
        }
    }

    @Override
    public void removeStockItem(StockItem stockItem) {

    }

    @Override
    public void beginTransaction() {
        em.getTransaction().begin();
    }
    @Override
    public void rollbackTransaction() {
        em.getTransaction().rollback();
    }
    @Override
    public void commitTransaction() {
        em.getTransaction().commit();
    }

    @Override
    public List<String> NameList() {
       // Query query = em.createQuery("SELECT name FROM stock_item");
        return null;//query.getResultList();
    }

    @Override
    public StockItem findStockItem_Name(String name) {
        return null;
    }

    @Override
    public boolean getTestBeginTransaction() {
        return false;
    }

    @Override
    public boolean getTestCommitTransaction() {
        return false;
    }
}
