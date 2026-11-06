package com.sadamart.ui;

import com.sadamart.application.Launcher;
import com.sadamart.infrastructure.OracleDatabaseProperties;
import com.sadamart.security.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Rebuilt Enterprise Sign-In Controller.
 * Handles sign-in and the transition into the store workspace.
 */
public final class LoginController {
    @FXML private VBox heroBox, cardBox;
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label status;
    @FXML private Button signInButton;

    @FXML
    private void initialize() {
        username.clear();
        password.clear();

        UiAnimations.playStaggeredCascade(List.of(heroBox, cardBox), 90);
        if (signInButton != null) {
            UiAnimations.attachSpringPhysics(signInButton);
        }
    }

    @FXML
    private void signIn() {
        String user = username.getText() == null ? "" : username.getText().trim();
        String pass = password.getText() == null ? "" : password.getText();
        if (user.isBlank() || pass.isBlank()) {
            status.setText("Enter your store username and password to continue.");
            status.getStyleClass().setAll("login-error");
            UiAnimations.playPulseHighlight(status);
            return;
        }
        try {
            UserDetails account = authenticateAgainstOracle(user, pass);
            applyRoleAndOpenWorkspace(account.username(), account.fullName(), account.role());
        } catch (Exception ex) {
            status.setText(ex.getMessage());
            status.getStyleClass().setAll("login-error");
            UiAnimations.playPulseHighlight(status);
        }
    }

    private UserDetails authenticateAgainstOracle(String username, String password) {
        if (!"true".equalsIgnoreCase(System.getenv("SADAMART_ORACLE_ENABLED"))) {
            throw new IllegalStateException("Store database is not connected. Start the project with run-connected.cmd.");
        }
        String dbPassword = System.getenv("SADAMART_ORACLE_PASSWORD");
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException("Store database password is not configured.");
        }
        OracleDatabaseProperties properties = new OracleDatabaseProperties(
                true,
                environment("SADAMART_ORACLE_HOST", "localhost"),
                Integer.parseInt(environment("SADAMART_ORACLE_PORT", "1521")),
                environment("SADAMART_ORACLE_SERVICE", "FREEPDB1"),
                environment("SADAMART_ORACLE_USERNAME", "sadamart_app"),
                dbPassword,
                Boolean.parseBoolean(environment("SADAMART_ORACLE_TCPS", "false"))
        );
        properties.validate();
        String query = """
                SELECT username, full_name, password_hash, role, active
                FROM users
                WHERE LOWER(username) = LOWER(?)
                """;
        try {
            initializeDatabase(properties);
        } catch (Exception ex) {
            throw new IllegalStateException("Store database setup failed: " + ex.getMessage(), ex);
        }
        try (Connection connection = DriverManager.getConnection(
                properties.jdbcUrl(), properties.username(), properties.password());
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()
                        || result.getInt("active") != 1
                        || !PasswordHasher.verify(password, result.getString("password_hash"))) {
                    throw new IllegalArgumentException("Invalid username or password.");
                }
                return new UserDetails(
                        result.getString("username"),
                        result.getString("full_name"),
                        result.getString("role")
                );
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1017) {
                throw new IllegalStateException(
                        "The store database rejected the sadamart_app password. Ask the Oracle administrator to reset it.",
                        ex);
            }
            throw new IllegalStateException(
                    "Store database error (Oracle " + ex.getErrorCode() + "): " + ex.getMessage(),
                    ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to connect to the store database.", ex);
        }
    }

    private static void initializeDatabase(OracleDatabaseProperties properties) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                properties.jdbcUrl(), properties.username(), properties.password())) {
            JdbcConnection liquibaseConnection = new JdbcConnection(connection);
            liquibase.database.Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(liquibaseConnection);
            try (Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private void applyRoleAndOpenWorkspace(String user, String fullName, String role) {
        WorkspaceDataStore.get().setCurrentOperator(fullName);
        WorkspaceDataStore.get().setCurrentRole(role);
        Launcher.navigateTo("/fxml/shell.fxml", "SADA Mart | Store Workspace");
    }

    private record UserDetails(String username, String fullName, String role) { }

    @FXML
    private void forgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Help");
        alert.setHeaderText("Need help signing in?");
        alert.setContentText("Please contact your store manager to reset your password.");
        alert.initOwner(username.getScene().getWindow());
        alert.showAndWait();
    }

}
