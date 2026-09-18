package com.hospital.model;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private String apptDate;   // YYYY-MM-DD
    private String apptTime;   // HH:MM
    private String reason;
    private String status;

    // Joined display fields (not stored on this table)
    private String patientName;
    private String doctorName;

    public Appointment() {}

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getApptDate() { return apptDate; }
    public void setApptDate(String apptDate) { this.apptDate = apptDate; }
    public String getApptTime() { return apptTime; }
    public void setApptTime(String apptTime) { this.apptTime = apptTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}
