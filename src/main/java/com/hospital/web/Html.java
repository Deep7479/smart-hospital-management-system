package com.hospital.web;

/** Tiny helper for wrapping page content in the common layout. */
public final class Html {

    private Html() {}

    public static String page(String title, String activeNav, String body) {
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>%s | Smart Hospital Management System</title>
              <link rel="stylesheet" href="/static/style.css">
            </head>
            <body>
              <nav class="navbar">
                <div class="brand">🏥 Smart Hospital Management</div>
                <div class="nav-links">
                  <a href="/patients" class="%s">Patients</a>
                  <a href="/doctors" class="%s">Doctors</a>
                  <a href="/appointments" class="%s">Appointments</a>
                </div>
              </nav>
              <main class="container">
                %s
              </main>
              <footer>Java &middot; SQLite (JDBC) &middot; Embedded HTTP Server</footer>
            </body>
            </html>
        """.formatted(
                escape(title),
                "nav-link".concat(activeNav.equals("patients") ? " active" : ""),
                "nav-link".concat(activeNav.equals("doctors") ? " active" : ""),
                "nav-link".concat(activeNav.equals("appointments") ? " active" : ""),
                body
        );
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
