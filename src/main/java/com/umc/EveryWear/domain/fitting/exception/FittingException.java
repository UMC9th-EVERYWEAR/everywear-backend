package com.umc.EveryWear.domain.fitting.exception;

import com.umc.EveryWear.domain.fitting.enums.FittingClientErrorType;
import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;
import lombok.Getter;

@Getter
public class FittingException extends GeneralException {

    private final FittingClientErrorType clientErrorType;

    public FittingException(BaseErrorCode code) {
        super(code);
        this.clientErrorType = FittingClientErrorType.UNKNOWN_ERROR;
    }

    public FittingException(
            BaseErrorCode code,
            FittingClientErrorType clientErrorType
    ) {
        super(code);
        this.clientErrorType =
                clientErrorType == null
                        ? FittingClientErrorType.UNKNOWN_ERROR
                        : clientErrorType;
    }
}
