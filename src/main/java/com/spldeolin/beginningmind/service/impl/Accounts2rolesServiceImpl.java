package com.spldeolin.beginningmind.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spldeolin.beginningmind.api.CommonServiceImpl;
import com.spldeolin.beginningmind.dao.Accounts2rolesMapper;
import com.spldeolin.beginningmind.model.Accounts2roles;
import com.spldeolin.beginningmind.service.Accounts2rolesService;
import lombok.extern.log4j.Log4j2;
import tk.mybatis.mapper.entity.Condition;
import com.spldeolin.beginningmind.api.exception.ServiceException;
import com.spldeolin.beginningmind.api.dto.Page;
import com.github.pagehelper.PageHelper;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example;

/**
 * “帐号与权限的关联”业务实现
 *
 * @author Deolin 2018/5/1
 * @generator Cadeau Support
 */
@Service
@Log4j2
public class Accounts2rolesServiceImpl extends CommonServiceImpl<Accounts2roles> implements Accounts2rolesService {

    @Autowired
    private Accounts2rolesMapper accounts2rolesMapper;

    @Override
    public Long createEX(Accounts2roles accounts2roles) {
        /* 业务校验 */
        super.create(accounts2roles);
        return accounts2roles.getId();
    }

    @Override
    public void updateEX(Accounts2roles accounts2roles) {
        if (!isExist(accounts2roles.getId())) {
            throw new ServiceException("帐号与权限的关联不存在或是已被删除");
        }
        /* 业务校验 */
        if (!super.update(accounts2roles)) {
            throw new ServiceException("帐号与权限的关联数据过时");
        }
    }

    @Override
    public void deleteEX(Long id) {
        if (!isExist(id)) {
            throw new ServiceException("帐号与权限的关联不存在或是已被删除");
        }
        /* 业务校验 */
        super.delete(id);
    }

    @Override
    public String deleteEX(List<Long> ids) {
        List<Accounts2roles> exist = super.get(ids);
        if (exist.size() == 0) {
            throw new ServiceException("选中的帐号与权限的关联全部不存在或是已被删除");
        }
        /* 业务校验 */
        super.delete(ids);
        return "操作成功";
    }

    @Override
    public Page<Accounts2roles> page(Integer pageNo, Integer pageSize) {
        Condition condition = new Condition(Accounts2roles.class);
        condition.createCriteria()/* 添加条件 */;
        PageHelper.startPage(pageNo, pageSize);
        return Page.wrap(accounts2rolesMapper.selectBatchByCondition(condition));
    }

}