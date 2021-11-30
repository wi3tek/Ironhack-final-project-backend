package com.predictmatch.edgeservice.proxy;

import com.predictmatch.edgeservice.dto.team.TeamDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("live-results-service")
public interface TeamsProxy {

    @GetMapping("/liveresults/api/v1/teams")
    public ResponseEntity<List<TeamDto>> getAllTeams();


}
