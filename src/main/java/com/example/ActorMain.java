package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class ActorMain extends AbstractBehavior<ActorMain.StartMessage> {

    public static class StartMessage {}

    ActorRef<Trinker.Nachricht> trinker1;
    ActorRef<Kaffeekasse.Anfrage> kaffeekasse;
    ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer;
    ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine1;
    ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine2;

    public static Behavior<StartMessage> create() {
        return Behaviors.setup(ActorMain::new);
    }

    private ActorMain(ActorContext<StartMessage> context) {
        super(context);
        kaffeekasse = context.spawn(Kaffeekasse.create(1), "Kaffeekasse");
        kaffeemaschine1 = context.spawn(Kaffeemaschine.create(), "Kaffemaschine1");
        kaffeemaschine2 = context.spawn(Kaffeemaschine.create(), "Kaffemaschine2");
        loadbalancer = context.spawn(Loadbalancer.create(kaffeekasse, kaffeemaschine1, kaffeemaschine2), "Loadbalancer");
        trinker1 = context.spawn(Trinker.create(kaffeekasse, loadbalancer), "Kaffetrinker1");
    }

    @Override
    public Receive<StartMessage> createReceive() {
        return newReceiveBuilder().onMessage(StartMessage.class, this::onStartMessage).build();
    }

    private Behavior<StartMessage> onStartMessage(StartMessage command) {
        //trinker1.tell(new Trinker.Nachricht());
        //kaffeekasse.tell(new Kaffeekasse.guthaben_abfragen(trinker1));
        //kaffeekasse.tell(new Kaffeekasse.guthaben_aufladen(trinker1));
        //trinker1.tell(new Trinker.guthaben_aufladen());
        trinker1.tell(new Trinker.kaffee_holen());
        return this;
    }
}
