package com.example.demo.pojo;

import lombok.Data;

@Data
public class ISocket {
    String path = "localhost";
    int port = 80;
    boolean isPassable;
}
