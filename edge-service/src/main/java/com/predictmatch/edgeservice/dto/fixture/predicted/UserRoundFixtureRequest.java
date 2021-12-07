package com.predictmatch.edgeservice.dto.fixture.predicted;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserRoundFixtureRequest {

    String username;
    Integer round;

}
