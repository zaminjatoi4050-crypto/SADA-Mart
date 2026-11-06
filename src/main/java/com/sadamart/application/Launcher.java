package com.sadamart.application;

import com.sadamart.ui.UiAnimations;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Enterprise JavaFX entry point and centralized scene router for SADA Mart.
 * Provides smooth animated scene transitions between Sign-In, Store Setup Wizard,
 * and the 13-module Retail Operations Shell.
 */
public class Launcher extends Application {
    private static Stage primaryStage;
    private static boolean darkTheme = false;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setResizable(true);
        stage.setMinWidth(820);
        stage.setMinHeight(560);
        stage.setWidth(1100);
        stage.setHeight(700);
        navigateTo("/fxml/login.fxml", "SADA Mart | Sign In");
        stage.show();
    }

    public static void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    Launcher.class.getResource(fxmlPath), "Missing FXML resource: " + fxmlPath));
            Parent root = loader.load();
            applyThemeClass(root);

            Scene currentScene = primaryStage.getScene();
            if (currentScene == null) {
                Scene scene = new Scene(root, 1100, 700);
                scene.getStylesheets().add(Objects.requireNonNull(
                        Launcher.class.getResource("/css/app.css")).toExternalForm());
                primaryStage.setScene(scene);
                installResponsiveLayout(scene, root);
            } else {
                currentScene.setRoot(root);
                installResponsiveLayout(currentScene, root);
            }
            primaryStage.setTitle(title);
            UiAnimations.playSceneEntrance(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load view: " + fxmlPath, ex);
        }
    }

    private static void installResponsiveLayout(Scene scene, Parent root) {
        Runnable update = () -> {
            Node hero = root.lookup(".login-hero");
            if (hero != null) {
                boolean compact = scene.getWidth() < 1050;
                hero.setVisible(!compact);
                hero.setManaged(!compact);
            }
        };
        scene.widthProperty().addListener((obs, oldValue, newValue) -> update.run());
        scene.heightProperty().addListener((obs, oldValue, newValue) -> update.run());
        update.run();
    }

    public static void setDarkTheme(boolean dark) {
        darkTheme = dark;
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyThemeClass(primaryStage.getScene().getRoot());
        }
    }

    public static boolean isDarkTheme() {
        return darkTheme;
    }

    public static void applyThemeClass(Parent root) {
        if (root == null) return;
        root.getStyleClass().remove("dark");
        if (darkTheme) {
            root.getStyleClass().add("dark");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
