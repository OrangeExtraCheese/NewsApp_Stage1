package tomaszmarzec.udacity.android.newsapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class News
{
    private String mTitle, mSection, mWebUrl, mDate;
    private List<String> mArrayAuthors;
    private List<String> mArrayAuthorsUrl;

    public News(String title, String section, List<String> arrayAuthors,
                List<String> arrayAuthorsUrl, String webUrl, String date)
    {
        mTitle = title;
        mSection = section;
        mArrayAuthors = arrayAuthors;
        mArrayAuthorsUrl = arrayAuthorsUrl;
        mWebUrl = webUrl;
        mDate = date;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSection() {
        return mSection;
    }

    public List<String> getArrayAuthors() {
        return mArrayAuthors;
    }

    public List<String> getArrayAuthorsUrl() {
        return mArrayAuthorsUrl;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public String getDate() {
        return mDate;
    }
}
