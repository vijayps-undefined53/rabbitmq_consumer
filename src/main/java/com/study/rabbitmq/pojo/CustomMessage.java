package com.study.rabbitmq.pojo;

import lombok.Data;

@Data
public class CustomMessage {
    private String text;
    private String priority;
}
