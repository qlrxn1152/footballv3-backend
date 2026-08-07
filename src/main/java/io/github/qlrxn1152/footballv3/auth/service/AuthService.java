package io.github.qlrxn1152.footballv3.auth.service;

import io.github.qlrxn1152.footballv3.auth.dto.request.MemberLoginRequest;
import io.github.qlrxn1152.footballv3.auth.dto.response.MemberLoginResponse;

public interface AuthService {

    MemberLoginResponse login(MemberLoginRequest request);
}
