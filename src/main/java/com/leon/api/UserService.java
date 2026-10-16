package com.leon.api;

public interface UserService {

    String getUserById(Long id);

    String getUserById(String username);

    String getUserName();

    void printWelcome();
}