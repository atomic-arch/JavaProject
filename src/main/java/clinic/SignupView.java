package clinic;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SignupView {
    // Lets this view navigate back to login after account creation.
    private final HelloApplication app;

    // Used to check duplicate usernames and save the new account.
    private final ClinicData data;

    // Stores the application and database access needed by the signup page.
    public SignupView(HelloApplication app, ClinicData data) {
        this.app = app;
        this.data = data;
    }

    // Builds the client signup form and connects its create-account and back actions.
    public Parent createView() {
        // Large title reused from the login page for visual consistency.
        Label clinicName = new Label("MediCare Clinic");
        clinicName.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #116466;");

        // Subtitle explains that this screen creates client accounts.
        Label description = new Label("Create a client account");
        description.setStyle("-fx-text-fill: #687684;");

        // Input fields collect the information needed for one users table row.
        TextField fullNameField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();

        // Prompt text appears inside empty fields to tell the user what to type.
        fullNameField.setPromptText("Full name");
        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        confirmPasswordField.setPromptText("Confirm password");
        emailField.setPromptText("Email");
        phoneField.setPromptText("Phone");

        // Make fields wide enough for names, emails, and phone numbers.
        ViewComponents.widenTextField(fullNameField);
        ViewComponents.widenTextField(usernameField);
        ViewComponents.widenTextField(passwordField);
        ViewComponents.widenTextField(confirmPasswordField);
        ViewComponents.widenTextField(emailField);
        ViewComponents.widenTextField(phoneField);

        // GridPane lays the labels and fields out as a clean form.
        GridPane form = ViewComponents.createFormGrid();
        ViewComponents.addFormRow(form, 0, "Full name", fullNameField);
        ViewComponents.addFormRow(form, 1, "Username", usernameField);
        ViewComponents.addFormRow(form, 2, "Password", passwordField);
        ViewComponents.addFormRow(form, 3, "Confirm password", confirmPasswordField);
        ViewComponents.addFormRow(form, 4, "Email", emailField);
        ViewComponents.addFormRow(form, 5, "Phone", phoneField);

        // Shows validation errors and account-created success messages.
        Label resultLabel = new Label();

        // Button that attempts to create a new client account.
        Button createAccountButton = new Button("Create Account");
        ViewComponents.stylePrimaryButton(createAccountButton);

        // Button that returns to the login screen without creating an account.
        Button backButton = new Button("Back to Sign In");
        ViewComponents.styleSecondaryButton(backButton);

        // Validates the signup fields and saves a new client account when they are valid.
        Runnable createAccount = () -> {
            // Trim username spaces before checking or saving it.
            String username = usernameField.getText().trim();

            // These fields are required because they are NOT NULL in the users table.
            if (fullNameField.getText().isBlank() || username.isBlank()
                    || passwordField.getText().isBlank() || emailField.getText().isBlank()
                    || phoneField.getText().isBlank()) {
                ViewComponents.showMessage(resultLabel, "Complete all fields.", true);
                return;
            }

            // Confirm password reduces accidental typos during signup.
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                ViewComponents.showMessage(resultLabel, "The passwords do not match.", true);
                return;
            }

            // Check the database before inserting so usernames remain unique.
            if (data.usernameExists(username)) {
                ViewComponents.showMessage(resultLabel, "That username is already in use.", true);
                return;
            }

            // Save the account as a CLIENT role; public signup never creates admins.
            data.addUser(new User(username, passwordField.getText(),
                    fullNameField.getText().trim(), emailField.getText().trim(),
                    phoneField.getText().trim(), User.Role.CLIENT));

            // Clear the form so the user can see the submission is complete.
            fullNameField.clear();
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            emailField.clear();
            phoneField.clear();

            // Tell the user the account can now be used on the login screen.
            ViewComponents.showMessage(resultLabel,
                    "Account created. You can now return to sign in.", false);
        };

        // Clicking Create Account runs the shared signup action.
        createAccountButton.setOnAction(event -> createAccount.run());

        // Pressing Enter in the confirm password field also tries to create the account.
        confirmPasswordField.setOnAction(event -> createAccount.run());

        // Back button returns to login without saving anything else.
        backButton.setOnAction(event -> app.showLoginScreen());

        // Place the two buttons beside each other.
        HBox buttons = new HBox(10, createAccountButton, backButton);

        // The signup card contains the form, feedback label, and buttons.
        VBox signupCard = new VBox(12, new Label("Sign up"), form, resultLabel, buttons);
        signupCard.setStyle("-fx-padding: 20; -fx-background-color: white;");

        // The full page stacks the title, subtitle, and signup card.
        VBox page = new VBox(16, clinicName, description, signupCard);
        page.setStyle("-fx-padding: 80; -fx-background-color: #e7f7f4;");
        return page;
    }
}
