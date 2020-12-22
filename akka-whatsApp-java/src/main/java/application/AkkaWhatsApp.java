package application;

import akka.actor.Props;
import akka.actor.ActorSystem;
import application.actors.ManagingServer;


public class AkkaWhatsApp {
    public static void main(String[] args) {
        //creating the system
        ActorSystem system = ActorSystem.create("whatsApp-chat-system");
        //creating system actors
        system.actorOf(Props.create(ManagingServer.class), "ManagingServer");
    }
}
