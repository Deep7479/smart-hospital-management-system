# Smart Hospital Management System

A hospital management system built in Java with a relational SQLite
database and a lightweight embedded web UI (no external web framework —
just the JDK's built-in `HttpServer`, styled with plain HTML/CSS).

## Features

- **Patient registration** — add and list patients (name, age, gender, phone, address).
- **Doctor directory** — list of doctors with specialization.
- **Appointment booking** — book a patient with a doctor for a date/time and a reason for visit.
- **Double-booking prevention** — the `appointments` table has a `UNIQUE(doctor_id, appt_date, appt_time)` constraint, so a doctor can never be booked for two appointments in the same slot. The app catches the constraint violation and shows a friendly error instead of crashing.
- Sample data (4 doctors, 3 patients) is seeded automatically on first run.

## Architecture

```
com.hospital
├── Main.java              # entry point — initializes DB, starts server
├── Database.java          # JDBC connection + schema/seed setup
├── model/                 # Patient, Doctor, Appointment POJOs
├── dao/                   # PatientDAO, DoctorDAO, AppointmentDAO (JDBC queries)
└── web/
    ├── Server.java        # com.sun.net.httpserver routes for each page
    └── Html.java          # shared page layout helper
```

Database schema: `src/main/resources/schema.sql`
Static CSS: `src/main/resources/static/style.css`

## Tech Stack

Java 17, JDBC, SQLite (via [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc)), HTML, CSS. Built with Maven.

## Setup & Run

Requires JDK 17+ and Maven.

```bash
mvn clean package
java -jar target/hospital-management-system.jar
```

Then open http://localhost:8080 in your browser. The SQLite database
file (`hospital.db`) is created automatically in the working directory
on first run.

## Notes

- No external DB server needed — SQLite runs embedded, in a single file.
- The web layer avoids Spring/servlet containers on purpose, to keep the
  project's dependency footprint minimal and the request-handling logic
  transparent (see `Server.java`).
