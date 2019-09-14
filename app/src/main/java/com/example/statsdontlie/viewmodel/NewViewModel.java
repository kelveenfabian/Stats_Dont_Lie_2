package com.example.statsdontlie.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;

import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.statsdontlie.constants.BDLAppConstants;
import com.example.statsdontlie.localdb.BDLDatabaseRepositoryImpl;
import com.example.statsdontlie.model.PlayerAverageModel;
import com.example.statsdontlie.network.RetrofitSingleton;
import com.example.statsdontlie.repository.BDLRepository;
import com.example.statsdontlie.utils.GameStatUtil;
import com.example.statsdontlie.utils.ImageUtil;
import com.example.statsdontlie.utils.PlayerModelCreator;
import com.example.statsdontlie.utils.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

import io.reactivex.Observer;
import io.reactivex.internal.operators.completable.CompletableFromAction;
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
                .map(bdlResponseSingle -> bdlResponseSingle.blockingGet().getData())
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
                .map(playerAverageModel -> new Pair<PlayerAverageModel,byte[]>(
                        playerAverageModel,ImageUtil.getBitmapAsByteArray(
                        ImageUtil.getBitmapFromURL(
                                playerAverageModel.getImage()))))
                .subscribeOn(Schedulers.computation())
                .map(pair -> {
                    Completable.fromAction(() -> {
                        databaseRepository.addPlayerData(pair.first);
                        databaseRepository.addPlayerImage(
                                pair.first.getPlayerId().intValue(),
                                pair.second);
                    })
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                    return pair.first;
                });
}

    public List<PlayerAverageModel> getPlayerAverageModelList() {
        return databaseRepository.getPlayerAverageModelList();
    }

    public Bitmap getPlayerImageById(int playerId) {
        return databaseRepository.getPlayerImageById(playerId);
    }

    public List<Bitmap> getPlayerImageList() {
        return databaseRepository.getPlayerImageList();
    }


}



