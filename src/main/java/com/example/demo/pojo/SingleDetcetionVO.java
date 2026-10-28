package com.example.demo.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class SingleDetcetionVO {
    private String path;
    private String portRange;
    private List<Integer> passablePortList;
    private Integer passablePortCount;
}
