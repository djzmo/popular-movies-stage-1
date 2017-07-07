package com.djzmo.popularmoviesstage1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private MovieInformation[] mMovieData;
    private final Context mContext;
    final private MovieAdapterOnClickHandler mOnClickHandler;

    public MovieAdapter(Context c, MovieAdapterOnClickHandler onClickHandler) {
        mContext = c;
        mOnClickHandler = onClickHandler;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_list_item, parent, false);
        return new MovieViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.bind(mMovieData[position]);
    }

    @Override
    public int getItemCount() {
        return mMovieData == null ? 0 : mMovieData.length;
    }

    public void setData(MovieInformation[] data) {
        mMovieData = data;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final Context mParentContext;
        private final ImageView mThumbnail;

        public MovieViewHolder(Context c, View itemView) {
            super(itemView);
            mParentContext = c;
            mThumbnail = (ImageView) itemView.findViewById(R.id.iv_movie_thumbnail);
            itemView.setOnClickListener(this);
        }

        public void bind(MovieInformation movie) {
            Picasso.with(mParentContext).load(movie.posterUrl).into(mThumbnail);
        }

        @Override
        public void onClick(View view) {
            mOnClickHandler.onClick(mMovieData[getAdapterPosition()]);
        }
    }

    interface MovieAdapterOnClickHandler {
        void onClick(MovieInformation information);
    }
}
