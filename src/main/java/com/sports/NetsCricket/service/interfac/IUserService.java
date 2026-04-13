package com.sports.NetsCricket.service.interfac;

import com.sports.NetsCricket.dto.LoginRequest;
import com.sports.NetsCricket.dto.RegisterRequest;
import com.sports.NetsCricket.dto.Response;

public interface IUserService {
    Response register(RegisterRequest request);

    Response login(LoginRequest loginRequest);

    Response getAllUsers();

    Response getUserBookingHistory(String userId);

    Response deleteUser(String userId);

    Response getUserById(String userId);

    Response getMyInfo(String email);

}
