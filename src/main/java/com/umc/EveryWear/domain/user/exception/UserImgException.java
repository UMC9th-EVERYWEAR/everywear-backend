package com.umc.EveryWear.domain.user.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;

public class UserImgException extends GeneralException {
  public UserImgException(BaseErrorCode code) {super(code);}
}
