//import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
//import akka.actor.testkit.typed.javadsl.TestProbe;
//import akka.actor.typed.ActorRef;
//import application.actors.UserConnection;
//import application.messages.*;
//import application.messages.ErrorMessage;
//import org.junit.ClassRule;
//import org.junit.Test;
//
//public class UserConnectionTest {
//
//    @ClassRule
//    public static final TestKitJunitResource testKit = new TestKitJunitResource();
//
//    @Test
//    public void testUserConnectionActorNewUser() {
//        TestProbe<Message> testProbe = testKit.createTestProbe();
//        ActorRef<Message> underTest = testKit.spawn(UserConnection.create(), "user-connection-actor1");
//        underTest.tell(new UserConnectionRequest (testProbe.getRef(), "Tomer"));
//        testProbe.expectMessage(new Ack());
//    }
//
//    @Test
//    public void testUserConnectionActorExistsUser() {
//        TestProbe<Message> testProbe = testKit.createTestProbe();
//        TestProbe<Message> testProbe2 = testKit.createTestProbe();
//        ActorRef<Message> underTest = testKit.spawn(UserConnection.create(), "user-connection-actor2");
//
//        // The first probe succeed registering the user while the other one fails
//        underTest.tell(new UserConnectionRequest (testProbe.getRef(), "Tomer"));
//        testProbe.expectMessage(new Ack());
//
//        underTest.tell(new UserConnectionRequest (testProbe2.getRef(), "Tomer"));
//        testProbe2.expectMessage(new ErrorMessage());
//    }
//
//    @Test
//    public void testUserConnectionActorDisconnectUser() {
//        TestProbe<Message> testProbe = testKit.createTestProbe();
//        ActorRef<Message> underTest = testKit.spawn(UserConnection.create(), "user-connection-actor3");
//        underTest.tell(new UserConnectionRequest (testProbe.getRef(), "Tomer"));
//        testProbe.expectMessage(new Ack());
//
//        underTest.tell(new UserDisconnectionRequest (testProbe.getRef(), "Tomer"));
//        testProbe.expectMessage(new Ack());
//    }
//
//    @Test
//    public void testUserConnectionActorDisconnectUnexistsUser() {
//        TestProbe<Message> testProbe = testKit.createTestProbe();
//        ActorRef<Message> underTest = testKit.spawn(UserConnection.create(), "user-connection-actor4");
//
//        underTest.tell(new UserDisconnectionRequest (testProbe.getRef(), "Tomer"));
//        testProbe.expectMessage(new ErrorMessage());
//    }
//}
