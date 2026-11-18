package Testing_for_bascially_everything;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AtlantaFX_test extends Application {

    @Override
    public void start(Stage stage) {
        // Apply the modern theme
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        ModalPane modal = new ModalPane();

        // Poster-grid-style layout: wraps automatically
        TilePane grid = new TilePane(12, 12);
        grid.setPadding(new Insets(20));
        for (int i = 1; i <= 12; i++) {
            Button card = new Button("Movie " + i);
            card.setPrefSize(150, 220);
            card.setOnAction(e -> showDialog(modal, "Movie " + finalLabel(card)));
            grid.getChildren().add(card);
        }

        // ModalPane must sit on top of your content
        StackPane root = new StackPane(grid, modal);

        stage.setTitle("Movies");
        stage.setScene(new Scene(root, 1000, 650));
        stage.show();
    }

    private static String finalLabel(Button b) {
        return b.getText();
    }

    private void showDialog(ModalPane modal, String title) {
        Button close = new Button("Close");
        close.setOnAction(e -> modal.hide(true));

        VBox box = new VBox(12, new Label(title), close);
        box.setPadding(new Insets(20));
        box.setMaxSize(300, 150);

        modal.show(box);
    }

    public static void main(String[] args) {
        launch(args);
    }
}