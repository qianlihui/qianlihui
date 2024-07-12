package com.qlh.base.mybatis;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * abstract domain
 */
@Accessors(chain = true)
@Data
public class DomainBase {

    @Column(comment = "主键", cassandra = false)
    private Long id;
    @Column(comment = "创建人ID", len = 64)
    private String creator = "";
    @Column(comment = "修改人ID", len = 64)
    private String modifier = "";
    @Column(comment = "创建时间")
    private Date createTime;
    @Column(comment = "更新时间")
    private Date updateTime;
    @Column(comment = "乐观锁")
    private int version;

    public void increaseVersion() {
        version++;
    }

}
