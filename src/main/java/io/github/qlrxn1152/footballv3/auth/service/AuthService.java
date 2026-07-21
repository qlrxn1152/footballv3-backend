package io.github.qlrxn1152.footballv3.auth.service;

import io.github.qlrxn1152.footballv3.auth.dto.request.LoginRequest;
import io.github.qlrxn1152.footballv3.auth.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
