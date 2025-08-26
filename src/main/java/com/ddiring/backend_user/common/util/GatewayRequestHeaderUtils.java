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

    public static Long getUserId() {
        Long userId = Long.parseLong(getRequestHeaderParamAsString("X-Auth-UserId"));
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return userId;
    }

    public static String getClientDevice() {
        String clientDevice = getRequestHeaderParamAsString("X-Client-Device");
        if (clientDevice == null) {
            return null;
        }
        return clientDevice;
    }

    public static String getClientAddress() {
        String clientAddress = getRequestHeaderParamAsString("X-Client-Address");
        if (clientAddress == null) {
            return null;
        }
        return clientAddress;
    }

}