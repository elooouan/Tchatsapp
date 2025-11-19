package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.common.PacketSender;
import fr.uga.im2ag.m1info.chatservice.server.TchatsAppServer;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

public class ErrorProcessor implements PacketProcessor {

    StrategyContext context;

    public ErrorProcessor(StrategyContext context) {
    }

    @Override
    public void process(Packet msg) {
        //if (context.getSender() == pas le serveur lui envoyer sinon print)
        System.out.println("Erreur dans le packet" + msg); // A changer en cas de debug
    }
}
