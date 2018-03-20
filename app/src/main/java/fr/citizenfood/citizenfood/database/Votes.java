package fr.citizenfood.citizenfood.database;

/**
 * Created by eliam on 14/03/2018.
 */

public class Votes {
    private long id ;
    private String vote_state;
    private String vote_author;
    private String uid_vote;



    public void setUid_vote( String p_uid_vote )
    {
        this.uid_vote = p_uid_vote;
    }

    public String getUid_vote()
    {
        return this.uid_vote;
    }

    public void setVoteState( String state )
    {
        this.vote_state = state;
    }

    public String getVoteAuthor()
    {
        return this.vote_author;
    }

    public String getVoteState()
    {
        return this.vote_state;
    }

    public void setVoteId( long p_voteId )
    {
        this.id = p_voteId;
    }
    public void setVoteAuthor( String p_author )
    {
        this.vote_author = p_author;
    }

    public long getVoteId()
    {
        return this.id;
    }



}
