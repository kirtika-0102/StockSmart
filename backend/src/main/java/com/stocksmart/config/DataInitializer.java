package com.stocksmart.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.stocksmart.entity.Alert;
import com.stocksmart.entity.AlertStatus;
import com.stocksmart.entity.AlertType;
import com.stocksmart.entity.Category;
import com.stocksmart.entity.Inventory;
import com.stocksmart.entity.InventoryTransaction;
import com.stocksmart.entity.Location;
import com.stocksmart.entity.LocationType;
import com.stocksmart.entity.Product;
import com.stocksmart.entity.Role;
import com.stocksmart.entity.RoleName;
import com.stocksmart.entity.Supplier;
import com.stocksmart.entity.SupplierProduct;
import com.stocksmart.entity.TransactionType;
import com.stocksmart.entity.User;
import com.stocksmart.repository.AlertRepository;
import com.stocksmart.repository.CategoryRepository;
import com.stocksmart.repository.InventoryRepository;
import com.stocksmart.repository.InventoryTransactionRepository;
import com.stocksmart.repository.LocationRepository;
import com.stocksmart.repository.ProductRepository;
import com.stocksmart.repository.RoleRepository;
import com.stocksmart.repository.SupplierProductRepository;
import com.stocksmart.repository.SupplierRepository;
import com.stocksmart.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final InventoryRepository inventoryRepository;
    private final AlertRepository alertRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            LocationRepository locationRepository,
            SupplierRepository supplierRepository,
            SupplierProductRepository supplierProductRepository,
            InventoryRepository inventoryRepository,
            AlertRepository alertRepository,
            InventoryTransactionRepository inventoryTransactionRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.inventoryRepository = inventoryRepository;
        this.alertRepository = alertRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    @Override
    public void run(String... args) {
        seedRolesAndUsers();
        seedDemoCatalog();
    }

    private void seedRolesAndUsers() {
        for (RoleName name : RoleName.values()) {
            roleRepository.findByName(name).orElseGet(() -> {
                Role role = new Role();
                role.setName(name);
                return roleRepository.save(role);
            });
        }

        seedUser("admin", "admin@stocksmart.local", "Admin@123", "System", "Admin", RoleName.ADMIN);
        seedUser("manager", "manager@stocksmart.local", "Manager@123", "Inventory", "Manager",
                RoleName.INVENTORY_MANAGER);
        seedUser("staff", "staff@stocksmart.local", "Staff@123", "Store", "Staff", RoleName.STAFF);
    }

    private void seedUser(String username, String email, String rawPassword, String firstName, String lastName,
            RoleName roleName) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    private void seedDemoCatalog() {
        // Only seed if catalog is empty
        if (productRepository.count() > 0) {
            return;
        }

        // 1. Categories
        Category electronics = createCategory("Electronics", null);
        Category groceries = createCategory("Groceries", null);
        Category stationery = createCategory("Stationery", null);
        Category hardware = createCategory("Hardware & Tools", null);

        // 2. Locations
        Location warehouse = createLocation("Central Warehouse", LocationType.WAREHOUSE, "Plot 42, Sector 18 Logistics Hub");
        Location retailStore = createLocation("Downtown Retail Store", LocationType.STORE, "Unit 104, City Center Mall");

        // 3. Suppliers
        Supplier apex = createSupplier("Apex Electronics Ltd", "Rajesh Sharma", "orders@apexelectronics.com", "+91-9811122334", "Okhla Phase 3, New Delhi");
        Supplier metro = createSupplier("Metro Wholesale Distribution", "Priya Patel", "sales@metrowholesale.in", "+91-9822233445", "APMC Market Yard, Navi Mumbai");

        // 4. Products
        Product mouse = createProduct("Wireless Optical Mouse", "ELEC-MOU-01", "8901234567890",
                "Ergonomic 2.4GHz wireless mouse with nano USB receiver", new BigDecimal("24.99"), electronics);
        Product keyboard = createProduct("Mechanical Gaming Keyboard", "ELEC-KEY-02", "8901234567891",
                "RGB backlit mechanical keyboard with blue switches", new BigDecimal("59.99"), electronics);
        Product usbCable = createProduct("USB-C Fast Charging Cable 2m", "ELEC-CBL-03", "8901234567894",
                "Braided durable 100W PD charging cable", new BigDecimal("12.99"), electronics);
        Product notebook = createProduct("A5 Ruled Hardcover Notebook", "STAT-NBK-01", "8901234567892",
                "192 pages 100gsm acid-free paper journal", new BigDecimal("6.50"), stationery);
        Product greenTea = createProduct("Organic Green Tea (50 Bags)", "GROC-TEA-01", "8901234567893",
                "Pure whole-leaf Himalayan green tea bags", new BigDecimal("14.00"), groceries);
        Product screwdriver = createProduct("Precision Screwdriver Set 32-in-1", "HARD-SCR-01", "8901234567895",
                "Magnetic screwdriver kit for electronics repair", new BigDecimal("19.50"), hardware);

        // 5. Link Suppliers to Products
        linkSupplierProduct(apex, mouse, new BigDecimal("12.50"));
        linkSupplierProduct(apex, keyboard, new BigDecimal("32.00"));
        linkSupplierProduct(apex, usbCable, new BigDecimal("4.50"));

        linkSupplierProduct(metro, notebook, new BigDecimal("2.20"));
        linkSupplierProduct(metro, greenTea, new BigDecimal("7.00"));
        linkSupplierProduct(metro, screwdriver, new BigDecimal("9.00"));

        User adminUser = userRepository.findByUsername("admin").orElse(null);

        // 6. Inventory balances & transactions
        // Warehouse (Well stocked)
        seedInventoryItem(mouse, warehouse, 150, 20, adminUser);
        seedInventoryItem(keyboard, warehouse, 80, 15, adminUser);
        seedInventoryItem(usbCable, warehouse, 200, 30, adminUser);
        seedInventoryItem(notebook, warehouse, 300, 50, adminUser);
        seedInventoryItem(greenTea, warehouse, 120, 25, adminUser);
        seedInventoryItem(screwdriver, warehouse, 60, 10, adminUser);

        // Retail Store (Deliberately includes Low Stock & Out of Stock to show Alerts & KPIs)
        seedInventoryItem(mouse, retailStore, 8, 15, adminUser);         // Low Stock
        seedInventoryItem(keyboard, retailStore, 0, 5, adminUser);         // Out of Stock
        seedInventoryItem(usbCable, retailStore, 25, 15, adminUser);       // Healthy
        seedInventoryItem(notebook, retailStore, 45, 15, adminUser);       // Healthy
        seedInventoryItem(greenTea, retailStore, 18, 10, adminUser);       // Healthy
        seedInventoryItem(screwdriver, retailStore, 5, 5, adminUser);       // At threshold

        // 7. Seed corresponding alerts for the demo
        createAlert(mouse, retailStore, AlertType.LOW_STOCK,
                "Stock level (8) is below reorder threshold (15) for Wireless Optical Mouse at Downtown Retail Store");
        createAlert(keyboard, retailStore, AlertType.OUT_OF_STOCK,
                "Stock is depleted (0) for Mechanical Gaming Keyboard at Downtown Retail Store");
    }

    private Category createCategory(String name, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setParent(parent);
        return categoryRepository.save(category);
    }

    private Location createLocation(String name, LocationType type, String address) {
        Location location = new Location();
        location.setName(name);
        location.setType(type);
        location.setAddress(address);
        return locationRepository.save(location);
    }

    private Supplier createSupplier(String name, String contactName, String email, String phone, String address) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContactName(contactName);
        supplier.setEmail(email);
        supplier.setPhone(phone);
        supplier.setAddress(address);
        return supplierRepository.save(supplier);
    }

    private Product createProduct(String name, String sku, String barcode, String description, BigDecimal unitPrice, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setBarcode(barcode);
        product.setDescription(description);
        product.setUnitPrice(unitPrice);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private void linkSupplierProduct(Supplier supplier, Product product, BigDecimal unitCost) {
        SupplierProduct sp = new SupplierProduct();
        sp.setSupplier(supplier);
        sp.setProduct(product);
        sp.setUnitCost(unitCost);
        supplierProductRepository.save(sp);
    }

    private void seedInventoryItem(Product product, Location location, int quantity, int reorderLevel, User user) {
        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setLocation(location);
        inv.setQuantity(quantity);
        inv.setReorderLevel(reorderLevel);
        inventoryRepository.save(inv);

        if (quantity > 0) {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setProduct(product);
            tx.setLocation(location);
            tx.setTransactionType(TransactionType.STOCK_IN);
            tx.setQuantityChange(quantity);
            tx.setReferenceType("INITIAL_SEED");
            tx.setReferenceId("INIT-" + location.getId() + "-" + product.getId());
            tx.setUser(user);
            inventoryTransactionRepository.save(tx);
        }
    }

    private void createAlert(Product product, Location location, AlertType type, String message) {
        Alert alert = new Alert();
        alert.setProduct(product);
        alert.setLocation(location);
        alert.setAlertType(type);
        alert.setMessage(message);
        alert.setStatus(AlertStatus.ACTIVE);
        alertRepository.save(alert);
    }
}
