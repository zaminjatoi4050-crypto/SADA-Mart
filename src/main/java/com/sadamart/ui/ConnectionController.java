package com.sadamart.ui;

import com.sadamart.application.Launcher;
import com.sadamart.infrastructure.OracleDatabaseProperties;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Rebuilt Oracle Connection & Security Bootstrap Controller.
 * Adds TCPS/TLS toggle, live JDBC URL preview via OracleDatabaseProperties,
 * animated progress feedback, and bidirectional navigation back to Sign-In or into the Workspace Shell.
 */
public final class ConnectionController {
    @FXML private VBox formCard;
    @FXML private TextField host, port, service, username;
    @FXML private PasswordField password;
    @FXML private CheckBox tcpsToggle;
    @FXML private Label jdbcPreview, status;
    @FXML private Button testButton, clearButton, backButton, workspaceButton;
    @FXML private ProgressIndicator progress;

    @FXML
    private void initialize() {
        host.setText("localhost");
        port.setText("1521");
        service.setText("FREEPDB1");
        username.setText("sadamart_app");

        host.textProperty().addListener((obs, o, n) -> updateJdbcPreview());
        port.textProperty().addListener((obs, o, n) -> updateJdbcPreview());
        service.textProperty().addListener((obs, o, n) -> updateJdbcPreview());
        tcpsToggle.selectedProperty().addListener((obs, o, n) -> {
            if (n && "1521".equals(port.getText().trim())) {
                port.setText("2484");
            } else if (!n && "2484".equals(port.getText().trim())) {
                port.setText("1521");
            }
            updateJdbcPreview();
        });
        updateJdbcPreview();

        if (formCard != null) {
            UiAnimations.playNodeEntrance(formCard, 40);
        }
        for (Node n : List.of(testButton, clearButton, backButton, workspaceButton)) {
            if (n != null) UiAnimations.attachSpringPhysics(n);
        }
    }

    private void updateJdbcPreview() {
        String h = host.getText() == null || host.getText().isBlank() ? "localhost" : host.getText().trim();
        String p = port.getText() == null || port.getText().isBlank() ? "1521" : port.getText().trim();
        String s = service.getText() == null || service.getText().isBlank() ? "FREEPDB1" : service.getText().trim();
        String protocol = tcpsToggle.isSelected() ? "tcps://" : "//";
        jdbcPreview.setText("jdbc:oracle:thin:@" + protocol + h + ":" + p + "/" + s);
    }

    @FXML
    private void testConnection() {
        String h = host.getText().trim();
        String p = port.getText().trim();
        String s = service.getText().trim();
        String u = username.getText().trim();
        String secret = password.getText();

        if (!h.matches("[a-zA-Z0-9.-]+") || !p.matches("[0-9]{1,5}") || !s.matches("[a-zA-Z0-9_.-]+") || u.isBlank() || secret.isEmpty()) {
            status.setText("Validation Error: Enter a valid host, numeric port, service name, dedicated username, and password.");
            UiAnimations.playPulseHighlight(status);
            return;
        }
        int portNum = Integer.parseInt(p);
        OracleDatabaseProperties dbProps = new OracleDatabaseProperties(true, h, portNum, s, u, secret, tcpsToggle.isSelected());
        try {
            dbProps.validate();
        } catch (IllegalArgumentException ex) {
            status.setText(ex.getMessage());
            UiAnimations.playPulseHighlight(status);
            return;
        }

        String targetUrl = dbProps.jdbcUrl();
        testButton.setDisable(true);
        progress.setVisible(true);
        status.setText("Probing " + targetUrl + " (5s timeout)…");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                Properties props = new Properties();
                props.setProperty("user", u);
                props.setProperty("password", secret);
                props.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
                props.setProperty("oracle.jdbc.ReadTimeout", "5000");
                try (Connection connection = DriverManager.getConnection(targetUrl, props)) {
                    if (!connection.isValid(5)) {
                        throw new SQLException("Connection validation check failed");
                    }
                    return "Connected to Oracle " + connection.getMetaData().getDatabaseProductVersion().split("\\n")[0];
                } finally {
                    props.remove("password");
                }
            }
        };
        task.setOnSucceeded(e -> {
            WorkspaceDataStore.get().setOracleConnected(true);
            WorkspaceDataStore.get().setOracleEndpoint(h + ":" + portNum + "/" + s);
            status.setText(task.getValue() + " · Store branch is ready.");
            testButton.setDisable(false);
            progress.setVisible(false);
            UiAnimations.showToast(host.getScene().getWindow(), "Store Setup Verified", task.getValue(), true);
        });
        task.setOnFailed(e -> {
            WorkspaceDataStore.get().setOracleConnected(false);
            WorkspaceDataStore.get().setOracleEndpoint(h + ":" + portNum + "/" + s);
            status.setText("Could not reach branch server at " + targetUrl + ". Workspace remains available in Interactive Mode.");
            testButton.setDisable(false);
            progress.setVisible(false);
        });
        Thread thread = new Thread(task, "oracle-connection-test");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearPassword() {
        password.clear();
        status.setText("Password cleared from form field.");
    }

    @FXML
    private void backToLogin() {
        Stage stage = (Stage) host.getScene().getWindow();
        if (stage.getOwner() != null) {
            stage.close();
        } else {
            Launcher.navigateTo("/fxml/login.fxml", "SADA Mart | Sign In");
        }
    }

    @FXML
    private void openWorkspace() {
        Stage stage = (Stage) host.getScene().getWindow();
        if (stage.getOwner() != null) {
            stage.close();
        } else {
            Launcher.navigateTo("/fxml/shell.fxml", "SADA Mart | Store Workspace");
        }
    }
}
