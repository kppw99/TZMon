package org.blockinger2.game.database;

public class Score
{
    private long id;
    private long score;
    private String playerName;

    public long getId()
    {
        return id;
    }

    public long getScore()
    {
        return score;
    }

    public String getName()
    {
        return playerName;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setScore(long comment)
    {
        this.score = comment;
    }

    public void setName(String comment)
    {
        this.playerName = comment;
    }
}
