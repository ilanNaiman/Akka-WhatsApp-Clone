package application;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import application.actors.Group;
import application.actors.ManagingServer;
import application.actors.UserConnection;
import application.messages.Group.*;
import application.messages.InformTarget;
import application.messages.Message;
import application.messages.Ack;
import application.messages.ErrorMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.Date;

public class GroupTest {
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
    public void testGroupsActorCreateGroupMessage() {
        new TestKit(system) {
            {
                final Props props = Props.create(ManagingServer.class);
                final ActorRef subject = system.actorOf(props);

                Message groupCreate = new GroupCreate("Ani Oto", "");
                Message mock = new Ack("Ani Oto created successfully!");

                subject.tell(groupCreate, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
            }
        };
    }


    @Test
    public void testGroupsActorCreateGroupMessageWithError() {
        new TestKit(system) {
            {
                final Props props = Props.create(ManagingServer.class);
                final ActorRef subject = system.actorOf(props);

                Message groupCreate = new GroupCreate("Ani Oto", "");
                Message groupCreate1 = new GroupCreate("Ani Oto", "");
                Message mock = new Ack("Ani Oto created successfully!");
                Message mock1 = new ErrorMessage("Ani Oto already exists!");

                subject.tell(groupCreate, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock);
                subject.tell(groupCreate1, getRef());
                expectMsgEquals(Duration.ofSeconds(1), mock1);
            }
        };
    }

    @Test
    public void testMuteUserNotMember() {
        new TestKit(system) {
            {
                final ActorRef userConnection = system.actorOf(Props.create(UserConnection.class));
                final ActorRef subject = system.actorOf(Group.props("myGroup",getRef(), "admin", userConnection));

                Message groupUserMute = new GroupUserMute("myGroup", "tomer",
                        "tomer", Duration.ofSeconds(2));

                subject.tell(groupUserMute, getRef());

                // Tomer is not a group member so expecting an error message
                expectMsgEquals(Duration.ofSeconds(2), new ErrorMessage());
            }
        };
    }

    @Test
    public void testUnmuteUserNotMember() {
        new TestKit(system) {
            {
                final ActorRef userConnection = system.actorOf(Props.create(UserConnection.class));
                final ActorRef subject = system.actorOf(Group.props("myGroup",getRef(), "admin", userConnection));

                Message groupUserUnmute = new GroupUserUnmute("myGroup", "tomer",
                        "tomer");

                subject.tell(groupUserUnmute, getRef());

                // Tomer is not a group member so expecting an error message
                expectMsgEquals(Duration.ofSeconds(2), new ErrorMessage());
            }
        };
    }

    @Test
    public void testMuteUserSenderNotAdmin() {
        new TestKit(system) {
            {
                final ActorRef userConnection = system.actorOf(Props.create(UserConnection.class));
                final ActorRef subject = system.actorOf(Group.props("myGroup", getRef(), "admin", userConnection));

                // Adding tomer and test to the group
                subject.tell(new GroupAddUser("myGroup", "admin", "tomer", getRef()), getRef());
                subject.tell(new GroupAddUser("myGroup", "admin", "test", getRef()), getRef());

                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));
                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));

                subject.tell(new GroupUserMute("myGroup", "test",
                        "tomer", Duration.ofSeconds(2)), getRef());

                // Tomer is not an admin so expecting an error message
                expectMsgEquals(Duration.ofSeconds(2), new ErrorMessage());
            }
        };
    }

    @Test
    public void testUnmuteUserSenderNotAdmin() {
        new TestKit(system) {
            {
                final ActorRef userConnection = system.actorOf(Props.create(UserConnection.class));
                final ActorRef subject = system.actorOf(Group.props("myGroup", getRef(), "admin", userConnection));

                // Adding tomer and test to the group
                subject.tell(new GroupAddUser("myGroup", "admin", "tomer", getRef()), getRef());
                subject.tell(new GroupAddUser("myGroup", "admin", "test", getRef()), getRef());

                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));
                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));

                subject.tell(new GroupUserUnmute("myGroup", "tomer",
                        "test"), getRef());

                // Tomer is not an admin so expecting an error message
                expectMsgEquals(Duration.ofSeconds(2), new ErrorMessage());
            }
        };
    }

    @Test
    public void testMuteUserSender() {
        new TestKit(system) {
            {
                final ActorRef userConnection = system.actorOf(Props.create(UserConnection.class));
                final ActorRef subject = system.actorOf(Group.props("myGroup", getRef(), "admin", userConnection));

                Message groupUserMute = new GroupUserMute("myGroup", "tomer",
                        "tomer", Duration.ofSeconds(2));

                // Adding tomer to the group
                subject.tell(new GroupAddUser("myGroup", "admin", "tomer", getRef()), getRef());
                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));

                // Promoting tomer to CoAdmin
                subject.tell(new GroupCoAdminAdd("myGroup", "admin", "tomer"), getRef());
                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));

                // Tomer is a member and CoAdmin so he can mute other users
                subject.tell(groupUserMute, getRef());
                expectMsgEquals(Duration.ofSeconds(2), groupUserMute);
            }
        };
    }

    @Test
    public void testUnmuteUserSender() {
        new TestKit(system) {
            {
                final ActorRef userConnection = system.actorOf(Props.create(UserConnection.class));
                final ActorRef subject = system.actorOf(Group.props("myGroup", getRef(), "admin", userConnection));

                Message groupUserUnmute = new GroupUserUnmute("myGroup", "tomer",
                        "tomer");

                // Adding tomer to the group
                subject.tell(new GroupAddUser("myGroup", "admin", "tomer", getRef()), getRef());
                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));

                // Promoting tomer to CoAdmin
                subject.tell(new GroupCoAdminAdd("myGroup", "admin", "tomer"), getRef());
                expectMsgEquals(Duration.ofSeconds(2), new InformTarget(getRef(), ""));

                // Tomer is a member and CoAdmin so he can mute other users
                subject.tell(groupUserUnmute, getRef());
                expectMsgEquals(Duration.ofSeconds(2), groupUserUnmute);
            }
        };
    }
}
