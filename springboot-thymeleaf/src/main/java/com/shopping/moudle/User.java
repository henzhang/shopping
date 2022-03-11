package com.shopping.moudle;

import lombok.AllArgsConstructor;
import lombok.Data;

/****
 * @Author:henzhang
 * @Description:
 *****/

@Data
@AllArgsConstructor
public class User {
    private Integer id;
    private String name;
    private String address;
}
