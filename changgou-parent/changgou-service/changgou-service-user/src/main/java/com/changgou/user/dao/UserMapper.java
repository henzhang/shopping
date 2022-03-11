package com.changgou.user.dao;

import com.changgou.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:henzhang
 * @Description:User的Dao
 *****/
public interface UserMapper extends Mapper<User> {

    /**
     * 添加积分
     *
     * @param username
     * @param points
     */
    @Update("UPDATE tb_user SET points=points+#{points} WHERE username=#{username}")
    void addUserPoints(@Param("username") String username, @Param("points") Integer points);
}
