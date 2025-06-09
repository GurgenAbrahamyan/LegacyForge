package com.gamb1t.server;

import com.badlogic.gdx.ApplicationListener;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Networking.Network;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class ServerCycle implements ApplicationListener {
    private String serverIp;
    private Server server;

    @Override
    public void create() {
        server = new Server(131072, 131072);
        Network.register(server.getKryo());

        RoomManager rm = new RoomManager(server);
        server.addListener(new ServerListener(rm));

        try {
            serverIp = null;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        serverIp = addr.getHostAddress();
                        break;
                    }
                }
                if (serverIp != null) break;
            }
            if (serverIp == null) {
                serverIp = InetAddress.getLocalHost().getHostAddress();
            }
            System.out.println("Server Local IP: " + serverIp);
        } catch (Exception e) {
            e.printStackTrace();
            serverIp = "127.0.0.1";
        }

        sendIPToFirebase(serverIp);

        try {
            server.bind(Network.TCP_PORT, Network.UDP_PORT);
            server.start();
            System.out.println("KryoNet server started on TCP: " + Network.TCP_PORT + ", UDP: " + Network.UDP_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to bind server", e);
        }
    }

    private void sendIPToFirebase(String ip) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("serverIp");
            ref.setValue(ip, (error, ref1) -> {
                if (error != null) {
                    System.err.println("Failed to set server IP: " + error.getMessage());
                } else {
                    System.out.println("Successfully sent IP to Firebase: " + ip);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception while sending IP to Firebase: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (server != null) {
            server.stop();
            server.close();
        }
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void render() {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
}
