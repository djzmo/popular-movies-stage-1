package com.djzmo.popularmoviesstage1;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieDetailActivity extends AppCompatActivity {

    private ScrollView mDetailContent;
    private TextView mErrorTextView;
    private Button mErrorTryAgainButton;
    private ProgressBar mLoadingIndicator;
    private String mRemoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        setTitle(R.string.movie_detail);

        mDetailContent = (ScrollView) findViewById(R.id.sv_detail_content);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_detail);
        mErrorTextView = (TextView) findViewById(R.id.tv_detail_error);
        mErrorTryAgainButton = (Button) findViewById(R.id.btn_try_again);

        showLoadingIndicator();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mErrorTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDetail(mRemoteId);
            }
        });

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)) {
            mRemoteId = intent.getStringExtra(Intent.EXTRA_TEXT);
            loadDetail(mRemoteId);
        }
        else {
            Toast.makeText(this, R.string.please_supply_id, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fillViews(MovieInformation data) {
        TextView title = (TextView) findViewById(R.id.tv_detail_title);
        TextView synopsis = (TextView) findViewById(R.id.tv_detail_synopsis);
        TextView userRating = (TextView) findViewById(R.id.tv_detail_rating);
        TextView year = (TextView) findViewById(R.id.tv_detail_year);
        TextView runtime = (TextView) findViewById(R.id.tv_detail_duration);
        ImageView poster = (ImageView) findViewById(R.id.iv_detail_thumbnail);

        Picasso.with(this).load(data.posterUrl).into(poster);
        title.setText(data.title);
        synopsis.setText(data.synopsis);
        userRating.setText(String.format(getString(R.string.rating_formatted), String.valueOf(data.userRating)));
        year.setText(data.releaseDate.substring(0, data.releaseDate.indexOf('-')));
        runtime.setText(String.format(getString(R.string.runtime_formatted), String.valueOf(data.runtime)));
    }

    private void loadDetail(String id) {
        showLoadingIndicator();
        new FetchDetailsTask().execute(id);
    }

    private void showDetailContentView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mDetailContent.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mErrorTextView.setText(R.string.error_message);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mDetailContent.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message, boolean withButton) {
        mErrorTextView.setText(message);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mDetailContent.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(withButton ? View.VISIBLE : View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void showLoadingIndicator() {
        mDetailContent.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchDetailsTask extends AsyncTask<String, Void, MovieInformation> {

        private boolean mNoConnection;

        public FetchDetailsTask() {
            mNoConnection = false;
        }

        @Override
        protected MovieInformation doInBackground(String... strings) {
            if(!NetworkUtils.isOnline(MovieDetailActivity.this) || strings.length == 0)
            {
                mNoConnection = true;
                return null;
            }

            String id = strings[0];
            URL url = NetworkUtils.getMovieDetailUrl(id);
            MovieInformation movie = new MovieInformation();

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                String jsonString = CommonUtils.readInputStream(urlConnection.getInputStream());

                JSONObject data = new JSONObject(jsonString);
                movie.title = data.getString("original_title");
                movie.synopsis = data.getString("overview");
                movie.remoteId = String.valueOf(data.getInt("id"));
                movie.userRating = data.getDouble("vote_average");
                movie.releaseDate = data.getString("release_date");
                movie.posterUrl = NetworkUtils.THUMBNAIL_BASE_URL + "/w185" + data.getString("poster_path");
                movie.runtime = data.getInt("runtime");
            }
            catch(IOException | JSONException e) {
                e.printStackTrace();
            }

            return movie;
        }

        @Override
        protected void onPostExecute(MovieInformation data) {
            if(data != null)
            {
                showDetailContentView();
                fillViews(data);
            }
            else if(mNoConnection) showErrorMessage(getString(R.string.no_internet_connection), true);
            else showErrorMessage();
        }
    }
}
