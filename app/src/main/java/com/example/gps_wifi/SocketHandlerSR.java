package com.example.gps_wifi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SocketHandlerSR implements Runnable {

    int port;

    DatagramSocket socket;
    InetAddress inetAddress;
    DatagramPacket packet;
    DatagramPacket packet_r;

    ArrayList<String> msgs;
    int size;
    String rcv;

    public SocketHandlerSR(String ip, int port) {
        try {
            socket = new DatagramSocket(port);
            inetAddress = InetAddress.getByName(ip);
            this.port = port;
        } catch (SocketException | UnknownHostException e) {

            e.printStackTrace();
        }

        msgs = new ArrayList<String>();
        size = 0;
        rcv = "";
    }

    public void addItemToSend(String _add) {
        msgs.add(_add);
        size++;
    }

    public String receiveData() {
        return rcv;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {

        boolean run = true;
        while(run) {
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

                //
                byte[] receive = new byte[1024];
                packet_r = new DatagramPacket(receive, receive.length);
                try {
                    socket.receive(packet_r);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                rcv = new String(packet_r.getData(), packet_r.getOffset(), packet_r.getLength());

                socket.close();
                run = false;

            }
        }

    }
}
