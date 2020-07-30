package com.gzf.sentinel.entry;

import lombok.Data;

/**
 * @program: sentinel
 * @description:
 * @author: Gaozf
 * @create: 2020-06-12 16:47
 **/
@Data
public class User {
    private int id;
    private String name;

    public User() {
    }
    public User(String str) {
        this.id = 9999;
        this.name = str;
    }
}
