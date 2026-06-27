package clinic;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DashboardLayout {
    // The app reference lets navigation buttons switch scenes.
    private final HelloApplication app;

    // The dashboard displays this user's name in the top bar.
    private final User loggedInUser;

    // Stores the application and signed-in user used by the shared dashboard layout.
    public DashboardLayout(HelloApplication app, User loggedInUser) {
        this.app = app;
        this.loggedInUser = loggedInUser;
    }

    // Builds the shared top bar and sidebar used by admin and client dashboards.
    public BorderPane create(String portalName, String selectedPage, boolean admin) {
        // Branding label shown on every dashboard screen.
        Label clinicLabel = new Label("MediCare Clinic");
        clinicLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #116466;");

        // Portal label tells the user whether they are in the admin or client area.
        Label portalLabel = new Label(portalName);

        // Shows which account is currently signed in.
        Label userLabel = new Label("Signed in as: " + loggedInUser.getFullName());

        // Logout button sends the user back to the login screen.
        Button logoutButton = new Button("Log Out");
        ViewComponents.styleSecondaryButton(logoutButton);
        // Returns the user to the login screen when the logout button is clicked.
        logoutButton.setOnAction(event -> app.showLoginScreen());

        // Top bar lays out branding, portal name, signed-in user, and logout button horizontally.
        HBox topBar = new HBox(16, clinicLabel, portalLabel, userLabel, logoutButton);
        topBar.setStyle("-fx-padding: 16; -fx-background-color: white;");

        // Sidebar holds the navigation buttons for the selected role.
        VBox sidebar = new VBox(8);
        sidebar.setStyle("-fx-padding: 16; -fx-background-color: #183c45;");

        // Admin and client dashboards have different navigation options.
        if (admin) {
            // Add admin-only pages such as clients and reports.
            addAdminButtons(sidebar, selectedPage);
        } else {
            // Add client-only pages such as request appointment and profile.
            addClientButtons(sidebar, selectedPage);
        }

        // Small footer label appears below the navigation buttons.
        Label footerLabel = new Label("Clinic Appointment System");
        footerLabel.setStyle("-fx-text-fill: #aac5ca;");
        sidebar.getChildren().add(footerLabel);

        // BorderPane gives the app a top bar, left sidebar, and center content area.
        BorderPane page = new BorderPane();
        page.setTop(topBar);
        page.setLeft(sidebar);
        return page;
    }

    // Adds the admin navigation buttons to the sidebar.
    private void addAdminButtons(VBox sidebar, String selectedPage) {
        // Each button calls back into HelloApplication to rebuild the admin dashboard page.
        sidebar.getChildren().addAll(
                createSidebarButton("Overview", "overview", selectedPage,
                        () -> app.showAdminDashboard("overview")),
                createSidebarButton("Appointments", "appointments", selectedPage,
                        () -> app.showAdminDashboard("appointments")),
                createSidebarButton("Clients", "clients", selectedPage,
                        () -> app.showAdminDashboard("clients")),
                createSidebarButton("Reports", "reports", selectedPage,
                        () -> app.showAdminDashboard("reports")));
    }

    // Adds the client navigation buttons to the sidebar.
    private void addClientButtons(VBox sidebar, String selectedPage) {
        // Each button calls back into HelloApplication to rebuild the client dashboard page.
        sidebar.getChildren().addAll(
                createSidebarButton("Overview", "overview", selectedPage,
                        () -> app.showClientDashboard("overview")),
                createSidebarButton("Request Appointment", "request", selectedPage,
                        () -> app.showClientDashboard("request")),
                createSidebarButton("My Appointments", "appointments", selectedPage,
                        () -> app.showClientDashboard("appointments")),
                createSidebarButton("My Profile", "profile", selectedPage,
                        () -> app.showClientDashboard("profile")));
    }

    // Creates one sidebar button, highlights it when selected, and connects its action.
    private Button createSidebarButton(String text, String pageName, String selectedPage,
            Runnable action) {
        // Create the actual clickable JavaFX Button.
        Button button = new Button(text);

        // Let the button stretch to fill the sidebar width.
        button.setMaxWidth(Double.MAX_VALUE);

        // Runs the page navigation action when this sidebar button is clicked.
        button.setOnAction(event -> action.run());

        // Highlight the button for the page the user is currently viewing.
        if (pageName.equals(selectedPage)) {
            button.setStyle("-fx-background-color: #2c6670; -fx-text-fill: white;");
        } else {
            // Non-selected buttons are transparent but still readable on the dark sidebar.
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #dcebee;");
        }

        return button;
    }
}
