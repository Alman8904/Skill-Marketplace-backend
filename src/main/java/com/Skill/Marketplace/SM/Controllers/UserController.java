package com.Skill.Marketplace.SM.Controllers;
import com.Skill.Marketplace.SM.DTO.userDTO.*;
import com.Skill.Marketplace.SM.Entities.UserModel;
import com.Skill.Marketplace.SM.Services.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Data
@RestController
@RequestMapping("/public/user")
public class UserController {



    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser (@RequestBody CreateUserDTO request){

        UserModel user = userService.createNewUser(request);
        return ResponseEntity.ok(
                new ResponseToUser(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserType()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO updates){
        UserModel updatedUser = userService.updateUser(id, updates);
        return ResponseEntity.ok(
                new ResponseToUser(
                        updatedUser.getId(),
                        updatedUser.getUsername(),
                        updatedUser.getFirstName(),
                        updatedUser.getLastName(),
                        updatedUser.getUserType()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> searchUser(@PathVariable Long id){
        UserModel user = userService.getUserById(id);
        return ResponseEntity.ok(
                new ResponseToUser(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserType()
                )
        );
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> searchUserByUsername(@PathVariable String username){
        UserModel user = userService.getUserByUsername(username);
        return ResponseEntity.ok(
                new ResponseToUser(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserType()
                )
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAllUsers(){
        List<UserModel> users = userService.getAllUsers();
        List<ResponseToUser> response =  users.stream().map(
                user -> new ResponseToUser(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserType()
                )
        ).toList();

        return ResponseEntity.ok(response);
    }
}
