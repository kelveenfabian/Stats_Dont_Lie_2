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

import io.reactivex.Completable;
import io.reactivex.Observable;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class NewViewModel extends AndroidViewModel {
    private BDLDatabaseRepositoryImpl databaseRepository;
    private BDLRepository repository;

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
                .map(new Function<Single<BDLResponse>, List<BDLResponse.GameStats>>() {
                    @Override
                    public List<BDLResponse.GameStats> apply(Single<BDLResponse> bdlResponseSingle) throws Exception {
                        return bdlResponseSingle.blockingGet().getData();
                    }
                })
                .map(gameStats -> {
                    GameStatUtil gameStatUtil = new GameStatUtil(gameStats);
                    PlayerModelCreator.calculatePlayerAvg(gameStatUtil);

                    PlayerAverageModel playerAverageModel = PlayerModelCreator.createPlayerModel(
                            gameStats.get(0).getPlayer().getId(),
                            PlayerUtil.getPlayerPhotoUrl(
                                    gameStats.get(0).getPlayer().getFirstName(),
                                    gameStats.get(0).getPlayer().getLastName()
                            ),
                            gameStatUtil);
                    return playerAverageModel;
                })
                .subscribeOn(Schedulers.computation())
                .map(playerAverageModel -> {
                    Completable.fromAction(() ->
                            databaseRepository.addPlayerData(playerAverageModel))
                            .subscribeOn(Schedulers.io())
                            .subscribe();
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



