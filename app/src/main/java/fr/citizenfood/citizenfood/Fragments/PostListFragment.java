package fr.citizenfood.citizenfood.Fragments;

/**
 * Created by William on 11/03/2018.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import fr.citizenfood.citizenfood.Activities.LoginActivity;
import fr.citizenfood.citizenfood.Activities.MainActivity;
import fr.citizenfood.citizenfood.Activities.PostDetailActivity;
import fr.citizenfood.citizenfood.Model.Post;
import fr.citizenfood.citizenfood.R;
import fr.citizenfood.citizenfood.ViewHolder.PostViewHolder;
import fr.citizenfood.citizenfood.database.InternalStockage;
import fr.citizenfood.citizenfood.database.Votes;


public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    private SQLiteDatabase database;
    private InternalStockage dbHelper;
    private String[] allColumns = { InternalStockage.COLUMN_ID,
            InternalStockage.COLUMN_VOTESTATE, InternalStockage.COLUMN_AUTHOR, InternalStockage.COLUMN_UID };

    private String like_uid = null;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public PostListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);
        dbHelper = new InternalStockage(getContext());
        return rootView;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = new InternalStockage(getContext());
        database = dbHelper.getWritableDatabase();



        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(PostViewHolder viewHolder, int position, final Post model) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.stars.containsKey(getUid()) && !LoginActivity.session.getVoteState()) {
                    viewHolder.starView.setImageResource(R.drawable.like_valid);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.like_unvalid);
                }

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
                        like_uid = model.uid;

                        // Run two transactions
                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private void vote(DatabaseReference postRef)
    {

        postRef.runTransaction(new Transaction.Handler()
        {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData)
            {
                Post p = mutableData.getValue(Post.class);
                if(!p.author.equalsIgnoreCase(LoginActivity.session.getUserLogin()) )
                {

                    if (p == null)
                    {
                        return Transaction.success(mutableData);
                    }
                    Votes v = dbHelper.getVotes(LoginActivity.session.getUserLogin());
                    if (p.stars.containsKey(getUid()) && v != null )
                    { // l'utilisateur enleve son vote si, il a deja voté et que le post contient un vote
                        Log.d(TAG, "doTransaction() ***************** IF *********************** ");
                        // Unstar the post and remove self from stars
                        p.starCount = p.starCount - 1;
                        p.stars.remove(getUid());
                        LoginActivity.session.setVoteState(false);
                        LoginActivity.session.setUidItem("");
                        boolean del = dbHelper.deleteVotes(getUid());
                        Log.d(TAG, "doTransaction() ******** VOTE DELETE TO THE LOCAL DATABASE ********* => [" + del + "]");

                    }
                    else if( !p.stars.containsKey(getUid()) && v != null ) // Si le post ne cotient pas de vote de cet utilisateur et qu'il a deja voté
                    {
                        // ON FAIT RIEN
                        //Log.d(TAG, "doTransaction() ***************** USER ALREADY VOTE ANOTHER TO POST *********************** ");

                    }
                    else {// Si l'utilisateur n'a pas encore voté

                        Log.d(TAG, "doTransaction() ***************** ELSE *********************** ");
                        // Star the post and add self to stars
                        p.starCount = p.starCount + 1;
                        p.stars.put(getUid(), true);
                        Votes v1 = new Votes();
                        v1.setVoteState("1");
                        v1.setUid_vote(getUid());
                        v1.setVoteAuthor(LoginActivity.session.getUserLogin());
                        long id = dbHelper.insertVotes(v1);
                        Log.d(TAG, "doTransaction()  ********** VOTE's ID ADDED ********** => [" + id + "]");

                    }
                    // Set value and report transaction success
                    mutableData.setValue(p);

                }
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference postRef)
    {
        //Toast.makeText(this.getContext(), "Bienvenue à citizenFood "+ LoginActivity.session.getUserLogin(),    Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStarClicked() called with: Etat vote = [" + LoginActivity.session.getVoteState() + "]");

        Log.d(TAG, "onStarClicked() called with: uidCurrentItem = [" + like_uid + "]");
        Log.d(TAG, "onStarClicked() called with: session uid = [" + LoginActivity.session.getUidItem() + "]");
        vote(postRef);

    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }









    // [END post_stars_transaction]


    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
