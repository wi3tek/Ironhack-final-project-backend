package com.predictmatch.predictionservice.predictionservice.service;

import com.predictmatch.predictionservice.predictionservice.dao.Prediction;
import com.predictmatch.predictionservice.predictionservice.dao.PredictionResult;
import com.predictmatch.predictionservice.predictionservice.dto.*;
import com.predictmatch.predictionservice.predictionservice.dto.fixture.FixtureDto;
import com.predictmatch.predictionservice.predictionservice.enums.PredictionStatus;
import com.predictmatch.predictionservice.predictionservice.exceptions.PredictionOnLiveMatchException;
import com.predictmatch.predictionservice.predictionservice.mapper.PredictionMapper;
import com.predictmatch.predictionservice.predictionservice.proxy.FixtureProxy;
import com.predictmatch.predictionservice.predictionservice.repository.PredictionRepository;
import com.predictmatch.predictionservice.predictionservice.repository.PredictionResultRepository;
import com.sun.istack.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PredictionServiceImpl implements PredictionService {

    @Autowired
    PredictionRepository predictionRepository;

    @Autowired
    PredictionResultRepository predictionResultRepository;

    @Autowired
    FixtureProxy fixtureProxy;

    @Override
    @Transactional
    public ResponseEntity<PredictionDto> addNewPrediction(PredictionRequest predictionRequest) {

        // 1. make a PredictionEntity from predictionRequest
        Optional<FixtureDto> fixture = Optional.ofNullable(fixtureProxy.findFixtureById( predictionRequest.getFixtureId()).getBody());


        if(fixture.isEmpty())
            throw new EntityNotFoundException("Error during making prediction - not found fixture with id: "+predictionRequest.getFixtureId());

        Prediction prediction = PredictionMapper.predictionRequestToPrediction( predictionRequest,fixture.get(),null);

        if(ChronoUnit.MINUTES.between ( fixture.get().getDate() ,prediction.getPredictionDate())>0)
            throw new PredictionOnLiveMatchException( fixture.get().getFixtureId());

        PredictionResult predictionResult = new PredictionResult(
                0,
                PredictionStatus.PENDING
        );

        predictionResultRepository.save(predictionResult);
        prediction.setPredictionResult( predictionResult );
        predictionRepository.save( prediction );



        return ResponseEntity.ok(PredictionMapper.predictionEntityToDto( prediction ));

    }

    @Override
    @Transactional
    public ResponseEntity<PredictionResponse> getUserPredictionByFixture(GetPredictionRequest predictionRequest) {

        // get fixture Result
        Optional<FixtureDto> fixtureDto =
                Optional.ofNullable(fixtureProxy.findFixtureById( predictionRequest.getFixtureId() ).getBody());

        // check if fixture is existing
        if(fixtureDto.get()==null)
            throw new EntityNotFoundException("Not found fixture with id: "+predictionRequest.getFixtureId());


        Optional<Prediction> storedPrediction =
                predictionRepository.findById( predictionRequest.getFixtureId()+"_"+ predictionRequest.getUserId());

        // check if there is prediction for the fixture
        if(storedPrediction.isEmpty())
            return ResponseEntity.ok( new PredictionResponse(null,new PredictionResultDto(0,PredictionStatus.NONE,
                    PredictionStatus.NONE.value )));


        PredictionResult predictionResult = storedPrediction.get().getPredictionResult();
        Prediction prediction= storedPrediction.get();

        // update prediction result for finished matches
        if(fixtureDto.get().getStatus().equals( "FINISHED") && predictionResult.getStatus().equals( PredictionStatus.PENDING )) {

            PredictionResult newResult = PredictionMapper.updatePredictionResult( prediction,
                    fixtureDto.get(), predictionResult);

            predictionResultRepository.save( newResult );

            prediction.setPredictionResult( newResult );
            predictionRepository.save( prediction);

            return ResponseEntity.ok(new PredictionResponse(PredictionMapper.predictionEntityToDto( prediction ),
                    PredictionMapper.predictionResultEntityToDto( newResult )
                    ));
        }

        return ResponseEntity.ok (new PredictionResponse(PredictionMapper.predictionEntityToDto( prediction ),
                PredictionMapper.predictionResultEntityToDto( predictionResult )));

    }

    @Override
    public ResponseEntity<List<PredictionResponse>> getAllUserPredictions(Long userId) {

        List<Prediction> userPredictions = predictionRepository.findByUserId( userId );
        List<PredictionResponse> predictionList = new ArrayList<>();

        if(userPredictions.size()==0)
            return ResponseEntity.ok(predictionList);

        userPredictions.forEach( prediction -> {

            predictionList.add( new PredictionResponse(PredictionMapper.predictionEntityToDto( prediction ),
                    PredictionMapper.predictionResultEntityToDto( prediction.getPredictionResult() )) );
        } );


        return ResponseEntity.ok(predictionList);
    }
}
