package emg.keycloak.demo.db;

import org.keycloak.component.ComponentModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static emg.keycloak.demo.constants.Constants.*;

public class DBUtil {
    public static Connection getConnection(ComponentModel config) throws SQLException {
        String driverClass = config.get(CONFIG_KEY_JDBC_DRIVER);
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException nfe) {
            throw new RuntimeException("Invalid JDBC driver: " + driverClass + ". Please check if your driver if properly installed");
        }

        return DriverManager.getConnection(config.get(CONFIG_KEY_JDBC_URL),
            config.get(CONFIG_KEY_DB_USERNAME),
            config.get(CONFIG_KEY_DB_PASSWORD));
    }
}
