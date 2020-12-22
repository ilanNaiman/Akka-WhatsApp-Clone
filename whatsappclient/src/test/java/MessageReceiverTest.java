import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import application.actors.MessageReceiver;
import application.messages.BinaryMessage;
import application.messages.RemoveSession;
import application.messages.User.UserBinaryMessage;
import application.messages.User.UserDisconnectionRequest;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;

public class MessageReceiverTest {
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
    public void testMessageReceiverActorGetBinaryMessage() {
        new TestKit(system) {
            {
                final Props props = MessageReceiver.props(getRef(), getRef());
                final ActorRef subject = system.actorOf(props);
                Path mockFile = Paths.get("src/test/java/input/test_file.txt");
                Path expectedDownloadFileTo = Paths.get("src/Downloads/tomer/test_file.txt");

                try {
                    BinaryMessage mock = new UserBinaryMessage(new Date(), "tomer", getRef(),
                            mockFile.getFileName().toString(), Files.readAllBytes(mockFile), "");
                    subject.tell(mock, getRef());
                    expectNoMessage(Duration.ofSeconds(3));
                    Assert.assertFalse(Files.notExists(expectedDownloadFileTo));
                } catch (IOException e) {
                    Assert.fail(e.toString());
                } finally {
                    FileUtils.deleteQuietly(expectedDownloadFileTo.getParent().toFile());
                }
            }
        };
    }

    @Test
    public void testSendRemoveSessionUponUserDisconnection() {
        new TestKit(system) {
            {
                final Props props = MessageReceiver.props(getRef(), getRef());
                final ActorRef subject = system.actorOf(props);

                UserDisconnectionRequest mock = new UserDisconnectionRequest(getRef(), "");
                RemoveSession removeSession = new RemoveSession(getRef());
                subject.tell(mock, getRef());
                expectMsg(Duration.ofSeconds(5), removeSession);
            }
        };
    }
}
