package de.honoka.qqrobot.normal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户被施加的浇水状态信息
 */
@Entity
@Data
@Accessors(chain = true)
public class UserStatus implements Serializable {

    //记录的编号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Integer id;

    //受影响的QQ
    private Long qq;

    //来源QQ
    private Long fromQq;

    //来源群
    private Long fromGroup;

    //施加时间
    private Date time;

    //状态名
    private String status;
}
