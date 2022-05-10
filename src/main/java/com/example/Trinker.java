package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Trinker extends AbstractBehavior<Trinker.Nachricht> {

    private final ActorRef<Kaffeekasse.Anfrage> kaffeekasse;
    private static ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer;

    public interface Nachricht {}
    public static final class Success implements Nachricht {
        ActorRef<Kaffeemaschine.Anfrage> maschine;
        public Success(ActorRef<Kaffeemaschine.Anfrage> maschine) {
            this.maschine = maschine;
        }
    }
    public static final class Fail implements Nachricht {}
    public static final class kaffee_holen implements Nachricht {}
    public static final class guthaben_aufladen implements Nachricht {}


    public static Behavior<Nachricht> create(ActorRef<Kaffeekasse.Anfrage> kaffeekasse, ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer) {
        return Behaviors.setup(context -> new Trinker(context, kaffeekasse, loadbalancer));
    }

    private Trinker(ActorContext<Nachricht> context, ActorRef<Kaffeekasse.Anfrage> kaffeekasse, ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer) {
        super(context);
        this.kaffeekasse = kaffeekasse;
        this.loadbalancer = loadbalancer;
    }

    @Override
    public Receive<Nachricht> createReceive() {
        return newReceiveBuilder()
                .onMessage(Success.class, this::onSuccess)
                .onMessage(Fail.class, this::onFail)
                .onMessage(kaffee_holen.class, this::onKaffeeHolen)
                .onMessage(guthaben_aufladen.class, this::onGuthabenAufladen)
                .build();
    }

    private Behavior<Nachricht> onSuccess(Success command) {
        if(command.maschine == null) {
            getContext().getLog().info("Guthaben erfolgreich aufgeladen");
        }
        else {
            getContext().getLog().info("Referenz Kaffeemaschine erhalten: {}", command.maschine.path());
            command.maschine.tell(new Kaffeemaschine.kaffee_bruehen());
        }

        /// TODO: Nächste zufällige Aktion (Kaffee holen oder Guthaben aufladen) vom Trinker ausführen mit math.random()

        return this;
    }

    private Behavior<Nachricht> onFail(Fail command) {
        getContext().getLog().info("onFail");
        return this;
    }

    private Behavior<Nachricht> onKaffeeHolen(kaffee_holen command) {
        getContext().getLog().info("onKaffeeHolen");
        loadbalancer.tell(new Loadbalancer.kaffee_holen(this.getContext().getSelf()));
        return this;
    }

    private Behavior<Nachricht> onGuthabenAufladen(guthaben_aufladen command) {
        getContext().getLog().info("guthaben_aufladen");
        kaffeekasse.tell(new Kaffeekasse.guthaben_aufladen(this.getContext().getSelf()));
        return this;
    }
}
