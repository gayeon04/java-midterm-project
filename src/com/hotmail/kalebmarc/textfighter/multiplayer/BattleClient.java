package com.hotmail.kalebmarc.textfighter.multiplayer;

import java.io.*;
import java.net.*;

public class BattleClient {
    private static final int PORT = 9999;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect(String host) throws IOException {
        socket = new Socket(host, PORT);
        out    = new PrintWriter(socket.getOutputStream(), true);
        in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void close() {
        try { if (in     != null) in.close();     } catch (IOException ignored) {}
        try { if (out    != null) out.close();    } catch (Exception  ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}
