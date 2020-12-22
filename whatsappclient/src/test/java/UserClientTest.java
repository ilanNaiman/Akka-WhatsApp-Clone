import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.javadsl.TestKit;
import akka.util.Timeout;
import application.UserClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;

public class UserClientTest {
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
     * Manager offline
     * Expected output:
     *
     * server is offline!
     */
    @Test
    public void testConnectionGetsServerOffline() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(7));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager offline
     * Expected output:
     *
     * server is offline!
     */
    @Test
    public void testDisconnectionGetsServerOffline() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(7));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Future<Object> disconnectionFutureResults = Patterns.ask(client1, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                    Await.result(disconnectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager Online
     * Expected output:
     *
     * tomer has connected successfully!
     * tomer is in use!
     * tomer has been disconnected successfully!
     */
    @Test
    public void testDoubleConnectionClient() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(3));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Future<Object> connectionFutureResults2 = Patterns.ask(client1, "/user connect tomer", timeout);
                Future<Object> disconnectionFutureResults = Patterns.ask(client1, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                    Await.result(connectionFutureResults2, timeout.duration());
                    Await.result(disconnectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager Online
     * Expected output:
     *
     * tomer has connected successfully!
     * tom has connected successfully!
     * [Wed Dec 11 23:14:10 IST 2019][user][tom] helloooooo helloooooo h
     * tom has been disconnected successfully!
     * tomer has been disconnected successfully!
     */
    @Test
    public void testSendTextToAnotherClient() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(10));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Future<Object> connectionFutureResults2 = Patterns.ask(client2, "/user connect tom", timeout);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Future<Object> sendTextFutureResults = Patterns.ask(client2, "/user text tomer helloooooo helloooooo h", timeout);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Future<Object> disconnectionFutureResults = Patterns.ask(client2, "/user disconnect", timeout);
                Future<Object> disconnectionFutureResults2 = Patterns.ask(client1, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                    Await.result(connectionFutureResults2, timeout.duration());
                    Await.result(sendTextFutureResults, timeout.duration());
                    Await.result(disconnectionFutureResults, timeout.duration());
                    Await.result(disconnectionFutureResults2, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager Online
     * Expected output:
     *
     * tomer has connected successfully!
     * tom has connected successfully!
     * tom has been disconnected successfully!
     * tomer has been disconnected successfully!
     */
    @Test
    public void testTwoDisconnectionClients() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(2));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Future<Object> connectionFutureResults2 = Patterns.ask(client2, "/user connect tom", timeout);
                Future<Object> disconnectionFutureResults = Patterns.ask(client1, "/user disconnect", timeout);
                Future<Object> disconnectionFutureResults2 = Patterns.ask(client2, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                    Await.result(connectionFutureResults2, timeout.duration());
                    Await.result(disconnectionFutureResults, timeout.duration());
                    Await.result(disconnectionFutureResults2, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager Online
     * Expected output:
     *
     * tomer has connected successfully!
     * tom has connected successfully!
     * tomer has been disconnected successfully!
     * tomer does not exist!
     * tom has been disconnected successfully!
     */
    @Test
    public void testSendMessageToDisconnectedUser() throws InterruptedException {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(10));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect tom", timeout);

                Thread.sleep(2000);

                Patterns.ask(client1, "/user disconnect", timeout);

                Thread.sleep(2000);

                Patterns.ask(client2, "/user text tomer helloooooo helloooooo h", timeout);

                Thread.sleep(2000);

                Patterns.ask(client2, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager Online
     * Expected output:
     *
     * bar has connected successfully!
     * foo has connected successfully!
     * tomer has connected successfully!
     * [Thu Dec 12 01:37:13 IST 2019][user][foo] hello tomer from foo
     * [Thu Dec 12 01:37:13 IST 2019][user][tomer] hello foo from tomer
     * [Thu Dec 12 01:37:13 IST 2019][user][bar] hello foo from bar
     * [Thu Dec 12 01:37:13 IST 2019][user][bar] hello tomer from bar
     * [Thu Dec 12 01:37:13 IST 2019][user][tomer] hello bar from tomer
     * bar has been disconnected successfully!
     * foo has been disconnected successfully!
     * tomer has been disconnected successfully!
     */
    @Test
    public void testUserChatP2P() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(15));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client3 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect foo", timeout);
                Patterns.ask(client3, "/user connect bar", timeout);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Patterns.ask(client2, "/user text tomer hello tomer from foo", timeout);
                Patterns.ask(client1, "/user text foo hello foo from tomer", timeout);
                Patterns.ask(client3, "/user text foo hello foo from bar", timeout);
                Patterns.ask(client3, "/user text tomer hello tomer from bar", timeout);
                Patterns.ask(client1, "/user text bar hello bar from tomer", timeout);

                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Patterns.ask(client1, "/user disconnect", timeout);
                Patterns.ask(client2, "/user disconnect", timeout);
                Patterns.ask(client3, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager Online
     * Expected output: the order of which the messages received from the different clients is not matter, it should check that
     *                  messages are not lost while waiting for ActorRef
     *
     * foo has connected successfully!
     * tomer has connected successfully!
     * [Thu Dec 12 16:09:11 IST 2019][user][foo] hello tomer from foo - 1
     * [Thu Dec 12 16:09:11 IST 2019][user][tomer] hello foo from tomer - 1
     * [Thu Dec 12 16:09:11 IST 2019][user][foo] hello tomer from foo - 2
     * [Thu Dec 12 16:09:11 IST 2019][user][tomer] hello foo from tomer - 2
     * [Thu Dec 12 16:09:11 IST 2019][user][foo] hello tomer from foo - 3
     * [Thu Dec 12 16:09:11 IST 2019][user][tomer] hello foo from tomer - 3
     * [Thu Dec 12 16:09:11 IST 2019][user][foo] hello tomer from foo - 4
     * [Thu Dec 12 16:09:11 IST 2019][user][tomer] hello foo from tomer - 4
     * tomer has been disconnected successfully!
     * foo has been disconnected successfully!
     */
    @Test
    public void testBurstMessagesP2P() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(10));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect foo", timeout);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Patterns.ask(client1, "/user text foo hello foo from tomer - 1", timeout);
                Patterns.ask(client1, "/user text foo hello foo from tomer - 2", timeout);
                Patterns.ask(client1, "/user text foo hello foo from tomer - 3", timeout);
                Patterns.ask(client1, "/user text foo hello foo from tomer - 4", timeout);

                Patterns.ask(client2, "/user text tomer hello tomer from foo - 1", timeout);
                Patterns.ask(client2, "/user text tomer hello tomer from foo - 2", timeout);
                Patterns.ask(client2, "/user text tomer hello tomer from foo - 3", timeout);
                Patterns.ask(client2, "/user text tomer hello tomer from foo - 4", timeout);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Patterns.ask(client1, "/user disconnect", timeout);
                Patterns.ask(client2, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Manager Online
     * Expected Output:
     *
     * tomer has connected successfully!
     * foo has connected successfully!
     * [Thu Dec 12 16:16:03 IST 2019][user][tomer] hello foo from tomer - 1
     * [Thu Dec 12 16:16:03 IST 2019][user][foo] hello tomer from foo - 1
     * [Thu Dec 12 16:16:03 IST 2019][user][tomer] File received: test_file.txt
     * [Thu Dec 12 16:16:03 IST 2019][user][foo] File received: test_file.txt
     * tomer has been disconnected successfully!
     * foo has been disconnected successfully!
     *
     */
    @Test
    public void testBinaryMessageP2P() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(10));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect foo", timeout);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Patterns.ask(client1, "/user text foo hello foo from tomer - 1", timeout);
                Patterns.ask(client2, "/user text tomer hello tomer from foo - 1", timeout);
                Patterns.ask(client1, "/user file foo src/test/java/input/test_file.txt", timeout);
                Patterns.ask(client2, "/user file tomer src/test/java/input/test_file.txt", timeout);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Patterns.ask(client1, "/user disconnect", timeout);
                Patterns.ask(client2, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }};
    }

    /**
     * Manager Online
     * Expected Output:
     *
     * tomer has connected successfully!
     * foo has connected successfully!
     * Cannot send file: not_exists.txt	ERROR: java.nio.file.NoSuchFileException: not_exists.txt
     * [Mon Dec 23 15:24:56 IST 2019][user][tomer] File received: test_file.txt
     * foo has been disconnected successfully!
     * tomer has been disconnected successfully!
     */
    @Test
    public void testBinaryMessageNotExists() throws InterruptedException {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(10));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client2, "/user connect foo", timeout);

                Thread.sleep(3000);

                Patterns.ask(client1, "/user file foo not_exists.txt", timeout);
                Patterns.ask(client1, "/user file tomer src/test/java/input/dir with space/test_file.txt", timeout);

                Thread.sleep(1000);

                Patterns.ask(client1, "/user disconnect", timeout);
                Patterns.ask(client2, "/user disconnect", timeout);

                try {
                    Await.result(connectionFutureResults, timeout.duration());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    /**
     * Test messages without connecting
     * TODO: breaking the manager
     * Expected Output:
     * User is not connected!
     */
    @Test
    public void testSendingMessagesWithoutConnecting() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(1));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user text foo hello foo from tomer - 1", timeout);
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
     * tomer has connected successfully!
     * tomer is in use!
     * tomer is in use!
     * tomer is in use!
     *
     */
    @Test
    public void testBurstConnecting() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(8));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client1, "/user connect tomer", timeout);

                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

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
     *
     * tom has connected successfully!
     * tom is in use!
     * tomer has connected successfully!
     * tomer is in use!
     * tom has been disconnected successfully!
     * tomer has been disconnected successfully!
     *
     */
    @Test
    public void testConnectWithAlreadyConnectedUser() {
        new TestKit(system) {
            {
                Timeout timeout = Timeout.create(Duration.ofSeconds(8));
                final ActorRef client1 = system.actorOf(Props.create(UserClient.class));
                final ActorRef client2 = system.actorOf(Props.create(UserClient.class));

                Future<Object> connectionFutureResults = Patterns.ask(client2, "/user connect tom", timeout);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                Patterns.ask(client1, "/user connect tom", timeout);
                Patterns.ask(client1, "/user connect tomer", timeout);
                Patterns.ask(client1, "/user connect tomer", timeout);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

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
}
