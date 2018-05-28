/*
 * Generated by Cadeau Support.
 *
 * https://github.com/spldeolin/cadeau-support
 */

package com.spldeolin.beginningmind.input;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.Size;
import com.spldeolin.beginningmind.model.SecurityUser;
import com.spldeolin.beginningmind.valid.annotation.TextOption;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * “用户”Input类
 *
 * @author Deolin 2018/5/28
 */
@Data
@Accessors(chain = true)
public class SecurityUserInput implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 审计字段 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 昵称
     */
    @Size(max = 255)
    private String nickname;

    /**
     * 头像URL
     */
    @Size(max = 255)
    private String headerurl;

    /**
     * 性别
     */
    @Size(max = 6)
    @TextOption({"male", "female"})
    private String sex;

    /**
     * 联系地址
     */
    @Size(max = 255)
    private String address;

    private static final long serialVersionUID = 1L;

    public SecurityUser toModel() {
        return SecurityUser.builder().id(id).updatedAt(updatedAt).nickname(nickname).headerurl(headerurl).sex(
                sex).address(address).build();
    }

}