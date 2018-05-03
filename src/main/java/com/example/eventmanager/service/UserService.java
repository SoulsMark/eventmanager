package com.example.eventmanager.service;

import com.example.eventmanager.dao.UserRepository;
import com.example.eventmanager.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers(){
        //TODO Return list, not iterator
        Iterator<User> iter = userRepository.findAll().iterator();
        List<User> copy = new ArrayList<>();
        while (iter.hasNext())
            copy.add(iter.next());
        System.out.println(copy);
        return copy;

    }

    public User getUser(Long id){
        return userRepository.findOne(id);
    }

    public void updateUser(User user){
        userRepository.update(user);
    }

    public void deleteUser(User user){
        userRepository.delete(user);
    }
    
    public void changePass(User user){
    	userRepository.changePass(user);
    }

    public User findUser(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean isEmailExists(String email) {
        return userRepository.isEmailExists(email);
    }

    public boolean isUsernameExists(String username) {
        return userRepository.isUsernameExists(username);
    }

    public List<User> searchByLogin(String login){
        return userRepository.searchByLogin(login);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    public User getCurrentUser(){
        return findUser(getCurrentUsername());
    }
}