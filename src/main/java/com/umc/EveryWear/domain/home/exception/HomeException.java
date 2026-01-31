package com.umc.EveryWear.domain.home.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;

public class HomeException extends GeneralException {
    public HomeException(BaseErrorCode code) {
        super(code);
    }
}
