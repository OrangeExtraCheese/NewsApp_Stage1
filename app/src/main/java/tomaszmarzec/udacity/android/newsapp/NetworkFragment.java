package tomaszmarzec.udacity.android.newsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/* Idea to encapsulate logic of networking from UI logic in separate thread briefly based on Android
   Developer training - https://developer.android.com/training/basics/network-ops/connecting */
public class NetworkFragment extends Fragment implements LoaderManager.LoaderCallbacks<List>
{
    private static final String TAG = "Network Fragment";
    // If network connection is restored, new request is made with last used query URL.
    private String mLastUrlString;
    private LoaderManager mLoaderManager;
    private static FetchNewsCallback mCallback;
    private String mUrlApiKey = "b30e70e6-cce9-426a-a4fa-cdfffc23ff84";
    /* mRequireFetch is true before any successful fetch of news. It indicates that there are no
    fetched news to be displayed to user */
    private boolean mRequireFetch = true;

    public static NetworkFragment getInstance(FragmentManager fragmentManager)
    {
        NetworkFragment networkFragment = new NetworkFragment();

        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mLastUrlString = prepareNoQueryUrl();

    }

    @Override
    public void onAttach(Context context)
    {


        super.onAttach(context);
        mCallback = (FetchNewsCallback) context;

        mLoaderManager = getLoaderManager();

        context.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        String tagsUrl = "http://content.guardianapis.com/sections?&api-key=b30e70e6-cce9-426a-a4fa-cdfffc23ff84";
        Bundle bundle = new Bundle();
        bundle.putString("url", tagsUrl);

        mLoaderManager.initLoader(1, bundle, this);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        mCallback = null;
        getContext().unregisterReceiver(networkStateReceiver);
    }

    /*  Loader with id 0 is used to fetch news from Guardian API. With id 1 to fetch names of all
            sections to app. */
    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle bundle)
    {


        if(id==1)
        {
            return new SectionsLoader(getActivity(), bundle.getString("url"));
        }
        else
        {
            return new NewsLoader(getActivity(), bundle.getString("url"));
        }
    }



    @Override
    public void onLoadFinished(@NonNull Loader<List> loader, List results)
    {
        /*When activity is destroyed, fragment remains and is reused by FragmentManager when activity is
          recreated. The problem is that when for example app is resumed, LoaderCallbacks.onLoadFinished()
          is called again, which results in unnecessary query and data duplication. To avoid this,
          mRequireFetch variable is used to indicate if query is necessary or not*/
        if(mRequireFetch)
        {
                    /* Communication with MainActivity is made by calling methods of FetchNewsCallback interface,
            implemented in MainActivity. Results of query to Guardian server are passed to them. */

            if(loader.getId()==0)
            {
                if(results!=null && results.size() != 0)
                {
                    mCallback.onFetchedNews(results);

                /* If news has been fetched, in case of lack of Internet connection, there is no need
                   to show empty view wih "No Internet connection". */
                    mRequireFetch = false;
                }
                else
                {
                    mCallback.onFoundNothing();
                }
            }

            if(loader.getId()==1)
            {
                if(results!=null)
                {
                    fetchSections(results);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List> loader) {

    }

    /* Solution how to handle event in case of network connectivity change from:
    https://stackoverflow.com/questions/6169059/android-event-for-internet-connectivity-state-change
     by user Xmister */

    private BroadcastReceiver networkStateReceiver=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(checkConnection())
            {
                fetchNews(mLastUrlString);
            }
            else if(mRequireFetch)
            {
                mCallback.onNoNetworkConnection();
            }
        }
    };

    private boolean checkConnection()
    {
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
    }

    public void handleSearchQuery(String queryString)
    {
        if(queryString.equals(""))
        {
            fetchNews(prepareNoQueryUrl());
        }
        else
        {
            fetchNews(prepareUrl(queryString));
        }
    }

    private void fetchNews(String url)
    {
        mLastUrlString = url;
        mRequireFetch = true;

        if(checkConnection())
        {
            Bundle bundle = new Bundle();
            bundle.putString("url", url);

            if(mLoaderManager.getLoader(0)==null)
            {
                mLoaderManager.initLoader(0, bundle, this);
            }
            else
                mLoaderManager.restartLoader(0, bundle, this);
        }
        else
            mCallback.onNoNetworkConnection();
    }

    private String prepareNoQueryUrl()
    {
        String defaultUrlString = "https://content.guardianapis.com/search?&show-tags=contributor";
        Uri.Builder uriBuilder = Uri.parse(defaultUrlString).buildUpon();
        uriBuilder.appendQueryParameter("api-key", mUrlApiKey);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String maxNews = sharedPrefs.getString(getString(R.string.settings_max_news_key),
                getString(R.string.settings_max_news_default));
        String orderBy = sharedPrefs.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        uriBuilder.appendQueryParameter("page-size", maxNews);
        uriBuilder.appendQueryParameter("order-by", orderBy);

        return uriBuilder.toString();
    }

    private String prepareUrl(String queryString)
    {
        String basicUrlString = "https://content.guardianapis.com/search?";
        Bundle bundle = extractSections(queryString);
        // This var contains words which user inserted to search, not being tags
        String queryWords = bundle.getString("noTags");
        // This var contains words which user inserted to search, being only tags
        String querySections = bundle.getString("tags");

        Uri.Builder uriBuilder = Uri.parse(basicUrlString).buildUpon();
        uriBuilder.appendQueryParameter("q", queryWords);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("api-key", mUrlApiKey);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String maxNews = sharedPrefs.getString(getString(R.string.settings_max_news_key),
                getString(R.string.settings_max_news_default));
        String orderBy = sharedPrefs.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        uriBuilder.appendQueryParameter("page-size", maxNews);
        uriBuilder.appendQueryParameter("order-by", orderBy);

        if(!TextUtils.isEmpty(querySections))
        {
            uriBuilder.appendQueryParameter("section", querySections);
        }

        return uriBuilder.toString();
    }

    //This method separates sections (represented as tags in UI) from rest of search query words
    public Bundle extractSections(String string)
    {
        StringBuilder builder = new StringBuilder(string);
        StringBuilder tagsBuilder = new StringBuilder();

        int tagStart;

        while (true)
        {
            //If there are no tags left, loop will exit
            if(builder.indexOf("#")==-1)
            {
                break;
            }

            tagStart = builder.indexOf("#");

            /* If next tag or word is separated by space/spaces, for example #tag1 #tag2 or #tag1
               SearchedWord #tag2 */
            if(builder.indexOf(" ", tagStart+1) < builder.indexOf("#", tagStart+1) &&
                    builder.indexOf(" ", tagStart+1) != -1)
            {
                tagsBuilder.append(builder.substring(tagStart+1, builder.indexOf(" ", tagStart)) + "|");
                builder.replace(tagStart, builder.indexOf(" ", tagStart+1)+1, "");
            }
            // If next tag is not separated by space, for example #tag1#tag2
            else if(builder.indexOf("#", tagStart+1) != -1)
            {
                tagsBuilder.append(builder.substring(tagStart+1, builder.indexOf("#",
                        tagStart+1))+"|");
                builder.replace(tagStart, builder.indexOf("#", tagStart+1), "");
            }
            /* If there is no next tag (logical consequence of previous if statement being false)
               but there is space separating next world */
           /* else if(builder.indexOf(" ", tagStart+1) != -1)
            {
                tagsBuilder.append(builder.substring(tagStart+1, builder.indexOf(" ", tagStart))+"|");
                builder.replace(tagStart, builder.indexOf(" ", tagStart+1), "");
            }*/
            else //If tag is the last word in query
            {
                tagsBuilder.append(builder.substring(tagStart+1));
                builder.replace(tagStart, builder.length(), "");
            }
            /* All sections are separated by |, which means OR logical operator in query URL. Every
               article belongs to one section, so user can use many section names to get articles
               belonging to one of inserted sections*/
        }

        Bundle bundle = new Bundle();
        bundle.putString("tags", tagsBuilder.toString());
        bundle.putString("noTags", builder.toString());

        return bundle;
    }

    /* Following method is called from onLoadFinished, when Loader downloads section names.It formats
      section names before passing them to MainActivity to display.*/
    public void fetchSections(List<String> sectionsList)
    {
        StringBuilder builderSection = new StringBuilder();
        String[] sectionsArray = sectionsList.toArray(new String[0]);

        // Add "#" before every String in array
        for(int i = 0; i < sectionsArray.length; i++)
        {
            builderSection.setLength(0); // Clear StringBuilder buffer
            builderSection.append(sectionsArray[i]).insert(0, "#");
            sectionsArray[i] = builderSection.toString();
        }
        mCallback.onFetchedSections(sectionsArray);
    }
}
