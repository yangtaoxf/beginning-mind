/*
 * Generated by Cadeau Support.
 *
 * https://github.com/spldeolin/cadeau-support
 */

package com.spldeolin.beginningmind.core.input;

import java.io.Serializable;
import java.time.LocalDateTime;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import com.spldeolin.beginningmind.core.api.valid.annotation.Mobile;
import com.spldeolin.beginningmind.core.model.User;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * “用户”Input类
 *
 * @author Deolin 2018/8/4
 */
@Data
@Accessors(chain = true)
public class UserInput implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 审计字段 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 名字
     */
    @Length(max = 255)
    private String name;

    /**
     * 手机号
     */
    @Length(max = 20)
    @Mobile
    private String mobile;

    /**
     * E-mail
     */
    @Length(max = 255)
    @Email
    private String email;

    /**
     * 密码
     */
    @Length(max = 128)
    private String password;

    /**
     * 盐
     */
    @Length(max = 32)
    private String salt;

    /**
     * 能否登录
     */
    private Boolean enableSign;

    private static final long serialVersionUID = 1L;

    public User toModel() {
        return User.builder().id(id).updatedAt(updatedAt).name(name).mobile(mobile).email(email).password(password)
                .salt(salt).enableSign(enableSign).build();
    }

}
// id, updatedAt, name, mobile, email, password, salt, enableSign