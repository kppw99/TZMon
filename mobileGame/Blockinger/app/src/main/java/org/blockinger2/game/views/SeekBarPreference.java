package org.blockinger2.game.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.blockinger2.game.R;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener
{
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;
    private static final int DEFAULT_VALUE = 50;

    private int currentValue;
    private SeekBar seekbar;
    private TextView statusText;

    public SeekBarPreference(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);

        initPreference(context, attributeSet);
    }

    public SeekBarPreference(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);

        initPreference(context, attributeSet);
    }

    private void initPreference(Context context, AttributeSet attributeSet)
    {
        seekbar = new SeekBar(context, attributeSet);
        seekbar.setMax(MAX_VALUE);
        seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected View onCreateView(ViewGroup parent)
    {
        super.onCreateView(parent);

        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return mInflater.inflate(R.layout.preference_seekbar, parent, false);
    }

    @Override
    public void onBindView(View view)
    {
        super.onBindView(view);

        // Move our seekbar to the new view we've been given
        ViewParent oldContainer = seekbar.getParent();
        ViewGroup newContainer = view.findViewById(R.id.preference_seekbar_container);

        if (oldContainer != newContainer) {
            // Remove the seekbar from the old view
            if (oldContainer != null) {
                ((ViewGroup) oldContainer).removeView(seekbar);
            }

            // Remove the existing seekbar (there may not be one) and add ours
            newContainer.removeAllViews();
            newContainer.addView(seekbar, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Update a SeekBarPreference view with our current state
        statusText = view.findViewById(R.id.preference_seekbar_value);
        statusText.setText(String.valueOf(currentValue) + "%");
        statusText.setMinimumWidth(30);

        seekbar.setProgress(currentValue - MIN_VALUE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        int newValue = progress + MIN_VALUE;

        if (newValue > MAX_VALUE) {
            newValue = MAX_VALUE;
        } else if (newValue < MIN_VALUE) {
            newValue = MIN_VALUE;
        }

        // Change rejected, revert to the previous value
        if (!callChangeListener(newValue)) {
            seekBar.setProgress(currentValue - MIN_VALUE);

            return;
        }

        // Change accepted, store it
        currentValue = newValue;
        statusText.setText(String.valueOf(newValue) + "%");
        persistInt(newValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        //
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray typedArray, int index)
    {
        return typedArray.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            currentValue = getPersistedInt(currentValue);
        } else {
            currentValue = (int) defaultValue;
        }
    }
}
