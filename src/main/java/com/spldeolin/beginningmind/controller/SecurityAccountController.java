package com.spldeolin.beginningmind.controller;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spldeolin.beginningmind.api.exception.ServiceException;
import com.spldeolin.beginningmind.aspect.annotation.PageNo;
import com.spldeolin.beginningmind.aspect.annotation.PageSize;
import com.spldeolin.beginningmind.controller.dto.RequestResult;
import com.spldeolin.beginningmind.input.SecurityAccountInput;
import com.spldeolin.beginningmind.service.SecurityAccountService;

/**
 * “帐号（用于登录的信息）”管理
 *
 * @author Deolin 2018/5/1
 * @generator Cadeau Support
 */
@RestController
@RequestMapping("security_accounts")
@Validated
public class SecurityAccountController {

    @Autowired
    private SecurityAccountService securityAccountService;

    /**
     * 创建一个“帐号（用于登录的信息）”
     */
    @PostMapping
    public RequestResult create(@RequestBody @Valid SecurityAccountInput securityAccountInput) {
        return RequestResult.success(securityAccountService.createEX(securityAccountInput.toModel()));
    }

    /**
     * 获取一个“帐号（用于登录的信息）”
     */
    @GetMapping("{id}")
    public RequestResult get(@PathVariable Long id) {
        return RequestResult.success(
                securityAccountService.get(id).orElseThrow(() -> new ServiceException("帐号（用于登录的信息）不存在或是已被删除")));
    }

    /**
     * 更新一个“帐号（用于登录的信息）”
     */
    @PutMapping("{id}")
    public RequestResult update(@PathVariable Long id, @RequestBody @Valid SecurityAccountInput securityAccountInput) {
        securityAccountService.updateEX(securityAccountInput.toModel().setId(id));
        return RequestResult.success();
    }

    /**
     * 删除一个“帐号（用于登录的信息）”
     */
    @DeleteMapping("{id}")
    public RequestResult delete(@PathVariable Long id) {
        securityAccountService.deleteEX(id);
        return RequestResult.success();
    }

    /**
     * 获取一批“帐号（用于登录的信息）”
     */
    @GetMapping
    public RequestResult page(@PageNo Integer pageNo, @PageSize Integer pageSize) {
        return RequestResult.success(securityAccountService.page(pageNo, pageSize));
    }

    /**
     * 删除一批“帐号（用于登录的信息）”
     */
    @PutMapping("batch_delete")
    public RequestResult delete(@RequestBody List<Long> ids) {
        return RequestResult.success(securityAccountService.deleteEX(ids));
    }

}