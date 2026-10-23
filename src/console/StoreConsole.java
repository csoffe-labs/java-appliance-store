package console;

import dao.HomeApplianceDAO;
import dao.CustomerDAO;
import model.HomeAppliance;
import model.Customer;
import model.Address;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console menu - CRUD for Customer and HomeAppliance.
 * 
 * @author Chloe Soffe 
 */
public class StoreConsole {
    private HomeApplianceDAO applianceDAO;
    private CustomerDAO customerDAO;
    private Scanner scanner;
    
   
    public StoreConsole(String dbUrl) {
        this.applianceDAO = new HomeApplianceDAO(dbUrl);
        this.customerDAO = new CustomerDAO(dbUrl);
        this.scanner = new Scanner(System.in);
    }
    
    public void run() {
        mainMenu();
    }
    
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:HomeStore_V2.db";
        StoreConsole app = new StoreConsole(dbUrl);
        app.run();
    }
    

    
    private void mainMenu() {
        String choice;
        
        do {
            System.out.println("\n====================");
            System.out.println("The Home Appliance Store");
            System.out.println("Choose from these options");
            System.out.println("====================");
            System.out.println("[1] Product Management");
            System.out.println("[2] Customer Management");
            System.out.println("[3] Exit");
            System.out.print("Enter choice: ");
            
            choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    productMenu();
                    break;
                case "2":
                    customerMenu();
                    break;
                case "3":
                    System.out.println("\nGoodbye!");
                    break;
                default:
                    System.out.println("Invalid choice - please enter 1, 2, or 3");
            }
            
        } while (!choice.equals("3"));
    }
    /**
     * Displays the product management menu and handles
     * listing, searching, adding, updating and deleting products.
     */
    private void productMenu() {
        String choice;

        do {
            System.out.println("\n--- PRODUCT MANAGEMENT ---");
            System.out.println("[1] List all products");
            System.out.println("[2] Search by ID");
            System.out.println("[3] Add product");
            System.out.println("[4] Update product");
            System.out.println("[5] Delete product");
            System.out.println("[0] Back to main menu");
            System.out.print("\nChoice: ");

            choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        System.out.println("\nRetrieving all products from database...");
                        List<HomeAppliance> products = applianceDAO.findAll();
                        
                        if (!products.isEmpty()) {
                            System.out.println("Found " + products.size() + " product(s):\n");

                            System.out.println("| ID  | SKU        | Brand       | Model          | Category       | Price     | Qty |");
                            System.out.println("-------------------------------------------------------------------------------------");

                            for (HomeAppliance p : products) {
                                System.out.println(p);
                            }
                        
                        }
                        break;

                    case "2":
                        System.out.print("\nEnter product ID: ");
                        int searchId = Integer.parseInt(scanner.nextLine());
                        
                        Optional<HomeAppliance> found = applianceDAO.findById(searchId);

                        if (found.isPresent()) {
                            System.out.println("\nProduct found:");
                            System.out.println(found.get());
                        } else {
                            System.out.println("No product found with ID: " + searchId);
                        }
                        break;

                    case "3":
                        System.out.println("\n=== Add New Product ===");
                        System.out.println("All fields are required\n");

                        System.out.print("SKU: ");
                        String sku = scanner.nextLine();
                        if (sku.trim().isEmpty()) {
                            System.out.println("Error: SKU cannot be empty!");
                            break;
                        }

                        System.out.print("Brand: ");
                        String brand = scanner.nextLine();
                        if (brand.trim().isEmpty()) {
                            System.out.println("Error: Brand cannot be empty!");
                            break;
                        }

                        System.out.print("Model: ");
                        String model = scanner.nextLine();
                        if (model.trim().isEmpty()) {
                            System.out.println("Error: Model cannot be empty!");
                            break;
                        }

                        System.out.print("Description: ");
                        String desc = scanner.nextLine();
                        if (desc.trim().isEmpty()) {
                            System.out.println("Error: Description cannot be empty!");
                            break;
                        }

                        System.out.print("Category: ");
                        String cat = scanner.nextLine();
                        if (cat.trim().isEmpty()) {
                            System.out.println("Error: Category cannot be empty!");
                            break;
                        }

                        System.out.print("Price (in pence): ");
                        int price = Integer.parseInt(scanner.nextLine());

                        System.out.print("Stock level: ");
                        int stock = Integer.parseInt(scanner.nextLine());

                        HomeAppliance newP = new HomeAppliance(0, sku, brand, model, desc, cat, price, stock);
                        HomeAppliance saved = applianceDAO.insert(newP);
                        System.out.println("\nSuccess! Product added with ID: " + saved.getId());
                        break;
                    
                    case "4":
                        System.out.print("\nEnter product ID to update: ");
                        int updateId = Integer.parseInt(scanner.nextLine());
                        
                        Optional<HomeAppliance> maybeProduct = applianceDAO.findById(updateId);

                        if (!maybeProduct.isPresent()) {
                            System.out.println("Product not found with ID: " + updateId);
                            break;
                        }

                        HomeAppliance existing = maybeProduct.get();
                        System.out.println("\nCurrent product details:");
                        System.out.println(existing);
                        System.out.println("\nPress ENTER to keep current value, or type new value:\n");
                        
                        System.out.print("SKU [" + existing.getSku() + "]: ");
                        String newSku = scanner.nextLine();
                        if (newSku.isEmpty()) newSku = existing.getSku();

                        System.out.print("Brand [" + existing.getBrand() + "]: ");
                        String newBrand = scanner.nextLine(); 
                        if (newBrand.isEmpty()) newBrand = existing.getBrand();

                        System.out.print("Model [" + existing.getModel() + "]: ");
                        String modelInput = scanner.nextLine();
                        if (modelInput.isEmpty()) modelInput = existing.getModel();

                        System.out.print("Description [" + existing.getDescription() + "]: ");
                        String descInput = scanner.nextLine();
                        if (descInput.isEmpty()) descInput = existing.getDescription();

                        System.out.print("Category [" + existing.getCategory() + "]: ");
                        String catInput = scanner.nextLine(); 
                        if (catInput.isEmpty()) catInput = existing.getCategory();

                        System.out.print("Price [" + existing.getPrice() + "]: ");
                        String priceStr = scanner.nextLine();
                        int newPrice = priceStr.isEmpty() ? existing.getPrice() : Integer.parseInt(priceStr);

                        System.out.print("Stock [" + existing.getStockLevel() + "]: ");
                        String stockStr = scanner.nextLine();
                        int newStock = stockStr.isEmpty() ? existing.getStockLevel() : Integer.parseInt(stockStr);

                        HomeAppliance updated = new HomeAppliance(updateId, newSku, newBrand, modelInput, descInput, catInput, newPrice, newStock);
                        boolean ok = applianceDAO.update(updated);
                        System.out.println(ok ? "\nProduct updated successfully!" : "\nUpdate failed.");
                        break;

                    case "5":
                        System.out.print("\nEnter product ID to delete: ");
                        int deleteId = Integer.parseInt(scanner.nextLine());
                        
                        System.out.print("Are you sure you want to delete this product? (yes/no): ");
                        String confirm = scanner.nextLine().trim().toLowerCase();
                        
                        if (!confirm.equals("yes")) {
                            System.out.println("Delete cancelled.");
                            break;
                        }

                        boolean deleted = applianceDAO.delete(deleteId);
                        System.out.println(deleted ? "Product deleted successfully" : "Product not found");
                        break;
                        
                    case "0":
                        break;
                        
                    default:
                        System.out.println("Invalid option - please choose 0-5");
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }

        } while (!choice.equals("0"));
    }
    /**
     * Displays the customer management menu and performs
     * the CRUD operations supported by CustomerDAO.
     */
    private void customerMenu() {
        String choice;
        
        do {
            System.out.println("\n--- CUSTOMER MANAGEMENT ---");
            System.out.println("[1] List all customers");
            System.out.println("[2] Search by ID");
            System.out.println("[3] Add customer");
            System.out.println("[4] Update customer");
            System.out.println("[5] Delete customer");
            System.out.println("[0] Back to main menu");
            System.out.print("\nChoice: ");
            
            choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        System.out.println("\nRetrieving all customers from database...");
                        List<Customer> customers = customerDAO.findAll();
                        
                        if (customers.isEmpty()) {
                            System.out.println("No customers found in database.");
                        } else {
                            System.out.println("Found " + customers.size() + " customer(s):\n");
                            for (int i = 0; i < customers.size(); i++) {
                                System.out.println(customers.get(i));
                                System.out.println();
                            }
                        }
                        break;
                        
                    case "2":
                        System.out.print("\nEnter customer ID: ");
                        int searchId = Integer.parseInt(scanner.nextLine());
                        
                        Optional<Customer> found = customerDAO.findById(searchId);
                        if (found.isPresent()) {
                            System.out.println("\nCustomer found:");
                            System.out.println(found.get());
                        } else {
                            System.out.println("No customer found with ID: " + searchId);
                        }
                        break;
                        
                    case "3":
                        System.out.println("\n=== Add New Customer ===\n");
                        
                        System.out.print("Business name: ");
                        String name = scanner.nextLine();
                        
                        System.out.print("Telephone: ");
                        String tel = scanner.nextLine();
                        
                        System.out.println("\nAddress details:");
                        System.out.print("  Line 1: ");
                        String line1 = scanner.nextLine();
                        
                        System.out.print("  Line 2: ");
                        String line2 = scanner.nextLine();
                        
                        System.out.print("  Town: ");
                        String town = scanner.nextLine();
                        
                        System.out.print("  Country: ");
                        String country = scanner.nextLine();
                        
                        System.out.print("  Postcode: ");
                        String postcode = scanner.nextLine();
                        
                        Address address = new Address(line1, line2, town, country, postcode);
                        Customer newCustomer = new Customer(0, name, tel, address);
                        
                        Customer saved = customerDAO.insert(newCustomer);
                        System.out.println("Success! Customer added with ID: " + saved.getId());
                        break;
                        
                    case "4":
                        System.out.print("\nEnter customer ID to update: ");
                        int updateId = Integer.parseInt(scanner.nextLine());
                        
                        Optional<Customer> maybeCustomer = customerDAO.findById(updateId);
                        
                        if (!maybeCustomer.isPresent()) {
                            System.out.println("Customer not found with ID: " + updateId);
                            break;
                        }
                        
                        Customer existing = maybeCustomer.get();
                        System.out.println("\nCurrent customer details:");
                        System.out.println(existing);
                        System.out.println("\nPress ENTER to keep current value, or type new value:\n");
                        
                        System.out.print("Business name [" + existing.getBusinessName() + "]: ");
                        String newName = scanner.nextLine();
                        newName = newName.isEmpty() ? existing.getBusinessName() : newName;
                        
                        System.out.print("Telephone [" + existing.getTelephone() + "]: ");
                        String newTel = scanner.nextLine();
                        newTel = newTel.isEmpty() ? existing.getTelephone() : newTel;
                        
                        System.out.println("\nAddress:");
                        System.out.print("  Line 1 [" + existing.getAddress().getLine1() + "]: ");
                        String addr1 = scanner.nextLine();
                        addr1 = addr1.isEmpty() ? existing.getAddress().getLine1() : addr1;
                        
                        System.out.print("  Line 2 [" + existing.getAddress().getLine2() + "]: ");
                        String addr2 = scanner.nextLine();
                        addr2 = addr2.isEmpty() ? existing.getAddress().getLine2() : addr2;
                        
                        System.out.print("  Town [" + existing.getAddress().getTown() + "]: ");
                        String townInput = scanner.nextLine();
                        townInput = townInput.isEmpty() ? existing.getAddress().getTown() : townInput;
                        
                        System.out.print("  Country [" + existing.getAddress().getCountry() + "]: ");
                        String countryInput = scanner.nextLine();
                        countryInput = countryInput.isEmpty() ? existing.getAddress().getCountry() : countryInput;
                        
                        System.out.print("  Postcode [" + existing.getAddress().getPostCode() + "]: ");
                        String pc = scanner.nextLine();
                        pc = pc.isEmpty() ? existing.getAddress().getPostCode() : pc;
                        
                        Address updatedAddr = new Address(addr1, addr2, townInput, countryInput, pc);
                        Customer updated = new Customer(updateId, newName, newTel, updatedAddr);
                        
                        boolean success = customerDAO.update(updated);
                        System.out.println(success ? "\nCustomer updated successfully!" : "\nUpdate failed.");
                        break;
                        
                    case "5":
                        System.out.print("\nEnter customer ID to delete: ");
                        int deleteId = Integer.parseInt(scanner.nextLine());
                        
                        System.out.print("Are you sure you want to delete this customer? (yes/no): ");
                        String confirm = scanner.nextLine().trim().toLowerCase();
                        
                        if (!confirm.equals("yes")) {
                            System.out.println("Delete cancelled.");
                            break;
                        }
                        
                        boolean deleted = customerDAO.delete(deleteId);
                        System.out.println(deleted ? "Customer deleted successfully" : "Customer not found");
                        break;
                        
                    case "0":
                        break;
                        
                    default:
                        System.out.println("Invalid option - please choose 0-5");
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
            
        } while (!choice.equals("0"));
    }
}