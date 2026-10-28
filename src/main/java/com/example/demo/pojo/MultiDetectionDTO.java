package com.example.demo.pojo;
import java.util.List;
import lombok.Data;
@Data
public class MultiDetectionDTO {
    private List<ISocket> socketList;
    private int timeout = 1000;
}
