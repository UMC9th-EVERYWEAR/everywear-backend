package com.umc.EveryWear.domain.fitting.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;

public class FittingException extends GeneralException {
    public FittingException(BaseErrorCode code) {
        super(code);
    }
}
