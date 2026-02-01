package com.umc.EveryWear.domain.closet.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;

public class ClosetException extends GeneralException {
    public ClosetException(BaseErrorCode code) {
        super(code);
    }

    public ClosetException(BaseErrorCode code, String reason) {
        super(code, reason);
    }
}
