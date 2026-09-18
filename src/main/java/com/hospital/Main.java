package com.hospital;

import com.hospital.web.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        Database.initialize();
        int port = 8080;
        new Server().start(port);
    }
}
