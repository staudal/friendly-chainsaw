package dtos;

import entities.Role;
import entities.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class UserDTO {
    private String user_name;
    private String user_pass;
    private String firstName;
    private String lastName;
    private List<String> roles = new ArrayList<>();

    public UserDTO(User user) {
        this.user_name = user.getUserName();
        this.user_pass = user.getUserPass();
        for (Role role : user.getRoleList()) {
            this.roles.add(role.getRoleName());
        }
    }

    public static List<UserDTO> getUsersDTO(List<User> users){
        List<UserDTO> usersDTO = new ArrayList<>();
        users.forEach(user -> usersDTO.add(new UserDTO(user)));
        return usersDTO;
    }
}
