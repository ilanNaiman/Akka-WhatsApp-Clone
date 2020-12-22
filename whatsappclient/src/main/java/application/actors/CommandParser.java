package application.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import application.messages.ErrorMessage;
import application.messages.User.*;
import application.messages.ClientKeystrokes;
import application.messages.Message;
import application.messages.Group.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser extends AbstractActor {

    private String userName;

    private CommandParser(){
        this.userName = "";
    }

    public static Props props() {
        return Props.create(CommandParser.class, CommandParser::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClientKeystrokes.class, this::handleUserInput)
                .match(AcceptedConnection.class, this::handleAcceptedConnection)
                .build();
    }

        private void handleAcceptedConnection(AcceptedConnection acceptedConnection) {
            userName = acceptedConnection.getUserName();
        }

    private Message handleBinaryMessage(ClientKeystrokes cks, Matcher matcher, BiFunction<byte[], String, Message> function) {
        String filePath = matcher.group(2);
        Path file = Paths.get(filePath);
        Message message;

        try {
            byte[] fileBytes = Files.readAllBytes(file);
            message = function.apply(fileBytes, file.getFileName().toString());
        } catch (IOException e) {
            message = new ErrorMessage("Cannot send file: " + filePath + "\tERROR: " + e.toString());
            cks.getReceiver().tell(message, ActorRef.noSender());
        }

        return message;
    }

    private void handleUserInput(ClientKeystrokes cks) {

        final Map<Pattern, Function<Matcher, Message>> messagePatternToSupplier = new HashMap<Pattern, Function<Matcher, Message>>() {{
            put(Pattern.compile("^/user text ([^\\s]+) (.*)$"), (Matcher matcher) ->
                    new UserTextualMessage(new Date(), matcher.group(1), userName, cks.getReceiver(), matcher.group(2)));

            put(Pattern.compile("^/user file ([^\\s]+) (.*)$"), (Matcher matcher) -> handleBinaryMessage(cks, matcher,
                    (byte[] fileBytes, String fileName) ->
                        new UserBinaryMessage(new Date(), matcher.group(1), cks.getReceiver(), fileName, fileBytes, userName)
            ));

            put(Pattern.compile("^/user connect ([^\\s]+)$"), (Matcher matcher) ->
                    new UserConnectionRequest(cks.getReceiver(), cks.getMessageSender(), matcher.group(1)));

            put(Pattern.compile("^/user disconnect$"), (Matcher matcher) ->
                    new UserDisconnectionRequest(cks.getReceiver(), userName));

            put(Pattern.compile("^/group create ([^\\s]+)$"), (Matcher matcher) ->
                    new GroupCreate(matcher.group(1), userName));

            put(Pattern.compile("^/group leave ([^\\s]+)$"), (Matcher matcher) ->
                    new GroupLeave(matcher.group(1), userName));

            put(Pattern.compile("^/group send text ([^\\s]+) (.*)$"), (Matcher matcher) ->
                    new GroupTextualMessage(new Date(), matcher.group(1), userName, matcher.group(2)));

            put(Pattern.compile("^/group send file ([^\\s]+) (.*)$"), (Matcher matcher) -> handleBinaryMessage(cks, matcher,
                    (byte[] fileBytes, String fileName) ->
                        new GroupBinaryMessage(new Date(), matcher.group(1), fileName, userName, fileBytes)
            ));

            put(Pattern.compile("^/group user invite ([^\\s]+) ([^\\s]+)$"), (Matcher matcher) ->
                    new GroupUserInvite(matcher.group(1), userName, matcher.group(2), cks.getReceiver()));

            put(Pattern.compile("^/group user remove ([^\\s]+) ([^\\s]+)$"), (Matcher matcher) ->
                    new GroupUserRemove(matcher.group(1), userName, matcher.group(2)));

            put(Pattern.compile("^/group user mute ([^\\s]+) ([^\\s]+) (\\d+)$"), (Matcher matcher) ->
                    new GroupUserMute(matcher.group(1), matcher.group(2), userName,
                            Duration.ofSeconds(Integer.parseInt(matcher.group(3)))));

            put(Pattern.compile("^/group user unmute ([^\\s]+) ([^\\s]+)$"), (Matcher matcher) ->
                    new GroupUserUnmute(matcher.group(1), userName, matcher.group(2)));

            put(Pattern.compile("^/group coadmin add ([^\\s]+) ([^\\s]+)$"), (Matcher matcher) ->
                    new GroupCoAdminAdd(matcher.group(1), userName, matcher.group(2)));

            put(Pattern.compile("^/group coadmin remove ([^\\s]+) ([^\\s]+)$"), (Matcher matcher) ->
                    new GroupCoAdminRemove(matcher.group(1), userName, matcher.group(2)));

            put(Pattern.compile("Yes"), (Matcher matcher) -> {
                     cks.getReceiver().tell(new UserResponse("Yes"), getSelf());
                     return new UserResponse("Yes"); });


            put(Pattern.compile("No"), (Matcher matcher) -> {
                    cks.getReceiver().tell(new UserResponse("No"), getSelf());
                    return new UserResponse("No"); });

        }};
        Matcher matcher;

        for (Pattern pattern : messagePatternToSupplier.keySet()) {
            matcher = pattern.matcher(cks.getClientKeyStroke());

            if (matcher.find()) {
                cks.getMessageSender().tell(messagePatternToSupplier.get(pattern).apply(matcher), cks.getReceiver());
                break;
            }
        }
    }
}
