package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Kaffeekasse extends AbstractBehavior<Kaffeekasse.Anfrage> {

  private int guthaben_trinker1;
  private int guthaben_trinker2;
  private int guthaben_trinker3;
  private int guthaben_trinker4;


  public interface Anfrage {}
  public static final class guthaben_abfragen implements Anfrage{
    public ActorRef<Trinker.Nachricht> sender;
    public ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer;
    public guthaben_abfragen(ActorRef<Trinker.Nachricht> sender, ActorRef<Loadbalancer.AnfrageLoadbalancer> loadbalancer) {
      this.sender = sender;
      this.loadbalancer = loadbalancer;
    }
  }

  public static final class guthaben_aufladen implements Anfrage{
    public final ActorRef<Trinker.Nachricht> sender;
    public guthaben_aufladen(ActorRef<Trinker.Nachricht> sender) {
      this.sender = sender;
    }
  }

  public static Behavior<Anfrage> create(int startGuthaben) {
    return Behaviors.setup(context -> new Kaffeekasse(context, startGuthaben));
  }

  private Kaffeekasse(ActorContext<Anfrage> context, int startGuthaben) {
    super(context);
    /// TODO: Mappen der Trinkerreferenz zum Guthaben des Trinkers. :)
    this.guthaben_trinker1 = startGuthaben;
    this.guthaben_trinker2 = startGuthaben;
    this.guthaben_trinker3 = startGuthaben;
    this.guthaben_trinker4 = startGuthaben;
  }

  @Override
  public Receive<Anfrage> createReceive() {
    return newReceiveBuilder()
            .onMessage(guthaben_abfragen.class, this::onGuthaben_abfragen)
            .onMessage(guthaben_aufladen.class, this::onGuthaben_aufladen)
            .build();
  }

  private Behavior<Anfrage> onGuthaben_abfragen(guthaben_abfragen command) {
    getContext().getLog().info("onGuthaben_abfragen von {}!", command.sender.path());
    getContext().getLog().info("Guthabenstand: {}", guthaben_trinker1);
    if(guthaben_trinker1 > 0) {
      getContext().getLog().info("Guthaben > 0");
      command.loadbalancer.tell(new Loadbalancer.Success(command.sender));
    }
    else {
      getContext().getLog().info("Guthaben < 0!");
      command.loadbalancer.tell(new Loadbalancer.Fail(command.sender));
    }
    return this;
  }

  private Behavior<Anfrage> onGuthaben_aufladen(guthaben_aufladen command) {
    getContext().getLog().info("onGuthaben_aufladen von {}!", command.sender.path());
    guthaben_trinker1 += 1;
    getContext().getLog().info("Guthabenstand: {}", guthaben_trinker1);
    command.sender.tell(new Trinker.Success(null));
    return this;
  }
}
