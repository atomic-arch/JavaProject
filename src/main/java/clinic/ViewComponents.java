package clinic;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class ViewComponents {
    // Common card styling reused by dashboard cards and page sections.
    private static final String CARD_STYLE =
            "-fx-background-color: white; -fx-border-color: #d7e1e8; -fx-padding: 18;";

    // Prevents creating objects because this class only contains shared helper methods.
    private ViewComponents() {
    }

    // Creates a ListView and fills it with appointments.
    public static ListView<Appointment> createAppointmentList(List<Appointment> appointments) {
        // ListView displays objects by calling their toString method.
        ListView<Appointment> list = new ListView<>();

        // Copy the provided appointments into the JavaFX list control.
        list.getItems().addAll(appointments);

        // Give the list enough vertical room to show several rows.
        list.setPrefHeight(300);
        return list;
    }

    // Creates a ListView and fills it with client accounts.
    public static ListView<User> createClientList(List<User> clients) {
        // ListView displays User objects by calling User.toString().
        ListView<User> list = new ListView<>();

        // Copy the provided clients into the JavaFX list control.
        list.getItems().addAll(clients);

        // Give the list enough vertical room to show several rows.
        list.setPrefHeight(300);
        return list;
    }

    // Creates the main content area with a page title and description.
    public static VBox createContentBox(String title, String description) {
        // Large label for the page heading.
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        // Smaller muted label under the heading.
        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle("-fx-text-fill: #687684;");

        // VBox stacks the title and description vertically.
        VBox content = new VBox(16, titleLabel, descriptionLabel);
        content.setStyle("-fx-padding: 24; -fx-background-color: #f4f7fb;");
        return content;
    }

    // Creates one dashboard statistic card containing a value and label.
    public static VBox createStatCard(String label, String value) {
        // Large number shown at the top of the card.
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: #116466;");

        // Text explaining what the number means.
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-text-fill: #52616d;");

        // Card stacks the value and its label.
        VBox card = new VBox(6, valueLabel, textLabel);
        card.setStyle(CARD_STYLE);
        return card;
    }

    // Creates a styled section with a heading and the provided controls.
    public static VBox createSection(String title, Node... children) {
        // Heading label for this section card.
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // The section starts empty, then receives the title and provided controls.
        VBox section = new VBox(12);
        section.setStyle(CARD_STYLE);

        // Title always appears first.
        section.getChildren().add(titleLabel);

        // Add every JavaFX control passed into this helper.
        section.getChildren().addAll(children);
        return section;
    }

    // Creates a grid with spacing suitable for form rows.
    public static GridPane createFormGrid() {
        // GridPane is used for two-column forms: labels on the left, inputs on the right.
        GridPane form = new GridPane();

        // Horizontal and vertical gaps prevent the controls from touching.
        form.setHgap(14);
        form.setVgap(12);
        return form;
    }

    // Adds a label and its matching input control to one row of a form grid.
    public static void addFormRow(GridPane form, int row, String label, Node input) {
        // Create the left-side label for this form row.
        Label fieldLabel = new Label(label);
        fieldLabel.setStyle("-fx-font-weight: bold;");

        // Column 0 contains the label.
        form.add(fieldLabel, 0, row);

        // Column 1 contains the input control, such as TextField or ComboBox.
        form.add(input, 1, row);
    }

    // Gives a text field enough width for values such as email addresses.
    public static void widenTextField(TextField field) {
        // Pref width tells JavaFX the desired width when laying out the form.
        field.setPrefWidth(320);
    }

    // Gives a ComboBox the same width used by the form text fields.
    public static void widenComboBox(ComboBox<String> comboBox) {
        // Match ComboBox width with TextField width for aligned forms.
        comboBox.setPrefWidth(320);
    }

    // Gives a text area the same width used by the other form controls.
    public static void widenTextArea(TextArea textArea) {
        // Match TextArea width with the other form controls.
        textArea.setPrefWidth(320);
    }

    // Applies the main action color to a button.
    public static void stylePrimaryButton(Button button) {
        // Primary buttons are used for positive actions like save, submit, and sign in.
        button.setStyle("-fx-background-color: #147d80; -fx-text-fill: white;");
    }

    // Applies the neutral action color to a button.
    public static void styleSecondaryButton(Button button) {
        // Secondary buttons are used for navigation or less important actions.
        button.setStyle("-fx-background-color: #e8eef1; -fx-text-fill: #40515c;");
    }

    // Applies the warning color to a destructive action button.
    public static void styleDangerButton(Button button) {
        // Danger buttons are used for actions such as reject or remove.
        button.setStyle("-fx-background-color: #f9e2e2; -fx-text-fill: #a43c3c;");
    }

    // Displays a success or error message using the matching text color.
    public static void showMessage(Label label, String message, boolean error) {
        // Set the visible message first.
        label.setText(message);

        // Choose red for errors and green for success messages.
        if (error) {
            label.setStyle("-fx-text-fill: #ba3c3c;");
        } else {
            label.setStyle("-fx-text-fill: #26743c;");
        }
    }

    // Places several statistic cards next to each other in one row.
    public static HBox createStatRow(VBox... cards) {
        // HBox arranges the statistic cards horizontally with spacing.
        return new HBox(14, cards);
    }
}
