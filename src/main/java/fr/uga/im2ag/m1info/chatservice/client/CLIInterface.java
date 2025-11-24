package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.ui.CommandParser;

import java.util.Scanner;

public class CLIInterface {
    public CLIInterface(ClientAPI api) {
        // Appel de l'interface
        Scanner sc = new Scanner(System.in);

        // Command parser loop
        CommandParser parser = new CommandParser(api, sc);
        parser.run();
    }
}
