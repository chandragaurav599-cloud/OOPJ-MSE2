import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class OrderManagementSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";[cite: 6]
    private static final String USER = "root";[cite: 6]
    private static final String PASS = "your_password";[cite: 6]

    private static final List<String> VALID_STATUS =
            Arrays.asList("Pending", "Shipped", "Delivered", "Cancelled");[cite: 6]
    private static final Scanner sc = new Scanner(System.in);[cite: 6]

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {[cite: 6]
            System.out.println("Connected to MySQL database: ecommerce_db");[cite: 6]
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt("Enter choice: ");[cite: 6]
                switch (choice) {
                    case 1 -> insertOrders(con);[cite: 6]
                    case 2 -> displayByStatus(con);[cite: 6]
                    case 3 -> increaseQuantity(con);[cite: 6, 10]
                    case 4 -> updateStatus(con);[cite: 6]
                    case 5 -> totalValueAndDelete(con);[cite: 6]
                    case 6 -> {
                        running = false;[cite: 6, 7]
                        System.out.println("Goodbye!");[cite: 7]
                    }
                    default -> System.out.println("Invalid choice. Try again.");[cite: 6]
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());[cite: 6]
        }
    }

    private static void printMenu() {
        System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");[cite: 7]
        System.out.println("1. Insert sample orders (3+ records)");[cite: 7]
        System.out.println("2. Show orders by status (quantity > 2, price DESC)");[cite: 7]
        System.out.println("3. Increase quantity of an order");[cite: 7]
        System.out.println("4. Update order status");[cite: 7]
        System.out.println("5. Total order value / Delete cancelled order");[cite: 7]
        System.out.println("6. Exit");[cite: 7]
    }

    private static void insertOrders(Connection con) {
        Object[][] data = {
                {101, "Aarav Sharma", "Laptop", 3, 55000.0, "Pending"},[cite: 8]
                {102, "Priya Singh", "Headphones", 5, 2500.0, "Shipped"},[cite: 8]
                {103, "Rohan Mehta", "Keyboard", 2, 1200.0, "Delivered"},[cite: 8]
                {104, "Neha Gupta", "Monitor", 4, 12000.0, "Cancelled"}[cite: 8]
        };
        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";[cite: 8]
        try (PreparedStatement ps = con.prepareStatement(sql)) {[cite: 8]
            for (Object[] row : data) {[cite: 8]
                try {
                    ps.setInt(1, (Integer) row[0]);[cite: 8]
                    ps.setString(2, (String) row[1]);[cite: 8]
                    ps.setString(3, (String) row[2]);[cite: 8]
                    ps.setInt(4, (Integer) row[3]);[cite: 8]
                    ps.setDouble(5, (Double) row[4]);[cite: 8]
                    ps.setString(6, (String) row[5]);[cite: 8]
                    ps.executeUpdate();[cite: 8]
                    System.out.println("Inserted order ID " + row[0]);[cite: 8]
                } catch (SQLIntegrityConstraintViolationException dup) {[cite: 8]
                    System.out.println("Order ID " + row[0] + " already exists. Skipped.");[cite: 8]
                }
            }
        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());[cite: 8]
        }
    }

    private static void displayByStatus(Connection con) {
        String status = readLine("Enter status: ");[cite: 9]
        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";[cite: 9]
        try (PreparedStatement ps = con.prepareStatement(sql)) {[cite: 9]
            ps.setString(1, status);[cite: 9]
            try (ResultSet rs = ps.executeQuery()) {[cite: 9]
                boolean found = false;[cite: 9]
                printHeader();[cite: 9]
                while (rs.next()) {[cite: 9]
                    found = true;[cite: 9]
                    printRow(rs);[cite: 9]
                }
                if (!found) {[cite: 9]
                    System.out.println("No orders found with status " + status + " and quantity > 2.");[cite: 9]
                }
            }
        } catch (SQLException e) {
            System.out.println("Query error: " + e.getMessage());[cite: 9]
        }
    }

    private static void increaseQuantity(Connection con) {
        int id = readInt("Enter order ID: ");[cite: 10]
        int extra = readInt("Enter additional quantity: ");[cite: 10]
        if (extra <= 0) {[cite: 10]
            System.out.println("Additional quantity must be positive.");[cite: 10]
            return;
        }
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";[cite: 10]
        try (PreparedStatement ps = con.prepareStatement(sql)) {[cite: 10]
            ps.setInt(1, extra);[cite: 10]
            ps.setInt(2, id);[cite: 10]
            int rows = ps.executeUpdate();[cite: 10]
            if (rows == 0) {[cite: 10]
                System.out.println("Order with ID " + id + " does not exist.");[cite: 10]
            } else {
                System.out.println("Quantity increased by " + extra + " for order " + id + ".");[cite: 10]
            }
        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());[cite: 10]
        }
    }

    private static void updateStatus(Connection con) {
        int id = readInt("Enter order ID: ");[cite: 11]
        String newStatus = readLine("Enter new status (Pending/Shipped/Delivered): ");[cite: 11]
        String matched = null;[cite: 11]
        for (String s : VALID_STATUS) {[cite: 11]
            if (s.equalsIgnoreCase(newStatus.trim())) {[cite: 11]
                matched = s;[cite: 11]
                break;
            }
        }
        if (matched == null) {[cite: 11]
            System.out.println("Invalid status. Allowed: " + VALID_STATUS);[cite: 11]
            return;
        }
        String select = "SELECT * FROM Orders WHERE order_id = ?";[cite: 11]
        String update = "UPDATE Orders SET status = ? WHERE order_id = ?";[cite: 11]
        try (PreparedStatement sel = con.prepareStatement(select)) {[cite: 11]
            sel.setInt(1, id);[cite: 12]
            try (ResultSet rs = sel.executeQuery()) {[cite: 12]
                if (!rs.next()) {[cite: 12]
                    System.out.println("Order with ID " + id + " does not exist.");[cite: 12]
                    return;
                }
                System.out.println("Current details:");[cite: 12]
                printHeader();[cite: 12]
                printRow(rs);[cite: 12]
            }
            try (PreparedStatement upd = con.prepareStatement(update)) {[cite: 12]
                upd.setString(1, matched);[cite: 12]
                upd.setInt(2, id);[cite: 12]
                upd.executeUpdate();[cite: 12]
            }
            try (PreparedStatement sel2 = con.prepareStatement(select)) {[cite: 12]
                sel2.setInt(1, id);[cite: 12]
                try (ResultSet rs2 = sel2.executeQuery()) {[cite: 12]
                    System.out.println("Updated details:");[cite: 12]
                    printHeader();[cite: 12]
                    if (rs2.next()) {[cite: 12]
                        printRow(rs2);[cite: 12]
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());[cite: 12]
        }
    }

    private static void totalValueAndDelete(Connection con) {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(quantity * unit_price) AS total FROM Orders")) {[cite: 13]
            if (rs.next()) {[cite: 13]
                System.out.printf("Total order value: %.2f%n", rs.getDouble("total"));[cite: 13]
            }
        } catch (SQLException e) {
            System.out.println("Error computing total: " + e.getMessage());[cite: 13]
            return;
        }

        int id = readInt("Enter order ID to delete: ");[cite: 13]
        String select = "SELECT status FROM Orders WHERE order_id = ?";[cite: 13]
        String delete = "DELETE FROM Orders WHERE order_id = ?";[cite: 13]
        try (PreparedStatement sel = con.prepareStatement(select)) {[cite: 13]
            sel.setInt(1, id);[cite: 13]
            try (ResultSet rs = sel.executeQuery()) {[cite: 13]
                if (!rs.next()) {[cite: 13]
                    System.out.println("Order with ID " + id + " does not exist.");[cite: 13]
                    return;
                }
                String status = rs.getString("status");[cite: 13]
                if (!"Cancelled".equalsIgnoreCase(status)) {[cite: 13]
                    System.out.println("Cannot delete: order status is " + status + " (only Cancelled orders can be deleted).");[cite: 13]
                    return;
                }
            }
            try (PreparedStatement del = con.prepareStatement(delete)) {[cite: 14]
                del.setInt(1, id);[cite: 14]
                del.executeUpdate();[cite: 14]
                System.out.println("Cancelled order " + id + " deleted successfully.");[cite: 14]
            }
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());[cite: 14]
        }
    }

    private static void printHeader() {
        System.out.printf("%-8s %-18s %-16s %-5s %-12s %-10s%n",
                "ID", "Customer", "Product", "Qty", "Unit Price", "Status");[cite: 14]
        System.out.println("-".repeat(75));[cite: 14]
    }

    private static void printRow(ResultSet rs) throws SQLException {
        System.out.printf("%-8d %-18s %-16s %-5d %-12.2f %-10s%n",
                rs.getInt("order_id"),[cite: 14]
                rs.getString("customer_name"),[cite: 14]
                rs.getString("product_name"),[cite: 14]
                rs.getInt("quantity"),[cite: 14]
                rs.getDouble("unit_price"),[cite: 14]
                rs.getString("status"));[cite: 14]
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);[cite: 14]
            try {
                return Integer.parseInt(sc.nextLine().trim());[cite: 14]
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");[cite: 14]
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);[cite: 14]
        return sc.nextLine().trim();[cite: 14]
    }
}
