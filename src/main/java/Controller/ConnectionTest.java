package Controller;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTest {

    public static void main(String[] args) {
        DataBaseConnection db=new DataBaseConnection();
        try (Connection connection =
                     db.getConnection()) {

            System.out.println("Database connected successfully!");

            System.out.println(
                    "Connected to: " +
                            connection.getCatalog()
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
