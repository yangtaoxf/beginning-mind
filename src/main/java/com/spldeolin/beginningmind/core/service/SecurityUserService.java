/*
 * Generated by Cadeau Support.
 *
 * https://github.com/spldeolin/cadeau-support
 */

package com.spldeolin.beginningmind.core.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.spldeolin.beginningmind.core.api.CommonService;
import com.spldeolin.beginningmind.core.api.dto.Page;
import com.spldeolin.beginningmind.core.api.dto.PageParam;
import com.spldeolin.beginningmind.core.model.SecurityUser;

/**
 * “用户”业务
 *
 * @author Deolin 2018/5/28
 */
public interface SecurityUserService extends CommonService<SecurityUser> {

    /**
     * 创建一个“用户”
     * （附带业务校验）
     *
     * @param securityUser 待创建“用户”
     * @return 自增ID
     */
    Long createEX(SecurityUser securityUser);

    /**
     * 获取一个“用户”
     * （附带业务校验）
     *
     * @param id “用户”的ID
     * @return “用户”
     */
    SecurityUser getEX(Long id);

    /**
     * 更新一个“用户”
     * （附带业务校验）
     *
     * @param securityUser 待更新“用户”
     */
    void updateEX(SecurityUser securityUser);

    /**
     * 删除一个“用户”
     *
     * @param id 待删除“用户”的ID
     */
    void deleteEX(Long id);

    /**
     * 删除多个资源
     * （附带业务校验，并返回详细情况）
     *
     * @param ids 待删除资源的ID列表
     * @return 删除情况
     */
    String deleteEX(List<Long> ids);

    /**
     * 分页获取资源
     *
     * @param pageParam 页码和每页条目数
     * @return Page 分页对象
     */
    Page<SecurityUser> page(PageParam pageParam); // 根据具体需求拓展这个方法（追加搜索用参数等）

    /**
     * 通过用户名或手机号或email搜索用户
     */
    Optional<SecurityUser> searchOneByPrincipal(String principal);

    /**
     * 获取用户关联到的所有权限
     */
    Set<String> listUserPermissions(Long userId);

    /**
     * 获取指定用户的在线情况
     */
    Boolean isAccountSigning(Long userId);

    /**
     * 将指定用户踢下线
     */
    void killSigner(Long userId);

    /**
     * 启用/禁用用户
     */
    void banPick(Long userId);

}