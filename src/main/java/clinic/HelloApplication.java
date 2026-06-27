package clinic;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    // One shared ClinicData object is used by all screens so they talk to the same database.
    private final ClinicData data = new ClinicData();

    // The Stage is the main JavaFX window; each screen replaces its Scene.
    private Stage stage;

    // This holds the user after a successful login, or null before login/logout.
    private User loggedInUser;

    // Starts JavaFX, stores the main window, and displays the initial screen.
    @Override
    public void start(Stage stage) {
        // Save the Stage so other methods can change what is displayed later.
        this.stage = stage;

        // Set the window title shown by the operating system.
        stage.setTitle("MediCare Clinic");

        // The app always begins at the login page.
        showLoginScreen();

        // Actually displays the Stage after its first Scene is ready.
        stage.show();
    }

    // Clears the signed-in user and displays the login page.
    void showLoginScreen() {
        // Logging out or returning to login means no user is currently active.
        loggedInUser = null;

        // Build a new LoginView and place it in the main window.
        stage.setScene(createScene(new LoginView(this, data).createView()));
    }

    // Clears the signed-in user and displays the account creation page.
    void showSignupScreen() {
        // Signup is outside any logged-in dashboard, so clear the active user.
        loggedInUser = null;

        // Build a new SignupView and place it in the main window.
        stage.setScene(createScene(new SignupView(this, data).createView()));
    }

    // Stores the signed-in user and opens the correct dashboard for their role.
    void logIn(User user) {
        // Remember the account returned from the database login check.
        loggedInUser = user;

        // Admins and clients get different dashboards and different allowed actions.
        if (user.getRole() == User.Role.ADMIN) {
            // Admins start on the admin overview page.
            showAdminDashboard("overview");
        } else {
            // Clients start on the client overview page.
            showClientDashboard("overview");
        }
    }

    // Displays the requested page inside the admin dashboard.
    void showAdminDashboard(String selectedPage) {
        // Rebuild the admin dashboard with the selected sidebar page active.
        stage.setScene(createScene(new AdminView(this, data, loggedInUser).createView(selectedPage)));
    }

    // Displays the requested page inside the client dashboard.
    void showClientDashboard(String selectedPage) {
        // Rebuild the client dashboard with the selected sidebar page active.
        stage.setScene(createScene(new ClientView(this, data, loggedInUser).createView(selectedPage)));
    }

    // Creates a scene using the default application window size.
    Scene createScene(Parent root) {
        // Use one default size so all screens open consistently.
        return createScene(root, 1120, 740);
    }

    // Creates a scene containing the given JavaFX root element and dimensions.
    Scene createScene(Parent root, double width, double height) {
        // Scene is the container JavaFX uses to display a tree of controls in the Stage.
        return new Scene(root, width, height);
    }

    // Launches the JavaFX application.
    public static void main(String[] args) {
        // launch calls start(...) after JavaFX finishes its startup work.
        launch();
    }
}
