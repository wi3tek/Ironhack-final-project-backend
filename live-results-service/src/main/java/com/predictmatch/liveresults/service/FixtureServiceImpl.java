package com.predictmatch.liveresults.service;

import com.predictmatch.liveresults.dao.Fixture;
import com.predictmatch.liveresults.dao.Team;
import com.predictmatch.liveresults.dto.FixtureDto;
import com.predictmatch.liveresults.enmus.FixtureStatus;
import com.predictmatch.liveresults.mapper.FixtureMapper;
import com.predictmatch.liveresults.repository.FixtureRepository;
import com.predictmatch.liveresults.repository.TeamRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FixtureServiceImpl implements FixtureService {

    @Autowired
    FixtureRepository fixtureRepository;

    @Autowired
    TeamRepository teamRepository;

    @Override
    public ResponseEntity<List<FixtureDto>> initAllFixtures() {
        List<FixtureDto> fixturesDtos = new ArrayList<>();

        List<Fixture> storedFixtures = fixtureRepository.findAll();

        storedFixtures.forEach( fixture -> {
            Optional<Team> storedHomeTeam = teamRepository.findById( fixture.getHomeTeamId());
            Optional<Team> storedAwayTeam = teamRepository.findById( fixture.getAwayTeamId());

            if(storedHomeTeam.isEmpty()) {
                throw new EntityNotFoundException("Not found home team with id: "+fixture.getHomeTeamId());
            }

            if(storedAwayTeam.isEmpty()) {
                throw new EntityNotFoundException("Not found home team with id: "+fixture.getAwayTeamId());
            }

            fixturesDtos.add( FixtureMapper.entityToDto(fixture,storedHomeTeam.get(),storedAwayTeam.get()));

        });

        return ResponseEntity.ok(fixturesDtos);
    }

    @Override
    public ResponseEntity<List<FixtureDto>> initFixturesByStatus(FixtureStatus fixtureStatus) {
        List<FixtureDto> fixturesDtos = new ArrayList<>();

        List<Fixture> storedFixtures = fixtureRepository.findFixturesByFixtureStatus(getFixtureStatuses( fixtureStatus ));

        if(storedFixtures.size() == 0)
            return ResponseEntity.notFound().header( "message","" ).build();



        storedFixtures.forEach( fixture -> {
            Optional<Team> storedHomeTeam = teamRepository.findById( fixture.getHomeTeamId());
            Optional<Team> storedAwayTeam = teamRepository.findById( fixture.getAwayTeamId());

            if(storedHomeTeam.isEmpty()) {
                throw new EntityNotFoundException("Not found home team with id: "+fixture.getHomeTeamId());
            }

            if(storedAwayTeam.isEmpty()) {
                throw new EntityNotFoundException("Not found home team with id: "+fixture.getAwayTeamId());
            }

            fixturesDtos.add( FixtureMapper.entityToDto(fixture,storedHomeTeam.get(),storedAwayTeam.get()));

        });

        return ResponseEntity.ok(fixturesDtos);
    }

    private static List<String> getFixtureStatuses(FixtureStatus fixtureStatus) {
        return switch (fixtureStatus) {
            case LIVE -> List.of("1H","2H", "HT");
            case POSTPONED -> List.of("PST");
            case FINISHED -> List.of("FT");
            case UPCOMING -> List.of("NS", "TBD");
        };
    }
}
