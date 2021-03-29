package com.example.gps_wifi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SocketHandler implements Runnable {

    int port;

    DatagramSocket socket;
    InetAddress inetAddress;
    DatagramPacket packet;

    ArrayList<String> msgs;
    int size;

    public SocketHandler(String ip, int port) {
        try {
            socket = new DatagramSocket(port);
            inetAddress = InetAddress.getByName(ip);
            this.port = port;
        } catch (SocketException | UnknownHostException e) {

            e.printStackTrace();
        }

        msgs = new ArrayList<String>();
        size = 0;
    }

    public void addItemToSend(String _add) {
        msgs.add(_add);
        size++;
    }

    @Override
    public void run() {

        while(true) {
            if(size > 0) {
                byte[] toSend = msgs.get(0).getBytes();
                msgs.remove(0);
                size--;
                try {
                    packet = new DatagramPacket(toSend, toSend.length, inetAddress, port);
                    socket.send(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
