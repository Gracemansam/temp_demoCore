package com.lamicore.coredemo.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserDTO saveUser(UserDTO userDto) {
        User user = User.builder()
                    .firstName(userDto.getFirstName())
                    .lastName(userDto.getLastName())
                    .email(userDto.getEmail())
                    .phoneNumber(userDto.getPhoneNumber())
                    .password(userDto.getPassword())
                    .build();
         userRepository.save(user);
         return UserDTO.builder()
                    .id(user.getId())
                 .firstName(user.getFirstName())
                 .lastName(user.getLastName())
                 .email(user.getEmail())
                 .phoneNumber(user.getPhoneNumber())
                 .password(user.getPassword())
                 .build();
    }

    public UserDTO signIn(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .build();
    }

}
