package clinic;

import java.time.LocalDate;
import java.util.List;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClientView {
    // The main application object lets this view switch between client pages.
    private final HelloApplication app;

    // ClinicData is the database layer used to load and save client information.
    private final ClinicData data;

    // This is the client account currently using the dashboard.
    private final User loggedInUser;

    // Stores the application, database access, and signed-in client for this view.
    public ClientView(HelloApplication app, ClinicData data, User loggedInUser) {
        this.app = app;
        this.data = data;
        this.loggedInUser = loggedInUser;
    }

    // Builds the client dashboard and places the selected page in its center area.
    public Parent createView(String selectedPage) {
        // DashboardLayout creates the shared top bar and sidebar for client pages.
        BorderPane page = new DashboardLayout(app, loggedInUser)
                .create("Client Portal", selectedPage, false);

        // Decide which client page should appear in the center of the BorderPane.
        if ("request".equals(selectedPage)) {
            // Opens the appointment request form.
            page.setCenter(createAppointmentRequestPage());
        } else if ("appointments".equals(selectedPage)) {
            // Opens the client's appointment history.
            page.setCenter(createAppointmentsPage());
        } else if ("profile".equals(selectedPage)) {
            // Opens the profile edit form.
            page.setCenter(createProfilePage());
        } else {
            // Any unknown or default selection shows the overview page.
            page.setCenter(createOverviewPage());
        }

        return page;
    }

    // Builds the client overview with appointment totals and recent requests.
    private Parent createOverviewPage() {
        // Main vertical container for the client overview page.
        VBox content = ViewComponents.createContentBox("Welcome, " + loggedInUser.getFullName(),
                "Manage your appointments and keep your clinic information up to date.");

        // Count this client's pending and approved appointments for the stat cards.
        int pending = data.countClientStatus(loggedInUser.getUsername(), Appointment.Status.PENDING);
        int approved = data.countClientStatus(loggedInUser.getUsername(),
                Appointment.Status.APPROVED);

        // Build three dashboard cards using values filtered to the signed-in client.
        HBox cards = ViewComponents.createStatRow(
                ViewComponents.createStatCard("My Requests",
                        String.valueOf(getClientAppointments().size())),
                ViewComponents.createStatCard("Pending", String.valueOf(pending)),
                ViewComponents.createStatCard("Approved", String.valueOf(approved)));

        // Load the client's appointment list from the database.
        ListView<Appointment> list = ViewComponents.createAppointmentList(getClientAppointments());

        // Put the appointment list inside a section card.
        VBox section = ViewComponents.createSection("My recent appointments", list);

        // Add stat cards and the recent appointment section to the page.
        content.getChildren().addAll(cards, section);
        return content;
    }

    // Builds the form used by a client to submit a new appointment request.
    private Parent createAppointmentRequestPage() {
        // Main vertical container for the appointment request form.
        VBox content = ViewComponents.createContentBox("Request an Appointment",
                "Enter the visit details and submit the request for admin review.");

        // ComboBox lets the client choose from fixed doctor names.
        ComboBox<String> doctorBox = new ComboBox<>();
        doctorBox.getItems().addAll("Dr. Sarah Nassar", "Dr. Karim Fares", "Dr. Lina Khoury");

        // Set a default doctor so the field is never empty at startup.
        doctorBox.setValue("Dr. Sarah Nassar");
        ViewComponents.widenComboBox(doctorBox);

        // DatePicker provides a real calendar control instead of typing a date manually.
        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(320);

        // ComboBox limits appointment times to the clinic's available slots.
        ComboBox<String> timeBox = new ComboBox<>();
        timeBox.getItems().addAll("09:00 AM", "09:30 AM", "10:00 AM", "11:30 AM",
                "12:30 PM", "02:00 PM", "03:30 PM");

        // Set a default time so the field is valid unless the user changes it.
        timeBox.setValue("09:00 AM");
        ViewComponents.widenComboBox(timeBox);

        // TextArea gives the client room to explain why they need the visit.
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason for your visit");
        ViewComponents.widenTextArea(reasonArea);

        // Feedback label displays validation errors or success after submission.
        Label resultLabel = new Label();

        // GridPane aligns the form labels and inputs.
        GridPane form = ViewComponents.createFormGrid();
        ViewComponents.addFormRow(form, 0, "Doctor", doctorBox);
        ViewComponents.addFormRow(form, 1, "Date", datePicker);
        ViewComponents.addFormRow(form, 2, "Time", timeBox);
        ViewComponents.addFormRow(form, 3, "Reason", reasonArea);

        // Button that sends the appointment request to the database.
        Button submitButton = new Button("Submit Request");
        ViewComponents.stylePrimaryButton(submitButton);
        // Validates the form and saves a pending appointment request when clicked.
        submitButton.setOnAction(event -> {
            // DatePicker returns a LocalDate object, or null if no date is selected.
            LocalDate selectedDate = datePicker.getValue();

            // Require a date before saving the request.
            if (selectedDate == null) {
                ViewComponents.showMessage(resultLabel, "Choose an appointment date.", true);
                return;
            }

            // Require a reason so the admin can understand the request.
            if (reasonArea.getText().isBlank()) {
                ViewComponents.showMessage(resultLabel, "Enter a reason for the visit.", true);
                return;
            }

            // Reject past dates because appointments should be today or later.
            if (selectedDate.isBefore(LocalDate.now())) {
                ViewComponents.showMessage(resultLabel, "Choose today or a future date.", true);
                return;
            }

            // Save a new PENDING appointment connected to the signed-in client's username.
            data.addAppointment(new Appointment(loggedInUser.getUsername(),
                    loggedInUser.getFullName(), doctorBox.getValue(), selectedDate,
                    timeBox.getValue(), reasonArea.getText().trim(), Appointment.Status.PENDING));

            // Clear only the fields the client typed/chose after a successful submit.
            datePicker.setValue(null);
            reasonArea.clear();

            // Display a success message so the client knows the request was saved.
            ViewComponents.showMessage(resultLabel,
                    "Your appointment request was submitted for admin review.", false);
        });

        // Wrap the form, submit button, and message label in a section card.
        VBox section = ViewComponents.createSection("Appointment details", form, submitButton,
                resultLabel);

        // Add the appointment form section to the page.
        content.getChildren().add(section);
        return content;
    }

    // Builds the page that lists the signed-in client's appointment history.
    private Parent createAppointmentsPage() {
        // Main vertical container for the client's appointment history page.
        VBox content = ViewComponents.createContentBox("My Appointments",
                "Track the approval status of your clinic appointment requests.");

        // Load only the signed-in client's appointments.
        ListView<Appointment> list = ViewComponents.createAppointmentList(getClientAppointments());

        // Put the appointment history list inside a section card.
        VBox section = ViewComponents.createSection("Appointment history", list);

        // Add the section to the page.
        content.getChildren().add(section);
        return content;
    }

    // Builds the profile form used by a client to update contact details.
    private Parent createProfilePage() {
        // Main vertical container for the profile page.
        VBox content = ViewComponents.createContentBox("My Profile",
                "Update your contact information for future clinic visits.");

        // Pre-fill the form with the current values from the logged-in User object.
        TextField nameField = new TextField(loggedInUser.getFullName());
        TextField emailField = new TextField(loggedInUser.getEmail());
        TextField phoneField = new TextField(loggedInUser.getPhone());

        // Keep profile fields consistent with the other form widths.
        ViewComponents.widenTextField(nameField);
        ViewComponents.widenTextField(emailField);
        ViewComponents.widenTextField(phoneField);

        // Feedback label for validation or save confirmation.
        Label resultLabel = new Label();

        // The username is shown as a Label because the username is the database key.
        GridPane form = ViewComponents.createFormGrid();
        ViewComponents.addFormRow(form, 0, "Username", new Label(loggedInUser.getUsername()));
        ViewComponents.addFormRow(form, 1, "Full name", nameField);
        ViewComponents.addFormRow(form, 2, "Email", emailField);
        ViewComponents.addFormRow(form, 3, "Phone", phoneField);

        // Button that saves profile changes.
        Button saveButton = new Button("Save Changes");
        ViewComponents.stylePrimaryButton(saveButton);
        // Validates the name and saves the client's updated profile when clicked.
        saveButton.setOnAction(event -> {
            // Full name is required because it appears in appointment records.
            if (nameField.getText().isBlank()) {
                ViewComponents.showMessage(resultLabel, "Your full name cannot be empty.", true);
                return;
            }

            // Update the in-memory user object first.
            loggedInUser.setFullName(nameField.getText().trim());
            loggedInUser.setEmail(emailField.getText().trim());
            loggedInUser.setPhone(phoneField.getText().trim());

            // Persist the changed profile values to SQLite.
            data.updateUser(loggedInUser);

            // Confirm that the save completed.
            ViewComponents.showMessage(resultLabel,
                    "Your profile changes were saved for this session.", false);
        });

        // Wrap the profile form and save controls inside a section card.
        VBox section = ViewComponents.createSection("Personal information", form, saveButton,
                resultLabel);

        // Add the profile section to the page.
        content.getChildren().add(section);
        return content;
    }

    // Loads only the appointments that belong to the signed-in client.
    private List<Appointment> getClientAppointments() {
        // Passing the username prevents a client from seeing other clients' appointments.
        return data.filterAppointments(loggedInUser.getUsername(), null);
    }
}
