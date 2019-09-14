package com.example.statsdontlie.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.example.statsdontlie.OnFragmentInteractionListener;
import com.example.statsdontlie.R;
import com.example.statsdontlie.constants.BDLAppConstants;
import com.example.statsdontlie.view.fragments.GameFragment;
import com.example.statsdontlie.view.fragments.MenuFragment;
import com.example.statsdontlie.view.fragments.ResultFragment;
import com.example.statsdontlie.viewmodel.NewViewModel;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {
    NewViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = NewViewModel.getInstance(this);
        viewModelSetUp();
    }

    @SuppressLint("CheckResult")
    private void viewModelSetUp() {
        NewViewModel viewModel = NewViewModel.getInstance(this);

        if(viewModel.getPlayerAverageModelList().size() < BDLAppConstants.PLAYER_ARRAY_CONSTANTS.length
        || viewModel.getPlayerImageList().size() < BDLAppConstants.PLAYER_ARRAY_CONSTANTS.length) {
            Log.d("danny: inital list size",
                    "=" + viewModel.getPlayerAverageModelList().size());
            viewModel.callBDLResponseClient()
                    .subscribe(playerAverageModel -> {
                            },
                            throwable -> Log.d("TAG", throwable.toString()),
                            () -> {
                                Log.d("TAG", "OnComplete - List<PlayerAverageModel> size: "
                                        + viewModel.getPlayerAverageModelList().size());
                                displayMenuFragment();
                            });
        }else{
            displayMenuFragment();
            Log.d("danny: database check",
                    "database is populated with players to size of "
                    + viewModel.getPlayerAverageModelList().size());
        }
    }

    @Override
    public void displayMenuFragment() {
        getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.main_container, MenuFragment.newInstance())
          .commit();
    }

    @Override
    public void displayGameFragment() {
        getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.main_container, GameFragment.newInstance(), "game")
          .addToBackStack(null)
          .commit();
    }

    @Override
    public void displayResultFragment(int playerCorrectGuesses, int playerIncorrectGuesses) {
        getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.main_container, ResultFragment.newInstance(playerCorrectGuesses, playerIncorrectGuesses))
          .commit();
    }

}