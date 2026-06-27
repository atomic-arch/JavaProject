package clinic;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginView {
    // Lets this view tell the main app to switch screens after login or signup.
    private final HelloApplication app;

    // Used to check login credentials against the database.
    private final ClinicData data;

    // Stores the application and database access needed by the login page.
    public LoginView(HelloApplication app, ClinicData data) {
        this.app = app;
        this.data = data;
    }

    // Builds the login form and connects its sign-in and signup button actions.
    public Parent createView() {
        // Large title at the top of the login page.
        Label clinicName = new Label("MediCare Clinic");
        clinicName.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #116466;");

        // Subtitle explaining what the application is.
        Label description = new Label("Appointment Management System");
        description.setStyle("-fx-text-fill: #687684;");

        // Field where the user types their username.
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        ViewComponents.widenTextField(usernameField);

        // PasswordField hides typed characters while the user enters their password.
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ViewComponents.widenTextField(passwordField);

        // Displays login errors, such as an incorrect username or password.
        Label errorLabel = new Label();

        // Button that triggers the login attempt.
        Button signInButton = new Button("Sign In");
        ViewComponents.stylePrimaryButton(signInButton);

        // Button that switches to the signup page.
        Button signUpButton = new Button("Create Account");
        ViewComponents.styleSecondaryButton(signUpButton);

        // Attempts to find the entered account and opens its dashboard when it exists.
        Runnable signIn = () -> {
            // Ask the database for a user row matching the entered credentials.
            User user = data.findUser(usernameField.getText().trim(), passwordField.getText());

            // If no matching row exists, show an error and stay on the login page.
            if (user == null) {
                ViewComponents.showMessage(errorLabel, "Incorrect username or password.", true);
                return;
            }

            // If login succeeds, hand the user to HelloApplication for role-based routing.
            app.logIn(user);
        };

        // Clicking Sign In runs the shared login action.
        signInButton.setOnAction(event -> signIn.run());

        // Clicking Create Account opens the signup page.
        signUpButton.setOnAction(event -> app.showSignupScreen());

        // Pressing Enter inside the password field also runs login.
        passwordField.setOnAction(event -> signIn.run());

        // The login card groups the inputs, message label, and buttons.
        HBox buttons = new HBox(10, signInButton, signUpButton);
        VBox loginCard = new VBox(12, new Label("Welcome back"), usernameField,
                passwordField, errorLabel, buttons);
        loginCard.setStyle("-fx-padding: 20; -fx-background-color: white;");

        // The page stacks the title, subtitle, and login card vertically.
        VBox page = new VBox(16, clinicName, description, loginCard);
        page.setStyle("-fx-padding: 80; -fx-background-color: #e7f7f4;");
        return page;
    }
}
