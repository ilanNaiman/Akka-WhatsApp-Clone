import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.javadsl.TestKit;
import akka.util.Timeout;
import application.UserClient;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.io.File;
import java.time.Duration;

public class GroupClientTest {
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

    /**
     * Manager online
     * Expected output:
     * tomer has connected successfully!
     * tom has connected successfully!
     * myGroup created successfully!
     * You have been invited to myGroup, Accept?
     * Welcome to myGroup!
     * [Fri Dec 13 10:18:17 IST 2019][myGroup][tom]hello my group
     * [Fri Dec 13 10:18:17 IST 2019][myGroup][tom]hello my group
     * [Fri Dec 13 10:18:18 IST 2019][myGroup][tomer] You have been muted for 10 seconds in myGroup by tomer
     * You have been unmuted! Muting time is up!
     * [Fri Dec 13 10:18:30 IST 2019][myGroup][tom]test unmute
     * [Fri Dec 13 10:18:30 IST 2019][myGroup][tom]test unmute
     * [Fri Dec 13 10:18:31 IST 2019][myGroup][tom]tom has left myGroup!
     * tomer has been disconnected successfully!
     * tom has been disconnected successfully!
     */
    @Test
    public void testMuteUser() throws InterruptedException {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(20));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect tom", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group create myGroup", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group user invite myGroup tom", timeout);

                Thread.sleep(1000);

                Patterns.ask(client2, "Yes", timeout);

                Thread.sleep(3000);

                Patterns.ask(client2, "/group send text myGroup hello my group", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group user mute myGroup tom 10", timeout);

                Thread.sleep(1000);

                Patterns.ask(client2, "/group send text myGroup test mute", timeout);

                Thread.sleep(3000);

                Patterns.ask(client2, "/group send text myGroup test mute", timeout);

                Thread.sleep(8000);

                Patterns.ask(client2, "/group send text myGroup test unmute", timeout);

                Thread.sleep(1000);

                Patterns.ask(client2, "/user disconnect", timeout);
                Patterns.ask(client1, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Expected Output:
     * tom has connected successfully!
     * tomer has connected successfully!
     * myGroup created successfully!
     * You have been invited to myGroup, Accept?
     * Welcome to myGroup!
     * [Fri Dec 13 12:55:52 IST 2019][myGroup][tom]hello my group
     * [Fri Dec 13 12:55:52 IST 2019][myGroup][tom]hello my group
     * tom is not muted!
     * [Fri Dec 13 12:55:54 IST 2019][myGroup][tomer] You have been muted for 7 seconds in myGroup by tomer
     * [Mon Dec 23 14:56:19 IST 2019][myGroup][tomer] You have been unmuted in myGroup by tomer!
     * [Fri Dec 13 12:55:59 IST 2019][myGroup][tom]test unmute
     * [Fri Dec 13 12:55:59 IST 2019][myGroup][tom]test unmute
     * tomer admin has closed myGroup!
     * tomer admin has closed myGroup!
     * tom has been disconnected successfully!
     * tomer has been disconnected successfully!
     */
    @Test
    public void testUnmutedUser() throws InterruptedException {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(20));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect tom", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group create myGroup", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group user invite myGroup tom", timeout);

                Thread.sleep(1000);

                Patterns.ask(client2, "Yes", timeout);

                Thread.sleep(3000);

                Patterns.ask(client2, "/group send text myGroup hello my group", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group user unmute myGroup tom", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group user mute myGroup tom 7", timeout);

                Thread.sleep(1000);

                Patterns.ask(client2, "/group send text myGroup test mute", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group user unmute myGroup tom", timeout);

                Thread.sleep(3000);

                Patterns.ask(client2, "/group send text myGroup test unmute", timeout);

                Thread.sleep(1000);

                Patterns.ask(client2, "/user disconnect", timeout);
                Patterns.ask(client1, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     *
     * tom has connected successfully!
     * tomer has connected successfully!
     * myGroup created successfully!
     * You have been invited to myGroup, Accept?
     * Welcome to myGroup!
     * [Mon Dec 23 17:28:10 IST 2019][tom][myGroup] File received: test_file.txt
     * [Mon Dec 23 17:28:10 IST 2019][tom][myGroup] File received: test_file.txt
     * tomer has been disconnected successfully!
     * tom has been disconnected successfully!
     */
    @Test
    public void testGroupSendFile() throws InterruptedException {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(12));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect tom", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group create myGroup", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/group user invite myGroup tom", timeout);

                Thread.sleep(1000);

                Patterns.ask(client2, "Yes", timeout);

                Thread.sleep(3000);

                Patterns.ask(client2, "/group send file myGroup src/test/java/input/test_file.txt", timeout);

                Thread.sleep(3000);

                Patterns.ask(client2, "/user disconnect", timeout);
                Patterns.ask(client1, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                    FileUtils.deleteQuietly(new File("src/Downloads/myGroup/test_file.txt"));
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }
}
