package com.example.service;

import java.util.*;

public class UserService {

    private final Map<String, String> users = new HashMap<>();

    public void addUser(String userId, String name) {
        if(userId != null){
            if(name != null){
            users.put(userId, name);
            }
            else {
                System.out.println("Name is null");
            }
        }
        else {
            System.out.println("User ID is null");
        }
    }

    public boolean isValidUser(String userId){
        if(users.containsKey(userId)){
            return true;
        } else {
            return false;
        }
    }

    public void printAllUsers(){
        for(String key: users.keySet()){
            System.out.println("User: "+ key + " - " + users.get(key));
        }
        for(String key: users.keySet()){
            System.out.println("Again: "+ key + " - " + users.get(key));
        }
    }

    public void process(){
        String unused = "this will not be used";

        int sum = 0;
        for(int i=0;i<10;i++){
            sum += i;
        }
        System.out.println("Sum is: "+sum);

        int x = 42;
        if(x == 42){
            System.out.println("x is the answer");
        }else{
            System.out.println("x is not the answer");
        }
    }

}
