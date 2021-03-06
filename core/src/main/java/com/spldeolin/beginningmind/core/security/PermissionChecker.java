package com.spldeolin.beginningmind.core.security;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import com.spldeolin.beginningmind.core.entity.PermissionEntity;
import com.spldeolin.beginningmind.core.security.exception.UnauthorizeException;
import com.spldeolin.beginningmind.core.security.util.SignContext;

/**
 * @author Deolin 2018/12/07
 */
@Component
public class PermissionChecker {

    public void ensurePermission(HttpServletRequest request) throws UnauthorizeException {
        String url = request.getRequestURI();

        boolean isPermit = false;
        for (PermissionEntity permission : SignContext.current().getPermissions()) {
            if (request.getMethod().equalsIgnoreCase(permission.getMappingMethod())
                    && url.startsWith(permission.getMappingPath())) {
                isPermit = true;
            }
        }
        if (!isPermit) {
            throw new UnauthorizeException("权限不足");
        }
    }

}
