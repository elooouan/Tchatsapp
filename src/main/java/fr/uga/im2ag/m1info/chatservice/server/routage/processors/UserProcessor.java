package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

// Modifications locales (modifier le pseudo, ajouter un contact)
public class UserProcessor implements PacketProcessor {

    StrategyContext strategy;

    public UserProcessor(StrategyContext strategy) {
        this.strategy = strategy;
    }

    @Override
    public void process(Packet msg) {

    }
}
