package clinic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClinicData {
    // JDBC connection string used whenever this class opens SQLite.
    private final String databaseUrl;

    // Prepares the local SQLite file, loads its driver, and initializes its tables.
    public ClinicData() {
        // Use a system property during tests, otherwise use the normal app database file.
        Path databasePath = Path.of(System.getProperty("clinic.database", "data/clinic.db"))
                .toAbsolutePath();

        try {
            // Make sure the folder for the database file exists before connecting.
            Files.createDirectories(databasePath.getParent());

            // Load the SQLite JDBC driver from lib/sqlite-jdbc-*.jar.
            Class.forName("org.sqlite.JDBC");
        } catch (IOException | ClassNotFoundException exception) {
            // Stop the app early if the database folder or driver cannot be prepared.
            throw new IllegalStateException("Could not prepare the SQLite database.", exception);
        }

        // SQLite JDBC uses jdbc:sqlite: followed by the database file path.
        databaseUrl = "jdbc:sqlite:" + databasePath;

        // Create tables and seed demo data if this is a new empty database.
        initializeDatabase();
    }

    // Creates the required tables and inserts demo records when the database is empty.
    private void initializeDatabase() {
        // users stores both admin and client login/profile records.
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    role TEXT NOT NULL
                )
                """;

        // appointments stores appointment requests and links each one to a client username.
        String createAppointmentsTable = """
                CREATE TABLE IF NOT EXISTS appointments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    client_username TEXT NOT NULL,
                    doctor TEXT NOT NULL,
                    appointment_date TEXT NOT NULL,
                    appointment_time TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (client_username) REFERENCES users(username) ON DELETE CASCADE
                )
                """;

        // Open one connection to create both tables.
        try (Connection connection = connect();
                Statement statement = connection.createStatement()) {
            // executeUpdate is used here because CREATE TABLE changes the database structure.
            statement.executeUpdate(createUsersTable);
            statement.executeUpdate(createAppointmentsTable);

            // Only seed demo records when the users table is empty.
            if (countRows(connection, "users") == 0) {
                seedSampleData(connection);
            }
        } catch (SQLException exception) {
            // Convert checked SQLExceptions into one consistent runtime error.
            throw databaseError(exception);
        }
    }

    // Opens a database connection and enables SQLite foreign key rules for it.
    private Connection connect() throws SQLException {
        // Open a new connection to the SQLite database file.
        Connection connection = DriverManager.getConnection(databaseUrl);

        // SQLite requires this PRAGMA on each connection for ON DELETE CASCADE to work.
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        // Return the prepared connection to the caller.
        return connection;
    }

    // Counts the records in a table while the database is being initialized.
    private int countRows(Connection connection, String tableName) throws SQLException {
        // This helper is only used with trusted table names inside this class.
        try (Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            // COUNT(*) returns one row with one number in the first column.
            return results.getInt(1);
        }
    }

    // Inserts the starting admin, clients, and appointments as one transaction.
    private void seedSampleData(Connection connection) throws SQLException {
        // Disable auto-commit so all seed records succeed together or rollback together.
        connection.setAutoCommit(false);

        try {
            // Demo admin account used to access the admin dashboard.
            insertUser(connection, new User("admin", "admin123", "Clinic Administrator",
                    "admin@medicare.local", "01 000 000", User.Role.ADMIN));

            // Demo client account used to access the client dashboard.
            insertUser(connection, new User("client", "client123", "Rami Haddad",
                    "rami.haddad@example.com", "03 111 222", User.Role.CLIENT));

            // Second demo client so the admin screens have more than one client to show.
            insertUser(connection, new User("maya", "maya123", "Maya Saad",
                    "maya.saad@example.com", "70 222 333", User.Role.CLIENT));

            // Demo pending request for the first client.
            insertAppointment(connection, new Appointment("client", "Rami Haddad",
                    "Dr. Sarah Nassar", LocalDate.now().plusDays(3), "10:00 AM",
                    "Routine check-up", Appointment.Status.PENDING));

            // Demo approved request for the second client.
            insertAppointment(connection, new Appointment("maya", "Maya Saad",
                    "Dr. Karim Fares", LocalDate.now().plusDays(1), "12:30 PM",
                    "Follow-up visit", Appointment.Status.APPROVED));

            // Demo older approved appointment for appointment history.
            insertAppointment(connection, new Appointment("client", "Rami Haddad",
                    "Dr. Karim Fares", LocalDate.now().minusDays(10), "09:30 AM",
                    "Consultation", Appointment.Status.APPROVED));

            // Save all seeded users and appointments.
            connection.commit();
        } catch (SQLException exception) {
            // Undo all seed inserts if any one insert fails.
            connection.rollback();
            throw exception;
        } finally {
            // Restore normal auto-commit behavior before returning the connection.
            connection.setAutoCommit(true);
        }
    }

    // Reads and returns the user whose username and password match the login form.
    public User findUser(String username, String password) {
        // READ: selects one user row that matches the entered username and password.
        String sql = """
                SELECT username, password, full_name, email, phone, role
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            // Fill the ? placeholders with the login form values.
            statement.setString(1, username);
            statement.setString(2, password);

            // executeQuery is used for SELECT statements because they return rows.
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    // Convert the first matching database row into a User object.
                    return readUser(results);
                }
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }

        // Returning null tells the login page that no matching account was found.
        return null;
    }

    // Opens a connection and saves a user in the database.
    public void addUser(User user) {
        try (Connection connection = connect()) {
            // WRITE: reuse the insert helper so signup and admin-created clients save the same way.
            insertUser(connection, user);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    // Runs the INSERT statement that writes one user record to the database.
    private void insertUser(Connection connection, User user) throws SQLException {
        // WRITE: inserts one full account row into the users table.
        String sql = """
                INSERT INTO users (username, password, full_name, email, phone, role)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Fill each ? placeholder with values from the User object.
            statement.setString(1, user.getUsername());
            // A real application should hash passwords before storing them. This class project
            // keeps them readable in the database so the stored values are easy to inspect.
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getRole().name());
            // executeUpdate is used for INSERT because it changes the database.
            statement.executeUpdate();
        }
    }

    // Deletes a client account and its appointments from the database.
    public void removeClient(User client) {
        // WRITE: deletes one user row. The foreign key also deletes that client's appointments.
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            // Delete by username because username is the primary key in the users table.
            statement.setString(1, client.getUsername());
            // executeUpdate is used for DELETE because it changes the database.
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    // Checks whether a username is already stored in the database.
    public boolean usernameExists(String username) {
        // READ: counts matching usernames to prevent duplicate accounts.
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";

        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            // LOWER makes the duplicate check case-insensitive.
            statement.setString(1, username);

            // executeQuery is used because SELECT COUNT returns one result row.
            try (ResultSet results = statement.executeQuery()) {
                // If the count is greater than zero, the username already exists.
                return results.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    // Opens a connection and saves an appointment in the database.
    public void addAppointment(Appointment appointment) {
        try (Connection connection = connect()) {
            // WRITE: reuse the insert helper so all new appointment requests save consistently.
            insertAppointment(connection, appointment);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    // Runs the INSERT statement that writes one appointment record to the database.
    private void insertAppointment(Connection connection, Appointment appointment)
            throws SQLException {
        // WRITE: inserts one appointment row linked to a client username.
        String sql = """
                INSERT INTO appointments (
                    client_username, doctor, appointment_date, appointment_time, reason, status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Fill each ? placeholder with values from the Appointment object.
            statement.setString(1, appointment.getClientUsername());
            statement.setString(2, appointment.getDoctor());
            statement.setString(3, appointment.getDate().toString());
            statement.setString(4, appointment.getTime());
            statement.setString(5, appointment.getReason());
            statement.setString(6, appointment.getStatus().name());
            // executeUpdate is used for INSERT because it changes the database.
            statement.executeUpdate();
        }
    }

    // Updates an appointment's database status and its matching Java object.
    public void updateAppointmentStatus(Appointment appointment, Appointment.Status status) {
        // WRITE: changes only the status column for the selected appointment ID.
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";

        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            // Match the appointment by its database ID, then store the new enum name.
            statement.setString(1, status.name());
            statement.setInt(2, appointment.getId());
            // executeUpdate is used for UPDATE because it changes the database.
            statement.executeUpdate();
            // Keep the Java object in memory in sync with the saved database value.
            appointment.setStatus(status);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    // Reads every appointment and its client's name from the database.
    public List<Appointment> getAppointments() {
        // READ: joins appointments to users so each appointment can show the client's full name.
        String sql = """
                SELECT appointments.id, appointments.client_username, users.full_name,
                       appointments.doctor, appointments.appointment_date,
                       appointments.appointment_time, appointments.reason, appointments.status
                FROM appointments
                JOIN users ON users.username = appointments.client_username
                ORDER BY appointments.id
                """;

        ArrayList<Appointment> appointments = new ArrayList<>();

        try (Connection connection = connect();
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(sql)) {
            // Read every returned row and convert it into an Appointment object.
            while (results.next()) {
                appointments.add(readAppointment(results));
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }

        return appointments;
    }

    // Returns appointments matching an optional client username and status.
    public List<Appointment> filterAppointments(String clientUsername, Appointment.Status status) {
        ArrayList<Appointment> filteredAppointments = new ArrayList<>();

        // READ HELPER: starts from all appointments already loaded by getAppointments().
        for (Appointment appointment : getAppointments()) {
            // A null clientUsername means "do not filter by client".
            boolean clientMatches = clientUsername == null
                    || appointment.getClientUsername().equals(clientUsername);
            // A null status means "do not filter by appointment status".
            boolean statusMatches = status == null || appointment.getStatus() == status;

            if (clientMatches && statusMatches) {
                filteredAppointments.add(appointment);
            }
        }

        return filteredAppointments;
    }

    // Reads every client account from the database in alphabetical order.
    public List<User> getClientUsers() {
        // READ: selects only rows whose role is CLIENT, leaving admin accounts out of this list.
        String sql = """
                SELECT username, password, full_name, email, phone, role
                FROM users
                WHERE role = 'CLIENT'
                ORDER BY full_name
                """;

        ArrayList<User> clients = new ArrayList<>();

        try (Connection connection = connect();
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(sql)) {
            // Read every client row and convert it into a User object.
            while (results.next()) {
                clients.add(readUser(results));
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }

        return clients;
    }

    // Returns the number of registered client accounts.
    public int countClients() {
        // READ HELPER: gets the current client list and counts its items.
        return getClientUsers().size();
    }

    // Returns the number of saved appointments.
    public int countAppointments() {
        // READ HELPER: gets the current appointment list and counts its items.
        return getAppointments().size();
    }

    // Returns the number of appointments with the given status.
    public int countStatus(Appointment.Status status) {
        // READ HELPER: counts appointments after filtering by status.
        return filterAppointments(null, status).size();
    }

    // Returns the number of appointments with the given status for one client.
    public int countClientStatus(String username, Appointment.Status status) {
        // READ HELPER: counts appointments after filtering by both client and status.
        return filterAppointments(username, status).size();
    }

    // Updates a user's name, email, and phone number in the database.
    public void updateUser(User user) {
        // WRITE: updates editable profile fields while keeping the username unchanged.
        String sql = """
                UPDATE users
                SET full_name = ?, email = ?, phone = ?
                WHERE username = ?
                """;

        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            // Fill the new profile values and choose the row by username.
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getUsername());
            // executeUpdate is used for UPDATE because it changes the database.
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    // Converts the current database result row into a User object.
    private User readUser(ResultSet results) throws SQLException {
        // Pull each column from the current row and pass it into the User constructor.
        return new User(results.getString("username"), results.getString("password"),
                results.getString("full_name"), results.getString("email"),
                results.getString("phone"), User.Role.valueOf(results.getString("role")));
    }

    // Converts the current database result row into an Appointment object.
    private Appointment readAppointment(ResultSet results) throws SQLException {
        // Parse the date string back into a LocalDate because SQLite stores it as text.
        return new Appointment(results.getInt("id"), results.getString("client_username"),
                results.getString("full_name"), results.getString("doctor"),
                LocalDate.parse(results.getString("appointment_date")),
                results.getString("appointment_time"), results.getString("reason"),
                Appointment.Status.valueOf(results.getString("status")));
    }

    // Wraps an SQL error in an application-level exception with a clearer message.
    private IllegalStateException databaseError(SQLException exception) {
        // Keeping one error message makes database failures easier to recognize.
        return new IllegalStateException("Could not access the clinic database.", exception);
    }
}
