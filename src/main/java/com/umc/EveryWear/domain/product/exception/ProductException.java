package com.umc.EveryWear.domain.product.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;

public class ProductException extends GeneralException {
    public ProductException(BaseErrorCode code) {
        super(code);
    }
}
