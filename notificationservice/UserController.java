package notificationservice;

import java.util.HashMap;
import java.util.Map;

public class UserController {
    private final Map<String, User> users = new HashMap<>();
    private final NotificationService notificationService;

    public UserController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void createUser(String userId, String name, String email) {
        User user = new User(userId, name, email);
        users.put(userId, user);
    }

    public void addPreferences(User user, NotificationPreferences preferences) {
        user.setPreferences(preferences);
        notificationService.updateUserPreferences(user);
    }

    public void removePreferences(User user, NotificationType type) {
        user.getPreferences().getTypePreferences().put(type, false);
        notificationService.updateUserPreferences(user);
    }

    public User getUser(String userId) {
        return users.get(userId);
    }
}
