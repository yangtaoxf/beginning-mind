/*
 * Generated by Cadeau Support.
 *
 * https://github.com/spldeolin/cadeau-support
 */

package com.spldeolin.beginningmind.core.controller;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.spldeolin.beginningmind.core.api.exception.ServiceException;
import com.spldeolin.beginningmind.core.input.SecurityUserInput;
import com.spldeolin.beginningmind.core.service.SecurityUserService;
import com.spldeolin.beginningmind.core.valid.annotation.Require;

/**
 * “用户”管理
 *
 * @author Deolin 2018/5/28
 */
@RestController
@RequestMapping("/securityUser")
@Validated
public class SecurityUserController {

    @Autowired
    private SecurityUserService securityUserService;

    /**
     * 创建一个“用户”
     */
    @PostMapping("/create")
    Object create(@RequestBody @Valid SecurityUserInput securityUserInput) {
        return securityUserService.createEX(securityUserInput.toModel());
    }

    /**
     * 获取一个“用户”
     */
    @GetMapping("/get/{id}")
    Object get(@PathVariable Long id) {
        return securityUserService.getEX(id);
    }

    /**
     * 更新一个“用户”
     */
    @PostMapping("/update/{id}")
    Object update(@PathVariable Long id,
            @RequestBody @Valid @Require("updatedAt") SecurityUserInput securityUserInput) {
        securityUserService.updateEX(securityUserInput.toModel().setId(id));
        return null;
    }

    /**
     * 删除一个“用户”
     */
    @PostMapping("/delete/{id}")
    Object delete(@PathVariable Long id) {
        securityUserService.deleteEX(id);
        return null;
    }

    /**
     * 获取一批“用户”
     */
    @GetMapping("/search")
    Object page(@RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") @Max(1000) int pageSize) {
        return securityUserService.page(pageNo, pageSize);
    }

    /**
     * 删除一批“用户”
     */
    @PostMapping("/batchDelete")
    Object delete(@RequestBody List<Long> ids) {
        return securityUserService.deleteEX(ids);
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/banPick/{id}")
    Object banPick(@PathVariable Long id) {
        securityUserService.banPick(id);
        return null;
    }

}