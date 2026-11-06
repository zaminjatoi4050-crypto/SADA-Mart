package com.sadamart.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;

/**
 * Hardware-accelerated JavaFX choreography, transition, and micro-interaction engine.
 * Applies smooth cubic-bezier splines across scenes, module transitions, cards,
 * product grids, KPI counters, buttons, and toast notifications.
 */
public final class UiAnimations {
    public static final Interpolator EASE_OUT_EXPO = Interpolator.SPLINE(0.16, 1.0, 0.3, 1.0);
    public static final Interpolator EASE_IN_OUT_CUBIC = Interpolator.SPLINE(0.65, 0.0, 0.35, 1.0);

    private UiAnimations() {
    }

    /** Smooth cross-fade and subtle scale-in when switching top-level scenes. */
    public static void playSceneEntrance(Parent root) {
        root.setOpacity(0.0);
        root.setScaleX(0.985);
        root.setScaleY(0.985);

        FadeTransition fade = new FadeTransition(Duration.millis(320), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(EASE_OUT_EXPO);

        ScaleTransition scale = new ScaleTransition(Duration.millis(340), root);
        scale.setFromX(0.985);
        scale.setFromY(0.985);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(EASE_OUT_EXPO);

        new ParallelTransition(fade, scale).play();
    }

    /** Plays a staggered vertical slide + fade cascade across a list of UI cards or rows. */
    public static void playStaggeredCascade(List<? extends Node> nodes, int baseDelayMs) {
        int index = 0;
        for (Node node : nodes) {
            playNodeEntrance(node, index * baseDelayMs);
            index++;
        }
    }

    /** Animates a single node sliding up and fading in with a configurable delay. */
    public static void playNodeEntrance(Node node, int delayMs) {
        node.setOpacity(0.0);
        node.setTranslateY(18.0);

        FadeTransition fade = new FadeTransition(Duration.millis(360), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(EASE_OUT_EXPO);

        TranslateTransition slide = new TranslateTransition(Duration.millis(380), node);
        slide.setFromY(18.0);
        slide.setToY(0.0);
        slide.setInterpolator(EASE_OUT_EXPO);

        ParallelTransition combined = new ParallelTransition(fade, slide);
        combined.setDelay(Duration.millis(Math.max(0, delayMs)));
        combined.play();
    }

    /** Attaches responsive hover elevation and click spring physics to interactive buttons/cards. */
    public static void attachSpringPhysics(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(160), node);
            st.setToX(1.022);
            st.setToY(1.022);
            st.setInterpolator(EASE_OUT_EXPO);
            st.play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(EASE_OUT_EXPO);
            st.play();
        });
        node.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(90), node);
            st.setToX(0.975);
            st.setToY(0.975);
            st.setInterpolator(EASE_OUT_EXPO);
            st.play();
        });
        node.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(140), node);
            st.setToX(1.015);
            st.setToY(1.015);
            st.setInterpolator(EASE_OUT_EXPO);
            st.play();
        });
    }

    /** Pulses a node (such as the POS basket total or barcode indicator) when updated. */
    public static void playPulseHighlight(Node node) {
        ScaleTransition up = new ScaleTransition(Duration.millis(130), node);
        up.setToX(1.06);
        up.setToY(1.06);
        up.setInterpolator(EASE_OUT_EXPO);

        ScaleTransition down = new ScaleTransition(Duration.millis(190), node);
        down.setToX(1.0);
        down.setToY(1.0);
        down.setInterpolator(EASE_OUT_EXPO);

        new SequentialTransition(up, down).play();
    }

    /** Animates a numeric KPI counter from 0 to its target value with currency/unit formatting. */
    public static void animateCounter(Label label, String prefix, double targetValue, String suffix, int decimals) {
        Timeline timeline = new Timeline();
        int frames = 24;
        String format = "%." + decimals + "f";
        for (int i = 1; i <= frames; i++) {
            double progress = 1.0 - Math.pow(1.0 - ((double) i / frames), 3);
            double current = targetValue * progress;
            String text = prefix + String.format(Locale.US, "%,." + decimals + "f", current) + suffix;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 18.0), e -> label.setText(text)));
        }
        timeline.play();
    }

    /** Displays a non-blocking animated enterprise toast notification at the top-right of the window. */
    public static void showToast(Window owner, String title, String message, boolean success) {
        if (owner == null) return;
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);

        Label badge = new Label(success ? "✓" : "●");
        badge.getStyleClass().add(success ? "toast-badge-success" : "toast-badge-info");

        Label heading = new Label(title);
        heading.getStyleClass().add("toast-title");

        Label body = new Label(message);
        body.getStyleClass().add("toast-body");
        body.setWrapText(true);

        javafx.scene.layout.VBox copy = new javafx.scene.layout.VBox(2, heading, body);
        HBox container = new HBox(12, badge, copy);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("toast-card");
        container.setPrefWidth(360);

        popup.getContent().add(container);
        popup.show(owner, owner.getX() + owner.getWidth() - 395, owner.getY() + 76);

        container.setOpacity(0.0);
        container.setTranslateX(28.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(EASE_OUT_EXPO);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(260), container);
        slideIn.setFromX(28.0);
        slideIn.setToX(0.0);
        slideIn.setInterpolator(EASE_OUT_EXPO);

        PauseTransition hold = new PauseTransition(Duration.millis(2800));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(240), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(new ParallelTransition(fadeIn, slideIn), hold, fadeOut).play();
    }
}
