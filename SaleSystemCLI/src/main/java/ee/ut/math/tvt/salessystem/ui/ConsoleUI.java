package ee.ut.math.tvt.salessystem.ui;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Sale;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A simple CLI (limited functionality).
 */
public class ConsoleUI {
    private static final Logger log = LogManager.getLogger(ConsoleUI.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart cart;

    public ConsoleUI() {
        this.dao = new InMemorySalesSystemDAO();
        //this.dao = new HibernateSalesSystemDAO();
        cart = new ShoppingCart(dao);
    }


    public static void main(String[] args) throws Exception {
        ConsoleUI console = new ConsoleUI();
        console.run();
    }

    /**
     * Run the sales system CLI.
     */
    public void run() throws IOException {
        System.out.println("===========================");
        System.out.println("=       Sales System      =");
        System.out.println("===========================");
        printUsage();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            processCommand(in.readLine().trim().toLowerCase());
            System.out.println("Done. ");
        }
    }

    private void showStock() {
        List<StockItem> stockItems = dao.findStockItems();
        log.info("Displaying stock items");
        System.out.println("-------------------------");
        for (StockItem si : stockItems) {
            System.out.println(si.getId() + " " + si.getName() + " " + si.getPrice() + " Euro (" + si.getQuantity() + " items)");
        }
        if (stockItems.size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showCart() {
        log.info("Displaying shopping cart");
        System.out.println("-------------------------");
        for (SoldItem si : cart.getAll()) {
            System.out.println(si.getName() + " " + si.getPrice() + " Euro (" + si.getQuantity() + " items)");
        }
        if (cart.getAll().size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showHistory() {
        List<Sale> salesList = dao.findSales();
        log.info("Displaying sales history");
        System.out.println("-------------------------");
        for (Sale sale : salesList) {
            System.out.println(sale.getId() + " " + sale.getSaleDate() + " " + sale.getSaleTime() + " Total:" + sale.getSaleTotal() + " Euro");
        }
        if (salesList.size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showTeam() {
        log.info("Displaying team information");
        System.out.println("-------------------------");
        System.out.println("Team name:     KAKTUS");
        System.out.println("Team contact:  kaktus@ut.ee");
        System.out.println("Team members:  Jonathan Degouve, Victor Chevreau, Eva Urankar");
        System.out.println("-------------------------");
    }

    private void addToCart(String idx, String nr) {
        log.info("Adding item to the cart");
        try {
            Long id = Long.parseLong(idx);
            int amount = Integer.parseInt(nr);
            StockItem item = dao.findStockItem(id);
            if (item != null) {
                cart.addItem(new SoldItem(item, Math.min(amount, item.getQuantity())));
            } else {
                log.error("no stock item with id " + id);
            }
        } catch (SalesSystemException | NoSuchElementException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void modifyWarehouse(String idx, String nr, String NAME, String PRICE) {
        log.info("Adding item to the warehouse");
        try {
            Long id = Long.parseLong(idx);
            int amount = Integer.parseInt(nr);
            String name = NAME;
            double price = Integer.parseInt(PRICE);

            if (id < 0 || amount < 0 || price < 0) {
                log.info("Displaying warning dialog for invalid input: negative values.");
                throw new IllegalArgumentException("Barcode, Quantity, and Price cannot be negative");
            }

            // CREATE NEW STOCKITEM
            StockItem newStockItem = new StockItem();

            if(dao.findStockItem(id) == null) {
                try {
                    // insert data into newStockItem
                    newStockItem.setId(id);
                    newStockItem.setQuantity(amount);
                    newStockItem.setName(name);
                    newStockItem.setPrice(price);

                    // save into sales system dao
                    log.debug("Adding new item to the warehouse: " + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    dao.saveStockItem(newStockItem);
                    log.info("New item added to the warehouse: " + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                }
                catch(Exception e) {
                    e.printStackTrace();
                    log.info("Display of informations failed");
                    log.debug("New item info => name:" + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    log.debug("List of items in warehouse:" + dao.findStockItems());
                }
            }
            else {
                try {
                    // update quantity of stockItem
                    dao.updateQuantity(id, dao.findStockItem(id).getQuantity() + amount);
                    log.info("Item quantity updated in the warehouse: " + name + " (ID: " + id + ") - New Quantity: " + dao.findStockItem(id).getQuantity());

                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("Display of informations failed");
                    log.error("Failed to update item quantity in the warehouse", e);
                    log.debug("New item info => name:" + newStockItem.getName() + " (ID: " + newStockItem.getId() + ") - Quantity: " + newStockItem.getQuantity());
                    log.debug("List of items in warehouse:" + dao.findStockItems());
                }
            }
        } catch (SalesSystemException | NoSuchElementException e) {
            log.error(e.getMessage(), e);
            log.info("Invalid Input, warning message shown: numeric values needed for Barcode, Quantity, and Price.");
        }

    }

    private void printUsage() {
        System.out.println("-------------------------");
        System.out.println("Usage:");
        System.out.println("h\t\tShow this help");
        System.out.println("w\t\tShow warehouse contents");
        System.out.println("m IDX NR NAME PRICE\t\tAdd NR of NAME with index IDX and PRICE to the warehouse");
        System.out.println("c\t\tShow cart contents");
        System.out.println("a IDX NR \tAdd NR of stock item with index IDX to the cart");
        System.out.println("p\t\tPurchase the shopping cart");
        System.out.println("r\t\tReset the shopping cart");
        System.out.println("s\t\tShow Sale History");
        System.out.println("t\t\tShow team information");
        System.out.println("q\t\tExit the application");
        System.out.println("-------------------------");
    }

    private void processCommand(String command) {
        String[] c = command.split(" ");
        if (c[0].equals("h")) {
            log.info("Displaying usage information");
            printUsage();
        }else if (c[0].equals("q")) {
            log.info("Exiting the application");
            System.exit(0);
        }else if (c[0].equals("t"))
            showTeam();
        else if (c[0].equals("w"))
            showStock();
        else if (c[0].equals("c"))
            showCart();
        else if (c[0].equals("s"))
            showHistory();
        else if (c[0].equals("m") && c.length == 5)
            modifyWarehouse(c[1], c[2], c[3], c[4]);
        else if (c[0].equals("a") && c.length == 3)
            addToCart(c[1], c[2]);
        else if (c[0].equals("p"))
            cart.submitCurrentPurchase();
        else if (c[0].equals("r"))
            cart.cancelCurrentPurchase();
        else if (c[0].equals("&"))
            smiley();
        else {
            log.info("unknown command");
        }
    }


    private void smiley() {System.out.println("(｡◕‿‿◕｡)");}
}
