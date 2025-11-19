package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

public class ErrorProcessor implements PacketProcessor {

    StrategyContext context;
    String error = null;

    public ErrorProcessor(StrategyContext context) {
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public void process(Packet msg) {
        System.out.println("Erreur dans le packet" + error); // A changer en cas de debug
    }
}
