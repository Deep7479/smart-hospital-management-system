package com.hospital.dao;

import com.hospital.Database;
import com.hospital.model.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    /** Thrown when the requested doctor/date/time slot is already booked. */
    public static class SlotTakenException extends Exception {
        public SlotTakenException(String message) { super(message); }
    }

    public List<Appointment> findAll() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, p.name AS patient_name, d.name AS doctor_name
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            ORDER BY a.appt_date, a.appt_time
        """;
        try (Statement stmt = Database.get().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Books a new appointment. The UNIQUE(doctor_id, appt_date, appt_time) constraint
     * in the schema prevents two appointments from double-booking the same doctor's
     * slot; this method turns that constraint violation into a friendly exception.
     */
    public int book(int patientId, int doctorId, String date, String time, String reason) throws SQLException, SlotTakenException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appt_date, appt_time, reason, status) VALUES (?, ?, ?, ?, ?, 'Scheduled')";
        try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setString(3, date);
            ps.setString(4, time);
            ps.setString(5, reason);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                throw new SlotTakenException("This doctor already has an appointment at " + date + " " + time + ".");
            }
            throw e;
        }
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setApptDate(rs.getString("appt_date"));
        a.setApptTime(rs.getString("appt_time"));
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorName(rs.getString("doctor_name"));
        return a;
    }
}
