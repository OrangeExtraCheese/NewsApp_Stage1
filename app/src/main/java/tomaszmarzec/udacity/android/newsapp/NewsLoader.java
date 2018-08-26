package tomaszmarzec.udacity.android.newsapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<News>>
{
    private String mUrlString;

    public NewsLoader(Context context, String urlString)
    {
        super(context);
        mUrlString = urlString;
    }

    @Override
    protected void onStartLoading()
    {
        super.onForceLoad();
    }

    @Nullable
    @Override
    public List<News> loadInBackground()
    {
        if(mUrlString==null)
        {
            return null;
        }

            return NewsQuery.fetchNews(mUrlString);
    }
}
