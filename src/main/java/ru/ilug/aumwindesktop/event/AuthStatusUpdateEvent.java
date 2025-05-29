package ru.ilug.aumwindesktop.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.ilug.aumwindesktop.data.model.User;

@Getter
public class AuthStatusUpdateEvent extends ApplicationEvent {

    private final User user;
    private final String token;
    private final boolean authorized;

    public AuthStatusUpdateEvent(Object source, User user, String token, boolean authorized) {
        super(source);

        this.user = user;
        this.token = token;
        this.authorized = authorized;
    }
}
