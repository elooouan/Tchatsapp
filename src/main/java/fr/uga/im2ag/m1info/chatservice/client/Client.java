/*
 * Copyright (c) 2025.  Jerome David. Univ. Grenoble Alpes.
 * This file is part of TchatsApp.
 *
 * TchatsApp is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * TchatsApp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TchatsApp. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;

import fr.uga.im2ag.m1info.chatservice.common.PacketType;
import fr.uga.im2ag.m1info.chatservice.ui.ConsoleClientListener;
import fr.uga.im2ag.m1info.chatservice.ui.CommandParser;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * A basic client for Tchatsapp.
 * It allows to connect to a server, send and receive packets.
 */
public class Client {

    private Socket cnx;
    private PacketProcessor processor;
    private static ClientState clientState;

    public Client() {}

    /**
     * Attemps to connect to a given server.
     * @param host
     * @param port
     * @return false if there is an existing connection or if the connection fails.
     */
    public boolean connect(String host, int port) {
        if (cnx!=null && cnx.isConnected()) return false;
        try {
            cnx = new Socket(host,port);
            DataOutputStream dos = new DataOutputStream(cnx.getOutputStream());
            DataInputStream dis = new DataInputStream(cnx.getInputStream());

            dos.writeInt(clientState.getClientId());
            dos.flush();
            // read the empty packet and use the recipient id
            clientState.setClientId(Packet.readFrom(dis).to());

            // reception thread
            new Thread(() ->{
                try {
                    while (cnx!=null && !cnx.isInputShutdown()) {
                        Packet m = Packet.readFrom(dis);
                        processReceivedPacket(m);
                    }
                } catch (IOException e) {
                    if (cnx==null || !cnx.isConnected()) return;
                    e.printStackTrace();
                }

            }).start();
            return cnx.isConnected();
        } catch (IOException e) {
            if (!cnx.isClosed()) e.printStackTrace();
           return false;
        }
    }

    public int getClientId() {
        return clientState.getClientId();
    }

    /**
     * Set the packet processor to be called when packet are received by the client
     * @param p
     */
    public void setPacketProcessor(fr.uga.im2ag.m1info.chatservice.common.PacketProcessor p ) {
        processor=p;
    }

    private void processReceivedPacket(Packet m) {
        if (processor!=null) processor.process(m);
    }

    public boolean isConnected() {
        return cnx!=null && cnx.isConnected();
    }

    public void disconnect() {
        try {
            if (cnx != null) cnx.close();
            cnx = null;
        } catch (IOException e) {/* ignored */}
    }

    public boolean sendPacket(Packet pkt) {
        try {
            DataOutputStream dos = new DataOutputStream(cnx.getOutputStream());

            // Serialize the full packet exactly as PacketBuilder created it:
            // [length][from][to][type][payload...]
            ByteBuffer buf = pkt.asByteBuffer();
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            dos.write(data);

            dos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadData(String file){
        File stateFile = new File(file);
        if(!stateFile.exists()){
            clientState = new ClientState();
            return;
        }

        try {
            ObjectInputStream ois;

            FileInputStream dataFile = new FileInputStream(file);
            ois = new ObjectInputStream(dataFile);
            clientState = (ClientState) ois.readObject();
            ois.close();
            dataFile.close();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadData(){
        File stateFile = new File("clientState.ser");
        if(!stateFile.exists()){
            clientState = new ClientState();
            return;
        }

        try {
            ObjectInputStream ois;

            FileInputStream dataFile = new FileInputStream("clientState.ser");
            ois = new ObjectInputStream(dataFile);
            clientState = (ClientState) ois.readObject();
            ois.close();
            dataFile.close();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClientState getClientState(){
        return clientState;
    }

    private void saveData(String file) {
        try {
            ObjectOutputStream oos;

            FileOutputStream dataFile = new FileOutputStream(file);
            oos = new ObjectOutputStream(dataFile);
            oos.writeObject(clientState);
            oos.close();
            dataFile.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveData(){
        try{
            ObjectOutputStream oos;

            FileOutputStream dataFile = new FileOutputStream("clientState.ser");
            oos = new ObjectOutputStream(dataFile);
            oos.writeObject(clientState);
            oos.close();
            dataFile.close();

        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /** A bsic client in command line **/
    public static void main(String[] args) throws IOException {

        // Low-level TCP client
        Client c = new Client();

        if(args.length == 0){
            c.loadData();
        }else{
            c.loadData(args[0]);
        }

        // UI listener for incoming events
        ConsoleClientListener ui = new ConsoleClientListener();

        // High-level API (outgoing + incoming decoding)
        ClientAPI api = new ClientAPI(
                c::sendPacket,   // PacketSender -> use Client.sendPacket
                Client.getClientState().getClientId(),
                ui               // IncomingPacketProcessor.Listener
        );

        ui.setApi(api);
    
        // Tell Client to forward incoming packets to ClientAPI
        c.setPacketProcessor(api::handleIncoming);
    
        // Connect
        if (c.connect("localhost", 1666)) {
            // Server may assign a new id
            api.setClientId(Client.getClientState().getClientId());
    
            System.out.println("You are now connected with id: " + Client.getClientState().getClientId());
            System.out.println("Type /help for the list of commands.");

            // Lancement de l'interface
            new CLIInterface(api);
    
            c.disconnect();
            if(args.length == 0){
                c.saveData();
            }else{
                c.saveData(args[0]);
            }
            System.exit(0);
        } else {
            System.err.println("Connection failed.");
        }
    }
}
