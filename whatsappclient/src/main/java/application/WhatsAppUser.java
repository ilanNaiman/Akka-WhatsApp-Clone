package application;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ActorSystem;
import application.messages.StartListener;


public class WhatsAppUser {
    public static void main(String[] args) {

        // Create ActorSystem and top level supervisor
        ActorSystem system = ActorSystem.create("whatsApp-client-system");

        //creating system application.actors
        ActorRef manager = system.actorOf(Props.create(UserClient.class), "UserManger");

        manager.tell(new StartListener(), ActorRef.noSender());
    }
}
