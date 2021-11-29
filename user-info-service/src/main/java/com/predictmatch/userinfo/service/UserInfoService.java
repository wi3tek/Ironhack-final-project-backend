package com.predictmatch.userinfo.service;

import com.predictmatch.userinfo.dto.*;
import org.springframework.http.ResponseEntity;

public interface UserInfoService {
    ResponseEntity<UserInfoResponse> findUserByUsername(String username);

    ResponseEntity<UserInfoResponse> findUserById(Long id);

    ResponseEntity<CreatedUserInfo> createUser(UserInfoRequest request);

    ResponseEntity<UserInfoResponse> changeFavouriteTeam(Long id, TeamRequestDto team);

    ResponseEntity<UserInfoResponse> updateUserInfo(Long id, UserInfoRequest request);

    void removeUser(Long id);
}
