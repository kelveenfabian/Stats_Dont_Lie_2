package com.example.statsdontlie.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.statsdontlie.constants.BDLAppConstants;
import com.example.statsdontlie.localdb.BDLDatabaseRepositoryImpl;
import com.example.statsdontlie.model.BDLResponse;
import com.example.statsdontlie.model.PlayerAverageModel;
import com.example.statsdontlie.network.RetrofitSingleton;
import com.example.statsdontlie.repository.BDLRepository;
import com.example.statsdontlie.utils.GameStatUtil;
import com.example.statsdontlie.utils.PlayerModelCreator;
import com.example.statsdontlie.utils.PlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.internal.operators.completable.CompletableFromAction;

public class NewViewModel extends AndroidViewModel {
    private BDLDatabaseRepositoryImpl databaseRepository;
    private BDLRepository repository;
    private List<PlayerAverageModel> playerAverageModels = new ArrayList<>();

    public NewViewModel(@NonNull Application application) {
        super(application);
        repository = new BDLRepository(RetrofitSingleton.getSingleService());
        databaseRepository = BDLDatabaseRepositoryImpl.getInstance(application);
    }

    public static NewViewModel getInstance(AppCompatActivity activity) {
        return ViewModelProviders.of(activity).get(NewViewModel.class);
    }

    @SuppressLint("CheckResult")
    public Observable<PlayerAverageModel> callBDLResponseClient() {
        List<Integer> playerIdLists = new ArrayList<>();

        for (int playerIds : BDLAppConstants.PLAYER_ARRAY_CONSTANTS) {
            playerIdLists.add(playerIds);
        }

        return Observable.fromIterable(playerIdLists)
                .map(playerId -> repository.callBDLResponseClient(playerId))
                .map(bdlResponseSingle -> bdlResponseSingle.blockingGet().getData())

                .flatMapIterable(responses -> {


                    for(BDLResponse s : responses){
                    GameStatUtil gameStatUtil = new GameStatUtil(s);

                    PlayerModelCreator.calculatePlayerAvg(gameStatUtil);

                    PlayerAverageModel playerAverageModel = PlayerModelCreator.createPlayerModel(
                            response.blockingGet().getData().get(0).getPlayer().getId(),
                            PlayerUtil.getPlayerPhotoUrl(
                                    response.blockingGet().getData().get(0).getPlayer().getFirstName(),
                                    response.blockingGet().getData().get(0).getPlayer().getLastName()
                            ),
                            gameStatUtil);

                    return playerAverageModel;})
                .map(playerAverageModel -> {
                    databaseRepository.addPlayerData(playerAverageModel);
                    return playerAverageModel;
                });
    }

    public List<PlayerAverageModel> getPlayerAverageModels() {
        return databaseRepository.getPlayerAverageModelList();
    }

    public BDLDatabaseRepositoryImpl getDatabaseRepository(){
        return databaseRepository;
    }
}



