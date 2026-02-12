package com.umc.EveryWear.domain.user.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;

public class UserProductException extends GeneralException {
    public UserProductException(BaseErrorCode code) {
        super(code);
    }
}
