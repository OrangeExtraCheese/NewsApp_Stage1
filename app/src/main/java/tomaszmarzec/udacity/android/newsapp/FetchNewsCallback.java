package tomaszmarzec.udacity.android.newsapp;

import java.util.List;

// Following methods are implemented in MainActivity and called from NetworkFragment.
public interface FetchNewsCallback
{
     void onFetchedSections(String[] sectionsArray);
     void onFetchedNews(List<News> newsList);
     void onFoundNothing();
     void onNoNetworkConnection();
}

