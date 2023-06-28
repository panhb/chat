package com.demo.chat.model.vo;

import lombok.Data;

/**
 * @author hongbo.pan
 * @date 2023/6/27
 */
@Data
public class MessageVO {

    private String fromUser;
    private String toUser;
    private String message;
    private String sendType;
}
