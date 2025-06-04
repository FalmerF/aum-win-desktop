package ru.ilug.aumwindesktop.event.ui;

import javafx.event.Event;
import javafx.event.EventType;
import lombok.Getter;
import ru.ilug.aumwindesktop.data.model.User;

@Getter
public class UserUpdateEvent extends Event {

    public static final EventType<UserUpdateEvent> EVENT_TYPE = new EventType<>(Event.ANY, "USER_UPDATE_EVENT");

    private final User user;

    public UserUpdateEvent(User user) {
        super(EVENT_TYPE);

        this.user = user;
    }
}
