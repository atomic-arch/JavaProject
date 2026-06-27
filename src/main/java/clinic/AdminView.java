package clinic;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminView {
    // The main application object is used when this view needs to switch screens.
    private final HelloApplication app;

    // ClinicData is the database layer; this view calls it instead of writing SQL directly.
    private final ClinicData data;

    // This stores the admin account that is currently signed in.
    private final User loggedInUser;

    // Stores the application, database access, and signed-in administrator for this view.
    public AdminView(HelloApplication app, ClinicData data, User loggedInUser) {
        this.app = app;
        this.data = data;
        this.loggedInUser = loggedInUser;
    }

    // Builds the admin dashboard and places the selected page in its center area.
    public Parent createView(String selectedPage) {
        // DashboardLayout creates the shared top bar and sidebar for all admin pages.
        BorderPane page = new DashboardLayout(app, loggedInUser)
                .create("Admin Portal", selectedPage, true);

        // Decide which admin page should fill the center of the BorderPane.
        if ("appointments".equals(selectedPage)) {
            // Opens the appointment approval page.
            page.setCenter(createAppointmentsPage());
        } else if ("clients".equals(selectedPage)) {
            // Opens the client account management page.
            page.setCenter(createClientsPage());
        } else if ("reports".equals(selectedPage)) {
            // Opens the summary reports page.
            page.setCenter(createReportsPage());
        } else {
            // Any unknown or default selection shows the overview page.
            page.setCenter(createOverviewPage());
        }

        return page;
    }

    // Builds the admin overview with totals and a list of pending requests.
    private Parent createOverviewPage() {
        // Main vertical container for the overview page.
        VBox content = ViewComponents.createContentBox("Dashboard Overview",
                "Review today's clinic activity and pending requests.");

        // The cards pull live counts from the database through ClinicData.
        HBox cards = ViewComponents.createStatRow(
                ViewComponents.createStatCard("Registered Clients",
                        String.valueOf(data.countClients())),
                ViewComponents.createStatCard("Pending Requests",
                        String.valueOf(data.countStatus(Appointment.Status.PENDING))),
                ViewComponents.createStatCard("Approved Visits",
                        String.valueOf(data.countStatus(Appointment.Status.APPROVED))));

        // This list only shows appointments whose status is still pending.
        ListView<Appointment> pendingList = ViewComponents.createAppointmentList(
                data.filterAppointments(null, Appointment.Status.PENDING));

        // Put the pending list inside a white section card.
        VBox section = ViewComponents.createSection("Pending appointment requests", pendingList);

        // Add both the stat cards and the pending section to the page.
        content.getChildren().addAll(cards, section);
        return content;
    }

    // Builds the page where an admin can approve or reject appointments.
    private Parent createAppointmentsPage() {
        // Main vertical container for the appointment management page.
        VBox content = ViewComponents.createContentBox("Appointment Requests",
                "Select an appointment, then approve or reject it.");

        // Load every appointment from the database into a ListView.
        ListView<Appointment> list = ViewComponents.createAppointmentList(data.getAppointments());

        // This label is reused for validation or success messages under the buttons.
        Label resultLabel = new Label();

        // Button used to approve the currently selected appointment.
        Button approveButton = new Button("Approve Selected");
        ViewComponents.stylePrimaryButton(approveButton);
        // Approves the appointment selected in the list.
        approveButton.setOnAction(event ->
                updateSelectedAppointment(list, Appointment.Status.APPROVED, resultLabel));

        // Button used to reject the currently selected appointment.
        Button rejectButton = new Button("Reject Selected");
        ViewComponents.styleDangerButton(rejectButton);
        // Rejects the appointment selected in the list.
        rejectButton.setOnAction(event ->
                updateSelectedAppointment(list, Appointment.Status.REJECTED, resultLabel));

        // Keep the approve and reject buttons beside each other.
        HBox buttons = new HBox(10, approveButton, rejectButton);

        // Build one section containing the list, buttons, and feedback label.
        VBox section = ViewComponents.createSection("All appointment requests", list, buttons,
                resultLabel);

        // Add the section to the page content area.
        content.getChildren().add(section);
        return content;
    }

    // Reloads the appointment list from the database.
    private void refreshAppointmentList(ListView<Appointment> list) {
        // Clear the old items so the ListView does not duplicate appointments.
        list.getItems().clear();

        // Re-read appointments from SQLite after an approve/reject update.
        list.getItems().addAll(data.getAppointments());
    }

    // Saves a new status for the selected appointment and refreshes the visible list.
    private void updateSelectedAppointment(ListView<Appointment> list, Appointment.Status status,
            Label resultLabel) {
        // Read the item the admin clicked in the appointment ListView.
        Appointment selected = list.getSelectionModel().getSelectedItem();

        // If nothing is selected, stop here and tell the admin what to do.
        if (selected == null) {
            ViewComponents.showMessage(resultLabel, "Select an appointment first.", true);
            return;
        }

        // Save the new status to SQLite.
        data.updateAppointmentStatus(selected, status);

        // Reload the list so the visible row matches the database.
        refreshAppointmentList(list);

        // Show a success message after the database update finishes.
        ViewComponents.showMessage(resultLabel, "Appointment status updated.", false);
    }

    // Builds the page where an admin can view, create, and remove client accounts.
    private Parent createClientsPage() {
        // Main vertical container for the client management page.
        VBox content = ViewComponents.createContentBox("Client Accounts",
                "View, add, or remove registered client accounts.");

        // Load all client users from the database into a ListView.
        ListView<User> clientList = ViewComponents.createClientList(data.getClientUsers());

        // Shared feedback label for add/remove actions.
        Label resultLabel = new Label();

        // Text fields used by the admin to type a new client's account information.
        TextField fullNameField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();

        // Make the fields wide enough for normal names, emails, and phone numbers.
        ViewComponents.widenTextField(fullNameField);
        ViewComponents.widenTextField(usernameField);
        ViewComponents.widenTextField(passwordField);
        ViewComponents.widenTextField(emailField);
        ViewComponents.widenTextField(phoneField);

        // GridPane keeps labels in the left column and input controls in the right column.
        GridPane form = ViewComponents.createFormGrid();
        ViewComponents.addFormRow(form, 0, "Full name", fullNameField);
        ViewComponents.addFormRow(form, 1, "Username", usernameField);
        ViewComponents.addFormRow(form, 2, "Password", passwordField);
        ViewComponents.addFormRow(form, 3, "Email", emailField);
        ViewComponents.addFormRow(form, 4, "Phone", phoneField);

        // Button that creates a new CLIENT account.
        Button addButton = new Button("Add Client");
        ViewComponents.stylePrimaryButton(addButton);
        // Validates the form and inserts a new client account when the button is clicked.
        addButton.setOnAction(event -> {
            // Trim username spaces so "client " and "client" are treated the same.
            String username = usernameField.getText().trim();

            // These fields are required because the account cannot work without them.
            if (fullNameField.getText().isBlank() || username.isBlank()
                    || passwordField.getText().isBlank()) {
                ViewComponents.showMessage(resultLabel,
                        "Full name, username, and password are required.", true);
                return;
            }

            // Check the database before inserting so usernames stay unique.
            if (data.usernameExists(username)) {
                ViewComponents.showMessage(resultLabel, "That username is already in use.", true);
                return;
            }

            // Save the new client account in the users table with the CLIENT role.
            data.addUser(new User(username, passwordField.getText(),
                    fullNameField.getText().trim(), emailField.getText().trim(),
                    phoneField.getText().trim(), User.Role.CLIENT));

            // Reload the client list so the new account appears immediately.
            refreshClientList(clientList);

            // Clear the form after a successful insert.
            fullNameField.clear();
            usernameField.clear();
            passwordField.clear();
            emailField.clear();
            phoneField.clear();

            // Tell the admin the account was created.
            ViewComponents.showMessage(resultLabel, "Client added.", false);
        });

        // Button that removes the selected client account.
        Button removeButton = new Button("Remove Selected Client");
        ViewComponents.styleDangerButton(removeButton);
        // Deletes the client selected in the list when the button is clicked.
        removeButton.setOnAction(event -> {
            // Read the selected User object from the ListView.
            User selected = clientList.getSelectionModel().getSelectedItem();

            // Stop early if the admin clicked remove without selecting a client.
            if (selected == null) {
                ViewComponents.showMessage(resultLabel, "Select a client first.", true);
                return;
            }

            // Delete the client from SQLite; their appointments are removed by cascade.
            data.removeClient(selected);

            // Reload the list so the removed client disappears from the screen.
            refreshClientList(clientList);

            // Show a success message after the delete finishes.
            ViewComponents.showMessage(resultLabel, "Client removed.", false);
        });

        // Left section: currently registered clients.
        VBox clientSection = ViewComponents.createSection("Registered clients", clientList,
                removeButton);

        // Right section: form for creating a new client.
        VBox addSection = ViewComponents.createSection("Add a client", form, addButton,
                resultLabel);

        // Place both sections next to each other on the same row.
        content.getChildren().add(new HBox(14, clientSection, addSection));
        return content;
    }

    // Reloads the visible client list from the database.
    private void refreshClientList(ListView<User> clientList) {
        // Remove stale ListView rows first.
        clientList.getItems().clear();

        // Read the latest client rows from SQLite.
        clientList.getItems().addAll(data.getClientUsers());
    }

    // Builds a page containing summary totals for the clinic.
    private Parent createReportsPage() {
        // Main vertical container for the reports page.
        VBox content = ViewComponents.createContentBox("Clinic Reports",
                "A simple visual summary of the current in-memory records.");

        // Top stat cards use counts calculated from current database records.
        HBox cards = ViewComponents.createStatRow(
                ViewComponents.createStatCard("Total Requests",
                        String.valueOf(data.countAppointments())),
                ViewComponents.createStatCard("Pending",
                        String.valueOf(data.countStatus(Appointment.Status.PENDING))),
                ViewComponents.createStatCard("Rejected",
                        String.valueOf(data.countStatus(Appointment.Status.REJECTED))));

        // GridPane report displays label/value pairs in two columns.
        GridPane report = ViewComponents.createFormGrid();

        // Each row calls the data layer to calculate one report value.
        addReportRow(report, 0, "Registered clients", String.valueOf(data.countClients()));
        addReportRow(report, 1, "Approved appointments",
                String.valueOf(data.countStatus(Appointment.Status.APPROVED)));
        addReportRow(report, 2, "Pending appointments",
                String.valueOf(data.countStatus(Appointment.Status.PENDING)));
        addReportRow(report, 3, "Rejected appointments",
                String.valueOf(data.countStatus(Appointment.Status.REJECTED)));

        // Wrap the report grid in a styled section card.
        VBox section = ViewComponents.createSection("Current clinic totals", report);

        // Add both the summary cards and the detailed grid to the page.
        content.getChildren().addAll(cards, section);
        return content;
    }

    // Adds one label and value pair to the report grid.
    private void addReportRow(GridPane report, int row, String label, String value) {
        // The left label explains what this row is measuring.
        Label labelControl = new Label(label);

        // The right label contains the actual calculated number.
        Label valueControl = new Label(value);
        valueControl.setStyle("-fx-font-weight: bold; -fx-text-fill: #116466;");

        // Add both controls to the requested row in the report grid.
        report.add(labelControl, 0, row);
        report.add(valueControl, 1, row);
    }
}
