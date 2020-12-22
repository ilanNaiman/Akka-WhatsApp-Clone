package application.messages.User;

import application.messages.Message;

public class UserResponse implements Message {
    private final String answer;

    public UserResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}
