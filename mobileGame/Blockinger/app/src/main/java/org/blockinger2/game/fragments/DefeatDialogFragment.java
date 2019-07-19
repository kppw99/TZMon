package org.blockinger2.game.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.blockinger2.game.R;
import org.blockinger2.game.activities.GameActivity;

import java.util.Objects;

public class DefeatDialogFragment extends DialogFragment
{
    private CharSequence scoreString;
    private CharSequence timeString;
    private CharSequence apmString;
    private long score;

    public DefeatDialogFragment()
    {
        super();

        scoreString = "unknown";
        timeString = "unknown";
        apmString = "unknown";
    }

    public void setData(long scoreArg, String time, int apm)
    {
        scoreString = String.valueOf(scoreArg);
        timeString = time;
        apmString = String.valueOf(apm);
        score = scoreArg;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstance)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.game_over);
        builder.setMessage(
            getResources().getString(R.string.score_label) +
                "\n    " + scoreString + "\n\n" +
                getResources().getString(R.string.time_label) +
                "\n    " + timeString + "\n\n" +
                getResources().getString(R.string.apm_label) +
                "\n    " + apmString
        );

        builder.setNeutralButton(R.string.back, (dialog, which) -> ((GameActivity) Objects.requireNonNull(getActivity())).putScore(score));

        return builder.create();
    }
}
