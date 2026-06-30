package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String campus;
    private String intro;
    private Integer role;
    /** 1=正常 0=禁用 */
    private Integer accountStatus;
    private Date createdAt;
    private Date updatedAt;
}
