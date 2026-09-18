-- Smart Hospital Management System — Database Schema (SQLite)

CREATE TABLE IF NOT EXISTS doctors (
    doctor_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL,
    specialization TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS patients (
    patient_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL,
    age           INTEGER NOT NULL,
    gender        TEXT NOT NULL,
    phone         TEXT,
    address       TEXT
);

CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id     INTEGER NOT NULL,
    doctor_id      INTEGER NOT NULL,
    appt_date      TEXT NOT NULL,
    appt_time      TEXT NOT NULL,
    reason         TEXT,
    status         TEXT NOT NULL DEFAULT 'Scheduled',
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    UNIQUE (doctor_id, appt_date, appt_time)
);
