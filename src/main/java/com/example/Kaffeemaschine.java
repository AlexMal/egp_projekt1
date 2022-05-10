package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Kaffeemaschine extends AbstractBehavior<Kaffeemaschine.Anfrage> {

    private int vorrat_kaffee = 10;

    public interface Anfrage {}
    public static final class vorrat_abfragen implements Anfrage{

        public ActorRef<Trinker.Nachricht> sender;
        public ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer;

        public vorrat_abfragen(ActorRef<Trinker.Nachricht> sender, ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer) {
            this.sender = sender;
            this.loadbalancer = loadbalancer;
        }


    }

    public static final class kaffee_bruehen implements Anfrage{
        public kaffee_bruehen() {}
    }

    public static Behavior<Anfrage> create() {
        return Behaviors.setup(context -> new Kaffeemaschine(context));
    }

    private Kaffeemaschine(ActorContext<Anfrage> context) {
        super(context);
    }

    @Override
    public Receive<Anfrage> createReceive() {
        return newReceiveBuilder()
                .onMessage(vorrat_abfragen.class, this::onVorratAbfragen)
                .onMessage(kaffee_bruehen.class, this::onKaffeeBruehen)
                .build();
    }

    private Behavior<Anfrage> onVorratAbfragen(vorrat_abfragen command) {
        getContext().getLog().info("Kaffeevorrat bei {}!", vorrat_kaffee);
        command.loadbalancer.tell(new Loadbalancer.AntwortKaffeemaschinen(command.sender, this.getContext().getSelf() ,vorrat_kaffee));
        return this;
    }

    private Behavior<Anfrage> onKaffeeBruehen(kaffee_bruehen command) {
        getContext().getLog().info("Brrrrrrrrrrrrruehe Kaffee");
        vorrat_kaffee -= 1;
        return this;
    }

}
