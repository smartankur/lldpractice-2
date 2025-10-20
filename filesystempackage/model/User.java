package filesystempackage.model;

import lombok.Data;

@Data
public class User {
    private String name;
    private Role role;

    public User(String name, Role role) {
        this.name = name;
        this.role = role != null ? role : Role.PUBLIC;
    }
}