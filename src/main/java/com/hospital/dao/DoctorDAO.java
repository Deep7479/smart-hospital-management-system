package com.hospital.dao;

import com.hospital.Database;
import com.hospital.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public List<Doctor> findAll() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY doctor_id";
        try (Statement stmt = Database.get().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("specialization")
                ));
            }
        }
        return doctors;
    }

    public Doctor findById(int id) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(rs.getInt("doctor_id"), rs.getString("name"), rs.getString("specialization"));
                }
            }
        }
        return null;
    }
}
