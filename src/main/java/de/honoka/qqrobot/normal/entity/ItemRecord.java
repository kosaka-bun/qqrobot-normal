package de.honoka.qqrobot.normal.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@Data
@Accessors(chain = true)
public class ItemRecord implements Serializable {

    @Id
    private Long qq;

    @Id
    private String itemName;

    private Integer count;
}
