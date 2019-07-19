package org.blockinger2.game.components;

import org.blockinger2.game.activities.GameActivity;

public abstract class Component
{
    GameActivity host;

    Component(GameActivity gameActivity)
    {
        host = gameActivity;
    }

    public void reconnect(GameActivity gameActivity)
    {
        host = gameActivity;
    }

    public void disconnect()
    {
        host = null;
    }
}
