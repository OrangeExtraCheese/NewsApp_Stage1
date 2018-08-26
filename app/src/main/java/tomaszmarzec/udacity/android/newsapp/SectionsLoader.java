package tomaszmarzec.udacity.android.newsapp;


import android.content.Context;

import java.util.List;

public class SectionsLoader extends android.support.v4.content.AsyncTaskLoader
{
    private String mUrl;

    public SectionsLoader(Context context, String url)
    {
        super(context);
        mUrl = url;

    }

    @Override
    protected void onStartLoading()
    {
        forceLoad();
    }

    @Override
    public List<String> loadInBackground()
    {
        return NewsQuery.fetchTags(mUrl);
    }
}
