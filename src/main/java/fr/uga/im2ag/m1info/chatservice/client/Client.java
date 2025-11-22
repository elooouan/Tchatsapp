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
import fr.uga.im2ag.m1info.chatservice.common.PacketType;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;

import fr.uga.im2ag.m1info.chatservice.ui.ConsoleClientListener;
import fr.uga.im2ag.m1info.chatservice.ui.CommandParser;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * A basic client for Tchatsapp.
 * It allows to connect to a server, send and receive packets.
 */
public class Client {

    private int clientId;
    private Socket cnx;
    private PacketProcessor processor;
    private static ClientState clientState;

    public Client() {
        this(0);
    }
    public Client(int clientId) {
        this.clientId=clientId;
    }

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
            dos.writeInt(clientId);
            dos.flush();
            // read the empty packet and use the recipient id
            clientId=Packet.readFrom(dis).to();

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
        return clientId;
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

    //TODO make loadData work later
    private void loadData(int id){
        clientState = new ClientState(id);
        return;
//        File stateFile = new File("clientData.ser");
//        if(!stateFile.exists()){
//
////            Packet idCreation = Packet.createPacket(0,0, PacketType.CREATE_USER,"");
////            sendPacket(idCreation);
//
////            String msg = packet.getPayloadAsString();
////            String[] parts = msg.split(" ");
////            int id = Integer.parseInt(parts[2]);
//
//            clientState = new ClientState(id);
//            return;
//        }
//
//        try {
//            ObjectInputStream ois;
//
//            FileInputStream dataFile = new FileInputStream("clientData.ser");
//            ois = new ObjectInputStream(dataFile);
//            clientState = (ClientState) ois.readObject();
//            ois.close();
//            dataFile.close();
//
//        } catch (IOException | ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }

    }

    public static ClientState getClientState(){
        return clientState;
    }

    /** A bsic client in command line **/
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Your id ? (0 to create a new account)");
        int clientId = sc.nextInt();
        sc.nextLine(); // consume end of line
    
        // Low-level TCP client
        Client c = new Client(clientId);

        //TODO make loaddata work and move it
        c.loadData(clientId);
    
        // UI listener for incoming events
        ConsoleClientListener ui = new ConsoleClientListener();
    
        // High-level API (outgoing + incoming decoding)
        ClientAPI api = new ClientAPI(
                c::sendPacket,   // PacketSender -> use Client.sendPacket
                clientId,
                ui               // IncomingPacketProcessor.Listener
        );
    
        // Tell Client to forward incoming packets to ClientAPI
        c.setPacketProcessor(api::handleIncoming);
    
        // Connect
        if (c.connect("localhost", 1666)) {
            // Server may assign a new id
            clientId = c.getClientId();
            api.setClientId(clientId);
    
            System.out.println("You are now connected with id: " + clientId);
            System.out.println("Type /help for the list of commands.");
    
            // Command parser loop
            CommandParser parser = new CommandParser(api, sc);
            parser.run();
    
            c.disconnect();
            System.exit(0);
        } else {
            System.err.println("Connection failed.");
        }
    }
    

}
