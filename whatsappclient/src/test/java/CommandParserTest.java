import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.testkit.javadsl.TestKit;
import application.actors.CommandParser;
import application.messages.ClientKeystrokes;
import application.messages.ErrorMessage;
import application.messages.Message;
import application.messages.User.*;
import application.messages.Group.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.Date;

public class CommandParserTest {
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
    public void testCommandParserActorUserTextualMessage() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/user text tomer hi!", getRef(),  getRef());
                UserTextualMessage mock = new UserTextualMessage(new Date(), "tomer", "", getRef(), "hi!");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorUserBinaryMessage() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/user file tomer src/test/java/input/test_file.txt", getRef(), getRef());
                UserBinaryMessage mock = new UserBinaryMessage(new Date(), "tomer", getRef(), "test_file.txt", new byte[]{}, "");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorUserConnect() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/user connect tomer", getRef(), getRef());
                UserConnectionRequest mock = new UserConnectionRequest(getRef(), getRef(), "tomer");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorUserDisconnect() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/user disconnect", getRef(), getRef());
                UserDisconnectionRequest mock = new UserDisconnectionRequest(getRef(), "");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupCreate() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group create my_group", getRef(), getRef());
                GroupCreate mock = new GroupCreate("my_group", "");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupLeave() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group leave my_group", getRef(), getRef());
                GroupLeave mock = new GroupLeave("my_group", "");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupTextualMessage() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group send text my_group Hello!", getRef(), getRef());
                GroupTextualMessage mock = new GroupTextualMessage(new Date(),"my_group", "","Hello!");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupBinaryMessage() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group send file my_group src/test/java/input/test_file.txt", getRef(), getRef());
                GroupBinaryMessage mock = new GroupBinaryMessage(new Date(),"my_group",
                        "test_file.txt", "", new byte[]{});

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupBinaryMessageFileNotExists() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group send file my_group input/test_file.txt", getRef(), getRef());
                Message mock = new ErrorMessage();

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupUserInvite() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group user invite my_group tomer", getRef(), getRef());
                GroupUserInvite mock = new GroupUserInvite("my_group", "anonymous","tomer", getRef());

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupUserRemove() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group user remove my_group tom", getRef(), getRef());
                GroupUserRemove mock = new GroupUserRemove("my_group", "", "tom");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupUserMute() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group user mute my_group tomer 30", getRef(), getRef());
                GroupUserMute mock = new GroupUserMute("my_group", "tomer", "tomer", Duration.ofSeconds(30));

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupUserUnmute() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group user unmute my_group tomer", getRef(), getRef());
                GroupUserUnmute mock = new GroupUserUnmute("my_group", "tomer", "tomer");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupUserCoAdminAdd() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                AcceptedConnection cksConnect = new AcceptedConnection("tomer");
                ClientKeystrokes cks = new ClientKeystrokes("/group coadmin add my_group tomer", getRef(), getRef());
                GroupCoAdminAdd mock = new GroupCoAdminAdd("my_group", "tomer", "tomer");

                subject.tell(cksConnect, getRef());
                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }

    @Test
    public void testCommandParserActorGroupUserCoAdminRemove() {
        new TestKit(system) {
            {
                final Props props = Props.create(CommandParser.class);
                final ActorRef subject = system.actorOf(props);

                ClientKeystrokes cks = new ClientKeystrokes("/group coadmin remove my_group tomer", getRef(), getRef());
                GroupCoAdminRemove mock = new GroupCoAdminRemove("my_group", "", "tomer");

                subject.tell(cks, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }
}
