import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import application.actors.MessageSender;
import application.messages.Ack;
import application.messages.AddSession;
import application.messages.ErrorMessage;
import application.messages.Group.GroupUserMute;
import application.messages.Group.GroupUserUnmute;
import application.messages.Group.Mute;
import application.messages.Group.Unmute;
import application.messages.User.ReceivedUserRef;
import application.messages.User.UserDisconnectionRequest;
import application.messages.User.UserTextualMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.Date;

public class MessageSenderTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testUserTextualMessageNotInCache() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);

                UserTextualMessage mock = new UserTextualMessage(new Date(), "tomer", "", getRef(), "hi!");

                subject.tell(mock, getRef());

                // Excepting no reply as the user is not in the cache
                expectNoMessage(Duration.ofSeconds(1));
            }
        };
    }

    @Test
    public void testUserTextualMessage() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);

                // Adding a new session in the sender
                subject.tell(new AddSession(getRef(), "tomer"), getRef());
                UserTextualMessage mock = new UserTextualMessage(new Date(), "tomer", "", getRef(), "hi!");

                subject.tell(mock, getRef());

                // Excepting the message to be delivered to us
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testUserTextualMessageReceivedUserRef() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);

                UserTextualMessage mock = new UserTextualMessage(new Date(), "tomer", "", getRef(), "hi!");

                // Sending a user message who is not in the cache
                subject.tell(mock, getRef());

                // Adding the user to the cache
                subject.tell(new ReceivedUserRef("tomer", getRef(), getRef()), getRef());

                // Excepting the message to be delivered to us
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testUserTextualMessageReceivedUserRef2() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);

                UserTextualMessage mock = new UserTextualMessage(new Date(), "tomer", "", getRef(),
                        "hi!");
                UserTextualMessage mock2 = new UserTextualMessage(new Date(), "tomer", "", getRef(),
                        "Why are you not responding?");

                // Sending 2 user messages to a user who is not in the cache
                subject.tell(mock, getRef());
                subject.tell(mock2, getRef());

                // Adding the user to the cache
                subject.tell(new ReceivedUserRef("tomer", getRef(), getRef()), getRef());

                // Excepting the messages to be delivered to us
                expectMsgEquals(Duration.ofSeconds(1), mock);
                expectMsgEquals(Duration.ofSeconds(1), mock2);
            }
        };
    }

    @Test
    public void testMute() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);

                Mute mute = new Mute("myGroup", "tomer", Duration.ofSeconds(1), getRef());
                subject.tell(mute, getRef());

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Expecting the ack message after the mute time is up
                expectMsg(new Ack());
            }
        };
    }

    @Test
    public void testUnmuteToUnmutedUser() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);

                Unmute unmute = new Unmute("myGroup", "tomer", getRef(), "");
                subject.tell(unmute, getRef());
                expectMsgEquals(new ErrorMessage());
            }
        };
    }

    @Test
    public void testUnmuteMutedUser() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);
                GroupUserMute groupUserMute = new GroupUserMute("myGroup", "tomer", "", Duration.ofSeconds(10));
                GroupUserUnmute groupUserUnmute = new GroupUserUnmute("myGroup", "", "tomer");

                subject.tell(groupUserMute, getRef());
                subject.tell(groupUserUnmute, getRef());

                expectNoMessage();
            }
        };
    }

    @Test
    public void testMessageSenderUserDisconnectionWhileSeverUnavailiable() {
        new TestKit(system) {
            {
                final Props props = Props.create(MessageSender.class);
                final ActorRef subject = system.actorOf(props);

                UserDisconnectionRequest userDisconnectionRequest = new UserDisconnectionRequest(getRef(), "");
                subject.tell(userDisconnectionRequest, getRef());
                expectNoMessage();
            }
        };
    }
}
