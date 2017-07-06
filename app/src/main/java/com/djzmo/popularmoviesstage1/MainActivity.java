package com.djzmo.popularmoviesstage1;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;
    private Button mErrorTryAgainButton;
    private Menu mMenu;
    private MovieAdapter mAdapter;
    private String mCurrentDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.pop_movies);

        mCurrentDisplay = "popular";

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mAdapter = new MovieAdapter(this, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_movies);
        mErrorTextView = (TextView) findViewById(R.id.tv_movie_error);
        mErrorTryAgainButton = (Button) findViewById(R.id.btn_try_again);

        mErrorTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovieData(mCurrentDisplay);
            }
        });

        if(NetworkUtils.API_KEY.equals("YOUR_API_KEY"))
            showErrorMessage(getString(R.string.api_key_not_set), false);
        else loadMovieData(mCurrentDisplay);
    }

    private void loadMovieData(String type) {
        showLoadingIndicator();
        new FetchMovieTask().execute(type);
    }

    private void showMovieDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mErrorTextView.setText(R.string.error_message);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message, boolean withButton) {
        mErrorTextView.setText(message);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(withButton ? View.VISIBLE : View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void showLoadingIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(MovieInformation information) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, information.remoteId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        int mostPopularId = R.id.menuitem_sort_most_popular;
        int topRatedId = R.id.menuitem_sort_top_rated;

        MenuItem mostPopular = mMenu.findItem(mostPopularId);
        MenuItem topRated = mMenu.findItem(topRatedId);

        boolean dirty = false;

        if(id == mostPopularId && !mCurrentDisplay.equals("popular"))
        {
            mostPopular.setCheckable(true);
            mostPopular.setChecked(true);
            topRated.setCheckable(false);
            topRated.setChecked(false);
            mCurrentDisplay = "popular";
            dirty = true;
        }
        else if(id == topRatedId && !mCurrentDisplay.equals("top_rated"))
        {
            topRated.setChecked(true);
            topRated.setCheckable(true);
            mostPopular.setChecked(false);
            mostPopular.setCheckable(false);
            mCurrentDisplay = "top_rated";
            dirty = true;
        }

        if(dirty)
            loadMovieData(mCurrentDisplay);

        return super.onOptionsItemSelected(item);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, MovieInformation[]> {

        boolean mNoConnection;

        public FetchMovieTask() {
            mNoConnection = false;
        }

        @Override
        protected MovieInformation[] doInBackground(String... strings) {
            if(!NetworkUtils.isOnline(MainActivity.this))
            {
                mNoConnection = true;
                return null;
            }

            String type = "popular";

            if(strings.length > 0)
                type = strings[0];

            if(!type.equals("popular") && !type.equals("top_rated"))
                type = "popular";

            URL url = type.equals("popular") ? NetworkUtils.getPopularUrl() : NetworkUtils.getTopRatedUrl();
            MovieInformation[] movieInformation = null;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                String jsonString = CommonUtils.readInputStream(urlConnection.getInputStream());

                JSONObject root = new JSONObject(jsonString);
                JSONArray results = root.getJSONArray("results");

                int itemCount = results.length();
                movieInformation = new MovieInformation[itemCount];

                for(int i = 0; i < itemCount; ++i) {
                    JSONObject data = results.getJSONObject(i);

                    MovieInformation movie = new MovieInformation();
                    movie.title = data.getString("original_title");
                    movie.synopsis = data.getString("overview");
                    movie.remoteId = String.valueOf(data.getInt("id"));
                    movie.userRating = data.getDouble("vote_average");
                    movie.releaseDate = data.getString("release_date");
                    movie.posterUrl = NetworkUtils.THUMBNAIL_BASE_URL + "/w185" + data.getString("poster_path");
                    movieInformation[i] = movie;
                }
            }
            catch(IOException | JSONException e) {
                e.printStackTrace();
            }

            return movieInformation;
        }

        @Override
        protected void onPostExecute(MovieInformation[] data) {
            if(data != null) {
                showMovieDataView();
                mAdapter.setData(data);
            }
            else if(mNoConnection) showErrorMessage(getString(R.string.no_internet_connection), true);
            else showErrorMessage();
        }
    }
}
