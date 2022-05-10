package com.example;

import akka.actor.typed.ActorSystem;

import java.io.IOException;
public class Blanco {
  public static void main(String[] args) {
    final ActorSystem<ActorMain.StartMessage> actorMain = ActorSystem.create(ActorMain.create(), "mainActor");

    actorMain.tell(new ActorMain.StartMessage());

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      actorMain.terminate();
    }
  }
}
