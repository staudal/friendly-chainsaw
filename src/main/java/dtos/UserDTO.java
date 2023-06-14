package dtos;

import entities.Role;
import entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDTO {
    private String user_name;
    private String user_pass;
    private String firstName;
    private String lastName;
    private List<String> roles = new ArrayList<>();
    private Long festival;

    public UserDTO(User user) {
        this.user_name = user.getUserName();
        this.user_pass = user.getUserPass();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        for (Role role : user.getRoleList()) {
            this.roles.add(role.getRoleName());
        }
        if (user.getFestival() != null) {
            this.festival = user.getFestival().getId();
        }
    }

    public static List<UserDTO> getUserDTOs(List<User> users){
        List<UserDTO> usersDTO = new ArrayList<>();
        users.forEach(user -> usersDTO.add(new UserDTO(user)));
        return usersDTO;
    }

    public UserDTO(String user_name, String firstName, String lastName) {
        this.user_name = user_name;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
