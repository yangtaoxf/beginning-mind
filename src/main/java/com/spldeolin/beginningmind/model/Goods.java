package com.spldeolin.beginningmind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 商品
 *
 * @author Deolin 2018/5/1
 * @generator Cadeau Support
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Table(name = "goods")
public class Goods implements Serializable {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 审计字段 插入时间
     */
    @Column(name = "inserted_at")
    @JsonIgnore
    private LocalDateTime insertedAt;

    /**
     * 审计字段 更新时间
     */
    @Version
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 审计字段 是否被删除
     */
    @Column(name = "is_deleted")
    @JsonIgnore
    private Boolean isDeleted;

    /**
     * 商品名
     */
    private String name;

    /**
     * 净重（单位g）
     */
    @Column(name = "net_weight")
    private Integer netWeight;

    /**
     * 单价
     */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /**
     * 库存余量
     */
    @Column(name = "stock_balance")
    private Integer stockBalance;

    private static final long serialVersionUID = 1L;
}