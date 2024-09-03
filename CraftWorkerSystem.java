import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class CraftWorkerSystem {

    // Database connection URL, username, and password
    private static final String URL = "jdbc:mysql://localhost:3306/craftworkers";
    private static final String USER = "root";
    private static final String PASSWORD = "pass123";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Scanner scanner = new Scanner(System.in)) {

            System.out.println("1. Sign Up");
            System.out.println("2. Log In");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    signUp(conn, scanner);
                    break;
                case 2:
                    logIn(conn, scanner);
                    break;
                default:
                    System.out.println("Invalid option.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Method for user sign-up
    private static void signUp(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String hashedPassword = hashPassword(password);

        String insertSQL = "INSERT INTO Users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Sign-up successful. Welcome!");
                trackSignUpMetrics(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error during sign-up: " + e.getMessage());
        }
    }

    // Method for user log-in
    private static void logIn(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String hashedPassword = hashPassword(password);

        String querySQL = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login successful. Welcome back!");
                    userOptions(conn, scanner);
                } else {
                    System.out.println("Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during log-in: " + e.getMessage());
        }
    }

    // Method to handle user options after login
    private static void userOptions(Connection conn, Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("1. Add Craft Listing");
            System.out.println("2. Update Craft Listing");
            System.out.println("3. View Craft Listings");
            System.out.println("4. Manage Shopping Cart");
            System.out.println("5. Log Out");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    addCraftListing(conn, scanner);
                    break;
                case 2:
                    updateCraftListing(conn, scanner);
                    break;
                case 3:
                    viewCraftListings(conn);
                    break;
                case 4:
                    manageShoppingCart(conn, scanner);
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // Method to hash passwords
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Method to add a craft listing
    private static void addCraftListing(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Craft Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Craft Description: ");
        String description = scanner.nextLine();
        System.out.print("Enter Price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        String insertSQL = "INSERT INTO Crafts (name, description, price) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Craft listing added successfully.");
                trackListingManagementMetrics(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error adding craft listing: " + e.getMessage());
        }
    }

    // Method to update a craft listing
    private static void updateCraftListing(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Craft ID to Update: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter New Craft Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter New Craft Description: ");
        String description = scanner.nextLine();
        System.out.print("Enter New Price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        String updateSQL = "UPDATE Crafts SET name = ?, description = ?, price = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, id);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Craft listing updated successfully.");
                trackListingManagementMetrics(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error updating craft listing: " + e.getMessage());
        }
    }

    // Method to view craft listings
    private static void viewCraftListings(Connection conn) throws SQLException {
        String querySQL = "SELECT * FROM Crafts";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(querySQL)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                System.out.printf("ID: %d, Name: %s, Description: %s, Price: %.2f%n", id, name, description, price);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing craft listings: " + e.getMessage());
        }
    }

    // Method to manage the shopping cart
    private static void manageShoppingCart(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Shopping Cart Management");

        System.out.println("1. Add Item to Cart");
        System.out.println("2. Remove Item from Cart");
        System.out.println("3. View Cart");
        System.out.println("4. Checkout");
        System.out.print("Choose an option: ");
        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option) {
            case 1:
                addItemToCart(conn, scanner);
                break;
            case 2:
                removeItemFromCart(conn, scanner);
                break;
            case 3:
                viewCart(conn);
                break;
            case 4:
                checkout(conn);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Method to add an item to the shopping cart
    private static void addItemToCart(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Craft ID to Add to Cart: ");
        int craftId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        String insertSQL = "INSERT INTO Cart (craft_id, quantity) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, craftId);
            pstmt.setInt(2, quantity);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Item added to cart.");
                trackCartMetrics(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error adding item to cart: " + e.getMessage());
        }
    }

    // Method to remove an item from the shopping cart
    private static void removeItemFromCart(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Item ID to Remove from Cart: ");
        int itemId = scanner.nextInt();
        scanner.nextLine();

        String deleteSQL = "DELETE FROM Cart WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, itemId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Item removed from cart.");
                trackCartMetrics(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error removing item from cart: " + e.getMessage());
        }
    }

    // Method to view items in the shopping cart
    private static void viewCart(Connection conn) throws SQLException {
        String querySQL = "SELECT Cart.id, Crafts.name, Cart.quantity, Crafts.price " +
                "FROM Cart INNER JOIN Crafts ON Cart.craft_id = Crafts.id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(querySQL)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                System.out.printf("ID: %d, Name: %s, Quantity: %d, Price: %.2f%n", id, name, quantity, price);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing cart: " + e.getMessage());
        }
    }

    // Method to handle checkout process
    private static void checkout(Connection conn) throws SQLException {
        System.out.println("Checkout process...");
        String deleteSQL = "DELETE FROM Cart";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Checkout complete. Cart is now empty.");
                trackCartMetrics(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error during checkout: " + e.getMessage());
        }
    }

    // Method to track sign-up metrics
    private static void trackSignUpMetrics(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO Metrics (metric_type, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, "SignUp");
            pstmt.setInt(2, 1);
            pstmt.executeUpdate();
            System.out.println("Sign-up metrics tracked.");
        } catch (SQLException e) {
            System.out.println("Error tracking sign-up metrics: " + e.getMessage());
        }
    }

    // Method to track listing management metrics
    private static void trackListingManagementMetrics(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO Metrics (metric_type, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, "ListingManagement");
            pstmt.setInt(2, 1);
            pstmt.executeUpdate();
            System.out.println("Listing management metrics tracked.");
        } catch (SQLException e) {
            System.out.println("Error tracking listing management metrics: " + e.getMessage());
        }
    }

    // Method to track shopping cart metrics
    private static void trackCartMetrics(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO Metrics (metric_type, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, "CartOperation");
            pstmt.setInt(2, 1);
            pstmt.executeUpdate();
            System.out.println("Shopping cart metrics tracked.");
        } catch (SQLException e) {
            System.out.println("Error tracking shopping cart metrics: " + e.getMessage());
        }
    }
}
