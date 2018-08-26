package tomaszmarzec.udacity.android.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class NewsQuery
{
    private final static String LOG_TAG = NewsQuery.class.getName();

    public static List<News> fetchNews(String urlString)
    {
        String jsonString = makeHttpRequest(prepareUrl(urlString));
        return extractJsonNews(jsonString);
    }

    public static List<String> fetchTags(String urlString)
    {
        String jsonString = makeHttpRequest(prepareUrl(urlString));
        return extractJsonSections(jsonString);
    }

    private static URL prepareUrl(String urlString)
    {
        URL url = null;

        try{
            url = new URL(urlString);
        }
        catch (MalformedURLException e){
            Log.e(LOG_TAG, "Bad URL - " + e);
        }

        return url;
    }

    private static String makeHttpRequest(URL url)
    {
        String jsonResponse = null;
        if(url==null)
            return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if(urlConnection.getResponseCode()==200)
            {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        }
        catch (IOException e){
            Log.e(LOG_TAG, "HTTP connection problem - " + e);
        }
        finally {
            if(urlConnection != null)
                urlConnection.disconnect();

            if(inputStream!=null)
                try{
                    inputStream.close();
                }
                catch (IOException e){
                    Log.e(LOG_TAG, "Error closing input stream - " +e);
                }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException
    {
        StringBuilder jsonResponse = new StringBuilder();

        if(inputStream!=null)
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader buffReader = new BufferedReader(inputStreamReader);

            String line = buffReader.readLine();
            while (line!=null)
            {
                jsonResponse.append(line);
                line = buffReader.readLine();
            }
        }
        return jsonResponse.toString();
    }

    private static List<News> extractJsonNews(String jsonString)
    {
        if(TextUtils.isEmpty(jsonString))
            return null;

        List<News> newsArray = new ArrayList<>();

        try{
            JSONArray jsonResultsArray = new JSONObject(jsonString).getJSONObject("response").
                    getJSONArray("results");

            for(int i = 0; i<jsonResultsArray.length(); i++)
            {
                JSONObject jsonNews = jsonResultsArray.getJSONObject(i);
                String webTitle = jsonNews.getString("webTitle");
                String sectionName = jsonNews.getString("sectionName");
                String date = jsonNews.getString("webPublicationDate");
                String webUrl = jsonNews.getString("webUrl");

                List<String> arrayAuthors = new ArrayList<>();
                List<String> arrayAuthorUrls = new ArrayList<>();


                JSONArray jsonTagsArray = jsonNews.getJSONArray("tags");
                if(jsonTagsArray.length()>0)
                {
                    for(int j = 0; j<jsonTagsArray.length(); j++)
                    {
                        JSONObject contributor = jsonTagsArray.getJSONObject(j);
                        arrayAuthors.add(contributor.getString("webTitle"));
                        arrayAuthorUrls.add(contributor.getString("webUrl"));
                    }
                }
                else
                {
                    arrayAuthors.add("unknown");
                    arrayAuthorUrls.add("none");
                }

                newsArray.add(new News(webTitle, sectionName, arrayAuthors, arrayAuthorUrls, webUrl, date));
            }
        }
        catch (JSONException e){
            Log.e(LOG_TAG, "Error parsing JSON - " + e);
        }

        return newsArray;
    }

    private static List<String> extractJsonSections(String jsonString)
    {
        if(TextUtils.isEmpty(jsonString))
            return null;

        List<String> sectionsArray = new ArrayList<>();

        try{
            JSONArray resultsArray = new JSONObject(jsonString).getJSONObject("response").
                    getJSONArray("results");

            for(int i = 0; i<resultsArray.length(); i++)
            {
               JSONObject result = resultsArray.getJSONObject(i);

               if(result.has("id"))
               {
                   sectionsArray.add(result.getString("id"));
               }
            }
        }
        catch (JSONException e){
            Log.e(LOG_TAG, "Error parsing JSON - " + e);
        }

        return sectionsArray;
    }
}

