import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    static Connection connection = null;
    static Statement statement = null;
    static ResultSet resultSet = null;
    static CellStyle cellStyle = new CellStyle(CellStyle.HorizontalAlign.center);
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        LoadingProperties.loadProperties("./config.properties");

        String dbUrl = LoadingProperties.getProperty("db.url");
        String username = LoadingProperties.getProperty("db.username");
        String password = LoadingProperties.getProperty("db.password");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            createTableIfNotExists();

            while (true) {
                displayMenu();
                int option = Integer.parseInt(validateInput("Option: ", "[1-5]", "Invalid option. Please try again."));
                switch (option) {
                    case 1:
                        insertUser();
                        break;
                    case 2:
                        readUser();
                        break;
                    case 3:
                        updateUser();
                        break;
                    case 4:
                        deleteUser();
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void createTableIfNotExists() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users_tb (" +
                    "user_id SERIAL PRIMARY KEY, " +
                    "user_uuid VARCHAR(200), " +
                    "user_name VARCHAR(200), " +
                    "user_email VARCHAR(300), " +
                    "user_password VARCHAR(300), " +
                    "is_delete BOOLEAN DEFAULT false, " +
                    "is_verified BOOLEAN DEFAULT false)"
            );
        }
    }

    private static void displayMenu() throws SQLException {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM users_tb");
        Table table = new Table(7, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
        table.addCell("USER", cellStyle, 7);
        table.addCell("user_id", cellStyle);
        table.addCell("user_uuid", cellStyle);
        table.addCell("user_name", cellStyle);
        table.addCell("user_email", cellStyle);
        table.addCell("user_password", cellStyle);
        table.addCell("is_deleted", cellStyle);
        table.addCell("is_verified", cellStyle);

        while (resultSet.next()) {
            table.addCell(resultSet.getString("user_id"), cellStyle);
            table.addCell(resultSet.getString("user_uuid"), cellStyle);
            table.addCell(resultSet.getString("user_name"), cellStyle);
            table.addCell(resultSet.getString("user_email"), cellStyle);
            table.addCell(resultSet.getString("user_password"), cellStyle);
            table.addCell(String.valueOf(resultSet.getBoolean("is_delete")), cellStyle);
            table.addCell(String.valueOf(resultSet.getBoolean("is_verified")), cellStyle);
        }
        System.out.println(table.render());
        System.out.println("---------------------------------------");
        System.out.println("Choose operation:");
        System.out.println("1. Create user");
        System.out.println("2. Read user");
        System.out.println("3. Update user");
        System.out.println("4. Delete user");
        System.out.println("5. Exit");
        System.out.println("---------------------------------------");
    }

    private static void insertUser() throws SQLException {
        scanner.nextLine(); // Consume newline
        String userUUID = validateInput("Enter user UUID:", ".+", "User UUID cannot be empty.");
        String userName = validateInput("Enter user name:", ".+", "User name cannot be empty.");
        String userEmail = validateInput("Enter user email:", ".+@.+\\..+", "Invalid email format. Example : Username@gmail.com");
        String userPassword = validateInput("Enter user password:", ".+", "User password cannot be empty.");

        String insertQuery = "INSERT INTO users_tb (user_uuid, user_name, user_email, user_password) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, userUUID);
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, userEmail);
            preparedStatement.setString(4, userPassword);
            preparedStatement.executeUpdate();
            System.out.println("User created successfully.");
        }
    }

    private static void readUser() throws SQLException {
        int userId = Integer.parseInt(validateInput("Enter user ID:", "\\d+", "Invalid user ID."));
        String selectQuery = "SELECT * FROM users_tb WHERE user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    displayUser(resultSet);
                } else {
                    System.out.println("User not found.");
                }
            }
        }
    }

    private static void updateUser() throws SQLException {
        int userId = Integer.parseInt(validateInput("Enter user ID:", "\\d+", "Invalid user ID."));
        String userUUID = validateInput("Enter updated user UUID:", ".+", "User UUID cannot be empty.");
        String userName = validateInput("Enter updated user name:", ".+", "User name cannot be empty.");
        String userEmail = validateInput("Enter updated user email:", ".+@.+\\..+", "Invalid email format. Example : Username@gmail.com");
        String userPassword = validateInput("Enter updated user password:", ".+", "User password cannot be empty.");

        String updateQuery = "UPDATE users_tb SET user_uuid = ?, user_name = ?, user_email = ?, user_password = ? " +
                "WHERE user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, userUUID);
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, userEmail);
            preparedStatement.setString(4, userPassword);
            preparedStatement.setInt(5, userId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User updated successfully.");
            } else {
                System.out.println("User not found or no changes were made.");
            }
        }
    }

    private static void deleteUser() throws SQLException {
        int userId = Integer.parseInt(validateInput("Enter user ID:", "\\d+", "Invalid user ID."));
        String deleteQuery = "DELETE FROM users_tb WHERE user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setInt(1, userId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("User not found.");
            }
        }
    }

    private static void displayUser(ResultSet resultSet) throws SQLException {
        Table table = new Table(7, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
        table.addCell("USER", cellStyle, 7);
        table.addCell("user_id", cellStyle);
        table.addCell("user_uuid", cellStyle);
        table.addCell("user_name", cellStyle);
        table.addCell("user_email", cellStyle);
        table.addCell("user_password", cellStyle);
        table.addCell("is_deleted", cellStyle);
        table.addCell("is_verified", cellStyle);

        table.addCell(resultSet.getString("user_id"), cellStyle);
        table.addCell(resultSet.getString("user_uuid"), cellStyle);
        table.addCell(resultSet.getString("user_name"), cellStyle);
        table.addCell(resultSet.getString("user_email"), cellStyle);
        table.addCell(resultSet.getString("user_password"), cellStyle);
        table.addCell(String.valueOf(resultSet.getBoolean("is_delete")), cellStyle);
        table.addCell(String.valueOf(resultSet.getBoolean("is_verified")), cellStyle);

        System.out.println(table.render());
    }

    public static String validateInput(String input, String pattern, String errorMessage) {
        boolean isValid;
        String userInput;
        do {
            System.out.print(input);
            userInput = scanner.nextLine();
            isValid = Pattern.matches(pattern, userInput);
            if (!isValid) {
                System.out.println(errorMessage);
            }
        } while (!isValid);
        return userInput;
    }
}
