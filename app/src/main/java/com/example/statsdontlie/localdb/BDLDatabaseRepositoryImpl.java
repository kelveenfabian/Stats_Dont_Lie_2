package com.example.statsdontlie.localdb;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.sql.NBAPlayer;
import com.example.sql.PlayerImage;
import com.example.statsdontlie.Database;
import com.example.statsdontlie.model.PlayerAverageModel;
import com.squareup.sqldelight.Query;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BDLDatabaseRepositoryImpl implements BDLDatabaseRepository{
    private static BDLDatabase bdlDatabase;
    private static BDLDatabaseRepositoryImpl instance;

    private BDLDatabaseRepositoryImpl(Application application) {
        bdlDatabase = BDLDatabase.getInstance(application.getApplicationContext());
    }

    public static BDLDatabaseRepositoryImpl getInstance(Application application) {
        if(instance == null){
            instance = new BDLDatabaseRepositoryImpl(application);
        }
        return instance;
    }

    @Override
    public void addPlayerData(PlayerAverageModel playerAverageModel) {
        bdlDatabase.addNBAPlayer(playerAverageModel);
    }

    @Override
    public PlayerAverageModel getPlayerAverageModelById(int playerID) {
        Query<NBAPlayer> q = bdlDatabase.getNBAPlayerQueries().selectById(playerID);

        Log.d("danny",q.executeAsList().toString() + playerID);
        return new PlayerAverageModel(
          q.executeAsOne().getPlayerId(),
          q.executeAsOne().getFirstName(),
          q.executeAsOne().getLastName(),
          q.executeAsOne().getImage(),
          q.executeAsOne().getPlayerPointAvg(),
          q.executeAsOne().getPlayerAssistAvg(),
          q.executeAsOne().getPlayerBlocksAvg(),
          q.executeAsOne().getPlayerDefRebAvg(),
          q.executeAsOne().getPlayer3PM(),
          q.executeAsOne().getPlayer3PA()
        );
    }

    @Override
    public List<PlayerAverageModel> getPlayerAverageModelList(){
        List<PlayerAverageModel> playerAverageModelList = new ArrayList<PlayerAverageModel>();

        for(NBAPlayer p : bdlDatabase.getNBAPlayerQueries().selectAll().executeAsList()){
            playerAverageModelList.add(getPlayerAverageModelById(((int) p.getPlayerId())));
        }
        return playerAverageModelList;
    }

    @Override
    public void deletePlayerById(int playerId) {
        bdlDatabase.deletePlayerById(playerId);
    }

    @Override
    public void deleteAllPlayers() {
        bdlDatabase.deleteAllPlayers();
    }

    @Override
    public void addPlayerImage(int playerId, byte[] image) {
        bdlDatabase.addPlayerImage(playerId, image);
    }

    @Override
    public Bitmap getPlayerImageById(int playerId) {
        return BitmapFactory.decodeByteArray(
                bdlDatabase.getPlayerImage(playerId),
                0,
                bdlDatabase.getPlayerImage(playerId).length);
    }

    public List<Bitmap> getPlayerImageList() {
        List<Bitmap> playerImageList = new ArrayList<>();
        for(NBAPlayer p : bdlDatabase.getNBAPlayerQueries().selectAll().executeAsList()) {
            playerImageList.add(getPlayerImageById((int)p.getPlayerId()));
        }
        return playerImageList;
    }
}
