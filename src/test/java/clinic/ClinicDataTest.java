package clinic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class ClinicDataTest {
    // Runs database operations against a temporary file and checks that they persist correctly.
    public static void main(String[] args) throws Exception {
        // The test script passes a temporary database path using -Dclinic.database.
        Path databasePath = Path.of(System.getProperty("clinic.database"));

        // Start with a clean test database every time the test runs.
        Files.deleteIfExists(databasePath);

        // Creating ClinicData also creates tables and inserts demo records.
        ClinicData data = new ClinicData();

        // Confirm the demo seed data was inserted correctly.
        check(data.countClients() == 2, "The demo clients were not inserted.");
        check(data.countAppointments() == 3, "The demo appointments were not inserted.");

        // Create a new client for testing insert, update, and delete behavior.
        User testClient = new User("test-client", "test123", "Test Client",
                "test@example.com", "00 000 000", User.Role.CLIENT);

        // WRITE TEST: save the new client to the users table.
        data.addUser(testClient);

        // WRITE TEST: save an appointment connected to the new client.
        data.addAppointment(new Appointment("test-client", "Test Client", "Dr. Lina Khoury",
                LocalDate.now().plusDays(5), "02:00 PM", "Database test",
                Appointment.Status.PENDING));

        // READ TEST: load the appointment back by filtering for this client.
        List<Appointment> appointments = data.filterAppointments("test-client", null);
        check(appointments.size() == 1, "The new appointment was not saved.");

        // WRITE TEST: approve the appointment and save the status update.
        data.updateAppointmentStatus(appointments.get(0), Appointment.Status.APPROVED);

        // WRITE TEST: change the profile name and save the update.
        testClient.setFullName("Updated Test Client");
        data.updateUser(testClient);

        // Create a fresh ClinicData instance to prove values were saved to disk, not only memory.
        ClinicData reloadedData = new ClinicData();

        // READ TEST: log the user in from the reloaded database connection.
        User reloadedUser = reloadedData.findUser("test-client", "test123");
        check(reloadedUser != null, "The new client did not persist after reloading.");

        // READ TEST: confirm the updated profile name persisted.
        check(reloadedUser.getFullName().equals("Updated Test Client"),
                "The profile update did not persist.");

        // READ TEST: confirm the appointment status update persisted.
        check(reloadedData.countClientStatus("test-client", Appointment.Status.APPROVED) == 1,
                "The appointment status update did not persist.");

        // WRITE TEST: delete the test user.
        reloadedData.removeClient(reloadedUser);

        // READ TEST: confirm the user was deleted.
        check(!reloadedData.usernameExists("test-client"), "The test client was not deleted.");

        // READ TEST: confirm cascade delete removed the user's appointments too.
        check(reloadedData.filterAppointments("test-client", null).isEmpty(),
                "Deleting a client did not delete their appointments.");

        // Remove the temporary database file after the test finishes.
        Files.deleteIfExists(databasePath);

        // Print a success line so the shell script has clear output.
        System.out.println("SQLite persistence test passed.");
    }

    // Stops the test with a readable error when an expected condition is false.
    private static void check(boolean condition, String message) {
        // Throwing an exception fails the test script immediately.
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
