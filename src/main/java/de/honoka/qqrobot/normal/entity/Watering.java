package de.honoka.qqrobot.normal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Accessors(chain = true)
public class Watering implements Serializable {

    @Id
    @TableId(type = IdType.INPUT)
    private Long qq;

    private Integer level = 1;

    private Integer nowExp = 0;

    private Date lastTimeWatering;

    private Date nextTimeWatering;

    public void plusExp(int amount) {
        nowExp += amount;
    }
}
