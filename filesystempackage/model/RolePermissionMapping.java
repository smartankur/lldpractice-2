package filesystempackage.model;

import lombok.Getter;

@Getter
public enum RolePermissionMapping {
    DELETE_FILE(Role.ADMIN),
    CREATE_FILE(Role.USER),
    MODIFY_FILE(Role.USER),
    READ_FILE(Role.PUBLIC);

    private Role role;

    RolePermissionMapping(Role role) {
        this.role = role;
    }

    public boolean hasPermission(Role userRole) {
        // Admin has all permissions
        if (userRole == Role.ADMIN) return true;
        // User has USER and PUBLIC permissions
        if (userRole == Role.USER && (this.role == Role.USER || this.role == Role.PUBLIC)) return true;
        // Public has only PUBLIC permissions
        return userRole == Role.PUBLIC && this.role == Role.PUBLIC;
    }
}
