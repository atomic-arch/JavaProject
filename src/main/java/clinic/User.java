package clinic;

public class User {
    // Enum limits users to the two roles required by the project.
    public enum Role {
        // Admin users can manage clients, appointments, and reports.
        ADMIN,

        // Client users can request appointments and edit their profile.
        CLIENT
    }

    // Username is final because it is the database primary key.
    private final String username;

    // Password is final because this project does not include password changing.
    private final String password;

    // Role is final so a client cannot accidentally become an admin inside the app.
    private final Role role;

    // Profile fields can change through the client profile screen.
    private String fullName;
    private String email;
    private String phone;

    // Creates a user with login details, contact information, and an access role.
    public User(String username, String password, String fullName, String email, String phone,
            Role role) {
        // Store constructor arguments into object fields.
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Returns the username used to identify and sign in the user.
    public String getUsername() {
        return username;
    }

    // Returns the password used to sign in the user.
    public String getPassword() {
        return password;
    }

    // Returns the user's full name.
    public String getFullName() {
        return fullName;
    }

    // Changes the user's full name.
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Returns the user's email address.
    public String getEmail() {
        return email;
    }

    // Changes the user's email address.
    public void setEmail(String email) {
        this.email = email;
    }

    // Returns the user's phone number.
    public String getPhone() {
        return phone;
    }

    // Changes the user's phone number.
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Returns whether the user is an administrator or a client.
    public Role getRole() {
        return role;
    }

    // Returns the text displayed for this user inside a ListView.
    @Override
    public String toString() {
        // Admin client lists use this single-line summary for each account.
        return fullName + " | Username: " + username + " | Email: " + email + " | Phone: " + phone;
    }
}
