package com.ddiring.backend_user.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ddiring.backend_user.common.exception.CustomException;
import com.ddiring.backend_user.common.exception.ErrorCode;

public class GatewayRequestHeaderUtils {
    public static String getRequestHeaderParamAsString(String key) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes();
        return requestAttributes.getRequest().getHeader(key);
    }

    public static String getUserSeq() {
        String userSeq = (getRequestHeaderParamAsString("userSeq"));
        if (userSeq == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return userSeq;
    }

    public static String getRole() {
        String role = getRequestHeaderParamAsString("role");
        if (role == null) {
            return null;
        }
        return role;
    }

}