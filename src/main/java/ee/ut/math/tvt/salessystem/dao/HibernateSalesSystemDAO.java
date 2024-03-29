package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Sale;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HibernateSalesSystemDAO implements SalesSystemDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;
    private EntityTransaction currentTransaction;
    private boolean testBeginTransaction = false;
    private boolean testCommitTransaction = false;
    public HibernateSalesSystemDAO() {
        // if you get ConnectException / JDBCConnectionException then you
        // probably forgot to start the database before starting the application
        this.emf = Persistence.createEntityManagerFactory("pos");
        this.em = emf.createEntityManager();
    }
    // TODO implement missing methods
    public void close() {
        if (this.em != null && this.em.isOpen()) {
            this.em.close();
        }
        if (this.emf != null && this.emf.isOpen()) {
            this.emf.close();
        }
    }

    @Override
    public List<StockItem> findStockItems() {
        try {
            TypedQuery<StockItem> query = this.em.createQuery("SELECT s FROM StockItem s", StockItem.class);
            return query.getResultList();
        } catch (Exception e) {
            handleException(e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<SoldItem> findSoldItems() {
        try {
            TypedQuery<SoldItem> query = this.em.createQuery("SELECT s FROM SoldItem s", SoldItem.class);
            return query.getResultList();
        } catch (Exception e) {
            handleException(e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Sale> findSales() {
        try {
            TypedQuery<Sale> query = this.em.createQuery("SELECT s FROM Sale s", Sale.class);
            return query.getResultList();
        } catch (Exception e) {
            handleException(e);
            return Collections.emptyList();
        }
    }

    @Override
    public StockItem findStockItem(long id) {
        try {
            return this.em.find(StockItem.class, id);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        try {
            if (stockItem.getId() == null) {
                this.em.persist(stockItem);
            } else {
                this.em.merge(stockItem);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void removeStockItem(StockItem stockItem) {
        try {
            StockItem toBeRemoved = this.em.find(StockItem.class, stockItem.getId());
            this.em.remove(toBeRemoved);
        } catch (Exception e) {
            handleException(e);
        }
    }

  @Override
  public void saveSoldItemsAndCreateSale(List<SoldItem> soldItems) {
      try {
          // Merge each SoldItem
          List<SoldItem> mergedSoldItems = new ArrayList<>();
          for (SoldItem soldItem : soldItems) {
              mergedSoldItems.add(this.em.merge(soldItem));
          }

          // Create a new Sale based on the merged sold items
          Sale sale = new Sale(mergedSoldItems);
          this.em.persist(sale);
      } catch (Exception e) {
          handleException(e);
      }
  }


    @Override
    public Sale findSale(long id) {
        try {
            return this.em.find(Sale.class, id);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    private void handleException(Exception e) {
        e.printStackTrace();
    }


    @Override
    public void beginTransaction() {
        try {
            this.currentTransaction = this.em.getTransaction();
            if (!this.currentTransaction.isActive()) {
                this.currentTransaction.begin();
                this.testBeginTransaction = true;
            }
        } catch (Exception e) {
            if (this.currentTransaction != null && this.currentTransaction.isActive()) {
                this.currentTransaction.rollback();
            }
            handleException(e);
        }
    }


    @Override
    public void rollbackTransaction() {
        try {
            if (this.currentTransaction != null) {
                if (this.currentTransaction.isActive()) {
                    this.currentTransaction.rollback();
                    this.testCommitTransaction = false;
                } else {
                    throw new IllegalStateException("Transaction not active during rollback.");
                }
            } else {
                throw new IllegalStateException("Transaction not successfully started before rollback.");
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            this.currentTransaction = null;
        }
    }

    @Override
    public void commitTransaction() {
        try {
            if (this.currentTransaction != null) {
                if (this.currentTransaction.isActive()) {
                    this.currentTransaction.commit();
                    this.testCommitTransaction = true;
                } else {
                    throw new IllegalStateException("Transaction not active during commit.");
                }
            } else {
                throw new IllegalStateException("Transaction not successfully started before commit.");
            }
        } catch (Exception e) {
            if (this.currentTransaction != null && this.currentTransaction.isActive()) {
                this.currentTransaction.rollback();
            }
            handleException(e);
        } finally {
            this.currentTransaction = null;
        }
    }

    @Override
    public List<String> NameList() {
        TypedQuery<String> query = this.em.createQuery("SELECT s.name FROM StockItem s", String.class);
        return query.getResultList();
    }

    @Override
    public StockItem findStockItem_Name(String name) {
        TypedQuery<StockItem> query = this.em.createQuery("SELECT s FROM StockItem s WHERE s.name = :name", StockItem.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }
    @Override
    public void updateQuantity(Long itemId, int itemQuantity) {
        beginTransaction();
        Query query = this.em.createQuery("UPDATE StockItem SET quantity = :itemQuantity WHERE id = :itemId");
        query.setParameter("itemQuantity", itemQuantity);
        query.setParameter("itemId", itemId);
        int updatedRows = query.executeUpdate();
        commitTransaction();
        this.em.clear();
    }

    @Override
    public boolean getTestBeginTransaction() {
        return testBeginTransaction;
    }

    @Override
    public boolean getTestCommitTransaction() {
        return testCommitTransaction;
    }
}
