/*
 * Generated by Cadeau Support.
 *
 * https://github.com/spldeolin/cadeau-support
 */

package com.spldeolin.beginningmind.core.controller;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.spldeolin.beginningmind.core.api.dto.Page;
import com.spldeolin.beginningmind.core.api.dto.PageParam;
import com.spldeolin.beginningmind.core.input.SecurityUserInput;
import com.spldeolin.beginningmind.core.model.SecurityUser;
import com.spldeolin.beginningmind.core.service.SecurityUserService;

/**
 * “用户”管理
 *
 * @author Deolin 2018/7/3
 */
@RestController
@RequestMapping("/securityUser")
@Validated
public class SecurityUserController {

    @Autowired
    private SecurityUserService securityUserService;

    /**
     * 创建一个“用户”
     *
     * @param securityUserInput 待创建的“用户”
     * @return 创建成功后生成的ID
     */
    @PostMapping("/create")
    Long create(@RequestBody @Valid SecurityUserInput securityUserInput) {
        return securityUserService.createEX(securityUserInput.toModel());
    }

    /**
     * 获取一个“用户”
     *
     * @param id 待获取“用户”的ID
     * @return 用户
     */
    @GetMapping("/get")
    SecurityUser get(@RequestParam Long id) {
        return securityUserService.getEX(id);
    }

    /**
     * 更新一个“用户”
     *
     * @param id 待更新“用户”的ID
     * @param securityUserInput 待更新的“用户”
     */
    @PostMapping("/update")
    void update(@RequestParam Long id, @RequestBody @Valid SecurityUserInput securityUserInput) {
        securityUserService.updateEX(securityUserInput.toModel().setId(id));
    }

    /**
     * 删除一个“用户”
     *
     * @param id 待删除“用户”的ID
     */
    @PostMapping("/delete")
    void delete(@RequestParam Long id) {
        securityUserService.deleteEX(id);
    }

    /**
     * 获取一批“用户”
     *
     * @param pageParam 页码和每页条目数
     * @return “用户”分页
     */
    @GetMapping("/search")
    Page<SecurityUser> search(PageParam pageParam) {
        return securityUserService.page(pageParam);
    }

    /**
     * 删除一批“用户”
     *
     * @param ids 待删除“用户”的ID列表
     * @return 删除情况
     */
    @PostMapping("/batchDelete")
    String delete(@RequestParam List<Long> ids) {
        return securityUserService.deleteEX(ids);
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/banPick")
    void banPick(@RequestParam Long id) {
        securityUserService.banPick(id);
    }

}