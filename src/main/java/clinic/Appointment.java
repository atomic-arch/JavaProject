package clinic;

import java.time.LocalDate;

public class Appointment {
    // Enum limits appointment status to these three valid values.
    public enum Status {
        // Newly submitted appointment waiting for admin review.
        PENDING("Pending"),

        // Appointment accepted by an admin.
        APPROVED("Approved"),

        // Appointment rejected by an admin.
        REJECTED("Rejected");

        // Text shown to users instead of the all-caps enum constant.
        private final String displayName;

        // Stores the human-readable name shown for this appointment status.
        Status(String displayName) {
            this.displayName = displayName;
        }

        // Returns the human-readable status name used in the interface.
        public String getDisplayName() {
            return displayName;
        }
    }

    // Database ID. New unsaved appointments start at 0 until SQLite assigns an ID.
    private final int id;

    // Username links this appointment to a row in the users table.
    private final String clientUsername;

    // Client name is stored here for display in lists.
    private String clientName;

    // Doctor chosen for the appointment.
    private final String doctor;

    // Appointment calendar date.
    private final LocalDate date;

    // Appointment time as display text, such as "10:00 AM".
    private final String time;

    // Reason entered by the client.
    private final String reason;

    // Current admin approval status.
    private Status status;

    // Creates a new appointment that does not have a database ID yet.
    public Appointment(String clientUsername, String clientName, String doctor, LocalDate date,
            String time, String reason, Status status) {
        // Delegate to the full constructor and use 0 because SQLite will assign the real ID.
        this(0, clientUsername, clientName, doctor, date, time, reason, status);
    }

    // Creates an appointment with an ID, including appointments read from the database.
    public Appointment(int id, String clientUsername, String clientName, String doctor,
            LocalDate date, String time, String reason, Status status) {
        // Store every value passed in so the object represents one appointment record.
        this.id = id;
        this.clientUsername = clientUsername;
        this.clientName = clientName;
        this.doctor = doctor;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = status;
    }

    // Returns the appointment ID assigned by the database.
    public int getId() {
        return id;
    }

    // Returns the username of the client who requested the appointment.
    public String getClientUsername() {
        return clientUsername;
    }

    // Returns the full name of the client who requested the appointment.
    public String getClientName() {
        return clientName;
    }

    // Changes the client name stored in this appointment object.
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // Returns the doctor selected for the appointment.
    public String getDoctor() {
        return doctor;
    }

    // Returns the appointment date.
    public LocalDate getDate() {
        return date;
    }

    // Returns the appointment time.
    public String getTime() {
        return time;
    }

    // Returns the reason entered for the visit.
    public String getReason() {
        return reason;
    }

    // Returns the current approval status of the appointment.
    public Status getStatus() {
        return status;
    }

    // Changes the approval status stored in this appointment object.
    public void setStatus(Status status) {
        this.status = status;
    }

    // Returns the text displayed for this appointment inside a ListView.
    @Override
    public String toString() {
        // ListView uses this formatted line to show appointment details in one row.
        return "#" + id + " | " + clientName + " | " + doctor + " | " + date + " | " + time
                + " | " + reason + " | " + status.getDisplayName();
    }
}
