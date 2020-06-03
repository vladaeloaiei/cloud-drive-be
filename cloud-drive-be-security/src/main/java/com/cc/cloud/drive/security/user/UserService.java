package com.cc.cloud.drive.security.user;

import com.cc.cloud.drive.api.dto.UserDto;
import com.cc.cloud.drive.aws.s3.SNSService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    private SNSService snsService;

    @Autowired
    public UserService(UserRepository userRepository,
                       ModelMapper modelMapper,
                       SNSService snsService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.snsService = snsService;
    }

    @PostConstruct
    @Transactional
    public void generateSNSTopicsForUsers() {
        //TODO This init is req for users from data.sql
        List<String> usernames = Arrays.asList("user1", "user2");

        for (String username : usernames) {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("No user found"));

            user.setSnsTopicArn(snsService.createTopicForUser(user.getUsername(), user.getEmail()));
            userRepository.save(user);
        }
    }

    public Optional<UserDto> getById(Long id) {
        Optional<User> user = userRepository.findById(id);
        Optional<UserDto> userDto = user.map(u -> modelMapper.map(u, UserDto.class));

        userDto.ifPresent(u -> u.setPassword(null)); //Do not share password
        return userDto;
    }

    public Optional<UserDto> getByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        Optional<UserDto> userDto = user.map(u -> modelMapper.map(u, UserDto.class));

        userDto.ifPresent(u -> u.setPassword(null)); //Do not share password
        return userDto;
    }

    public Optional<UserDto> getSensitiveByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        return user.map(u -> modelMapper.map(u, UserDto.class));
    }

    public boolean login(String username, String password) {
        Optional<User> user = userRepository.findByUsernameAndPassword(username, password);

        return user.isPresent();
    }

    public UserDto save(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);

        user.setSnsTopicArn(snsService.createTopicForUser(user.getUsername(), user.getEmail()));
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }
}
