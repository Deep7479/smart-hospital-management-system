package com.hospital.web;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Minimal embedded web server (no external framework) exposing the hospital's HTML UI. */
public class Server {

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", ex -> redirect(ex, "/patients"));
        server.createContext("/static/", this::handleStatic);
        server.createContext("/patients", this::handlePatients);
        server.createContext("/doctors", this::handleDoctors);
        server.createContext("/appointments", this::handleAppointments);

        server.setExecutor(null);
        server.start();
        System.out.println("Smart Hospital Management System running at http://localhost:" + port);
    }

    // ---------------- Patients ----------------

    private void handlePatients(HttpExchange ex) throws IOException {
        try {
            if ("POST".equals(ex.getRequestMethod()) && ex.getRequestURI().getPath().equals("/patients/add")) {
                Map<String, String> form = parseForm(ex);
                Patient p = new Patient();
                p.setName(form.get("name"));
                p.setAge(Integer.parseInt(form.getOrDefault("age", "0")));
                p.setGender(form.get("gender"));
                p.setPhone(form.get("phone"));
                p.setAddress(form.get("address"));
                patientDAO.insert(p);
                redirect(ex, "/patients");
                return;
            }

            List<Patient> patients = patientDAO.findAll();
            StringBuilder rows = new StringBuilder();
            for (Patient p : patients) {
                rows.append("""
                    <tr>
                      <td>%d</td><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td>
                    </tr>
                """.formatted(p.getPatientId(), Html.escape(p.getName()), p.getAge(),
                        Html.escape(p.getGender()), Html.escape(p.getPhone()), Html.escape(p.getAddress())));
            }

            String body = """
                <h1>Patients (%d)</h1>
                <div class="card">
                  <h3>Register New Patient</h3>
                  <form method="post" action="/patients/add" class="form-grid">
                    <input name="name" placeholder="Full name" required>
                    <input name="age" type="number" placeholder="Age" required>
                    <select name="gender" required>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                    <input name="phone" placeholder="Phone">
                    <input name="address" placeholder="Address">
                    <button type="submit">Add Patient</button>
                  </form>
                </div>
                <table class="data-table">
                  <thead><tr><th>ID</th><th>Name</th><th>Age</th><th>Gender</th><th>Phone</th><th>Address</th></tr></thead>
                  <tbody>%s</tbody>
                </table>
            """.formatted(patients.size(), rows);

            respond(ex, Html.page("Patients", "patients", body));
        } catch (Exception e) {
            respondError(ex, e);
        }
    }

    // ---------------- Doctors ----------------

    private void handleDoctors(HttpExchange ex) throws IOException {
        try {
            List<Doctor> doctors = doctorDAO.findAll();
            StringBuilder rows = new StringBuilder();
            for (Doctor d : doctors) {
                rows.append("""
                    <tr><td>%d</td><td>%s</td><td>%s</td></tr>
                """.formatted(d.getDoctorId(), Html.escape(d.getName()), Html.escape(d.getSpecialization())));
            }
            String body = """
                <h1>Doctors (%d)</h1>
                <table class="data-table">
                  <thead><tr><th>ID</th><th>Name</th><th>Specialization</th></tr></thead>
                  <tbody>%s</tbody>
                </table>
            """.formatted(doctors.size(), rows);
            respond(ex, Html.page("Doctors", "doctors", body));
        } catch (Exception e) {
            respondError(ex, e);
        }
    }

    // ---------------- Appointments ----------------

    private void handleAppointments(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();

            if ("POST".equals(ex.getRequestMethod()) && path.equals("/appointments/book")) {
                Map<String, String> form = parseForm(ex);
                String error = null;
                try {
                    appointmentDAO.book(
                            Integer.parseInt(form.get("patient_id")),
                            Integer.parseInt(form.get("doctor_id")),
                            form.get("date"),
                            form.get("time"),
                            form.get("reason")
                    );
                } catch (AppointmentDAO.SlotTakenException e) {
                    error = e.getMessage();
                }
                if (error != null) {
                    respond(ex, Html.page("Appointments", "appointments", bookingForm(error)));
                } else {
                    redirect(ex, "/appointments");
                }
                return;
            }

            List<Appointment> appts = appointmentDAO.findAll();
            StringBuilder rows = new StringBuilder();
            for (Appointment a : appts) {
                rows.append("""
                    <tr>
                      <td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td>
                    </tr>
                """.formatted(a.getAppointmentId(), Html.escape(a.getPatientName()), Html.escape(a.getDoctorName()),
                        a.getApptDate(), a.getApptTime(), Html.escape(a.getReason()), a.getStatus()));
            }

            String body = """
                <h1>Appointments (%d)</h1>
                %s
                <table class="data-table">
                  <thead><tr><th>ID</th><th>Patient</th><th>Doctor</th><th>Date</th><th>Time</th><th>Reason</th><th>Status</th></tr></thead>
                  <tbody>%s</tbody>
                </table>
            """.formatted(appts.size(), bookingForm(null), rows);

            respond(ex, Html.page("Appointments", "appointments", body));
        } catch (Exception e) {
            respondError(ex, e);
        }
    }

    private String bookingForm(String error) {
        String errorHtml = error != null
                ? "<div class=\"flash\">" + Html.escape(error) + "</div>"
                : "";
        StringBuilder patientOptions = new StringBuilder();
        StringBuilder doctorOptions = new StringBuilder();
        try {
            for (Patient p : patientDAO.findAll()) {
                patientOptions.append("<option value=\"%d\">%s</option>".formatted(p.getPatientId(), Html.escape(p.getName())));
            }
            for (Doctor d : doctorDAO.findAll()) {
                doctorOptions.append("<option value=\"%d\">%s (%s)</option>"
                        .formatted(d.getDoctorId(), Html.escape(d.getName()), Html.escape(d.getSpecialization())));
            }
        } catch (Exception ignored) {
        }

        return """
            <div class="card">
              <h3>Book Appointment</h3>
              %s
              <form method="post" action="/appointments/book" class="form-grid">
                <select name="patient_id" required>%s</select>
                <select name="doctor_id" required>%s</select>
                <input name="date" type="date" required>
                <input name="time" type="time" required>
                <input name="reason" placeholder="Reason for visit">
                <button type="submit">Book Slot</button>
              </form>
              <p class="hint">A doctor can only hold one appointment per date/time — the database enforces this with a UNIQUE constraint.</p>
            </div>
        """.formatted(errorHtml, patientOptions, doctorOptions);
    }

    // ---------------- Static files ----------------

    private void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath().substring("/static/".length());
        try (InputStream in = Server.class.getResourceAsStream("/static/" + path)) {
            if (in == null) {
                ex.sendResponseHeaders(404, -1);
                return;
            }
            byte[] bytes = in.readAllBytes();
            ex.getResponseHeaders().add("Content-Type", path.endsWith(".css") ? "text/css" : "application/octet-stream");
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.getResponseBody().close();
        }
    }

    // ---------------- Helpers ----------------

    private void respond(HttpExchange ex, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private void respondError(HttpExchange ex, Exception e) throws IOException {
        String html = Html.page("Error", "", "<h1>Something went wrong</h1><pre>" + Html.escape(e.toString()) + "</pre>");
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(500, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().add("Location", location);
        ex.sendResponseHeaders(302, -1);
    }

    private Map<String, String> parseForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> result = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isBlank()) continue;
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }
}
