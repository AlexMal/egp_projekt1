package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Loadbalancer extends AbstractBehavior<Loadbalancer.AnfrageLoadbalancer> {

    private final ActorRef<Kaffeekasse.Anfrage> kaffeekasse;
    private final ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine1;
    private final ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine2;

    public interface AnfrageLoadbalancer {}
    public static final class kaffee_holen implements AnfrageLoadbalancer {
        public ActorRef<Trinker.Nachricht> sender;
        public kaffee_holen(ActorRef<Trinker.Nachricht> sender) {
            this.sender = sender;
        }
    }

    public static final class Success implements AnfrageLoadbalancer {
        public ActorRef<Trinker.Nachricht> sender;
        public Success(ActorRef<Trinker.Nachricht> sender) {
            this.sender = sender;
        }
    }

    public static final class Fail implements AnfrageLoadbalancer {
        public ActorRef<Trinker.Nachricht> sender;
        public Fail(ActorRef<Trinker.Nachricht> sender) {
            this.sender = sender;
        }
    }

    public static final class AntwortKaffeemaschinen implements AnfrageLoadbalancer {
        public ActorRef<Trinker.Nachricht> sender;
        public ActorRef<Kaffeemaschine.Anfrage> maschine;
        public int vorrat_kaffee;
        public AntwortKaffeemaschinen(ActorRef<Trinker.Nachricht> sender, ActorRef<Kaffeemaschine.Anfrage> maschine, int vorrat_kaffee) {
            this.sender = sender;
            this.maschine = maschine;
            this.vorrat_kaffee = vorrat_kaffee;
        }
    }

    public static Behavior<AnfrageLoadbalancer> create(ActorRef<Kaffeekasse.Anfrage> kaffeekasse, ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine1, ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine2) {
        return Behaviors.setup(context -> new Loadbalancer(context, kaffeekasse, kaffeemaschine1, kaffeemaschine2));
    }

    private Loadbalancer(ActorContext<AnfrageLoadbalancer> context, ActorRef<Kaffeekasse.Anfrage> kaffeekasse, ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine1, ActorRef<Kaffeemaschine.Anfrage> kaffeemaschine2) {
        super(context);
        this.kaffeekasse = kaffeekasse;
        this.kaffeemaschine1 = kaffeemaschine1;
        this.kaffeemaschine2 = kaffeemaschine2;

    }

    @Override
    public Receive<AnfrageLoadbalancer> createReceive() {
        return newReceiveBuilder()
                .onMessage(kaffee_holen.class, this::onKaffeeHolen)
                .onMessage(Success.class, this::onSuccess)
                .onMessage(Fail.class, this::onFail)
                .onMessage(AntwortKaffeemaschinen.class, this::onAntwortKaffeemaschinen)
                .build();
    }

    private Behavior<AnfrageLoadbalancer> onKaffeeHolen(kaffee_holen command) {
        getContext().getLog().info("loadbalancer onKaffeeHolen kontaktiere Kaffeekassee");
        kaffeekasse.tell(new Kaffeekasse.guthaben_abfragen(command.sender, this.getContext().getSelf()));
        return this;
    }

    /// Guthaben in Kaffeekasse reicht aus.
    /// Hole Vorrat der Kaffemaschinen.
    private Behavior<AnfrageLoadbalancer> onSuccess(Success command) {
        getContext().getLog().info("Guthaben reicht aus.");
        kaffeemaschine1.tell(new Kaffeemaschine.vorrat_abfragen(command.sender, this.getContext().getSelf()));
        kaffeemaschine2.tell(new Kaffeemaschine.vorrat_abfragen(command.sender, this.getContext().getSelf()));
        return this;
    }

    private Behavior<AnfrageLoadbalancer> onFail(Fail command) {
        getContext().getLog().info("Fail: Guthaben reicht nicht aus.");
        command.sender.tell(new Trinker.Fail());
        return this;
    }

    private Behavior<AnfrageLoadbalancer> onAntwortKaffeemaschinen(AntwortKaffeemaschinen command) {
        getContext().getLog().info("Antwort von {} Vorrat bei: {}", command.maschine, command.vorrat_kaffee);

        /// TODO: Hier muss ein Counter rein. Sobald beide Kaffeemaschinen geantwortet haben wird Referenz an Trinker gesendet.


        ActorRef<Kaffeemaschine.Anfrage> maschine_to_use = command.maschine;
        command.sender.tell(new Trinker.Success(maschine_to_use));
        return this;
    }
}
