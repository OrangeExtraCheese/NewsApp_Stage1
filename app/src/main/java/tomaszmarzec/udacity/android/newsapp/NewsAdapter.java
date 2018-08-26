package tomaszmarzec.udacity.android.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>
{
    private List<News> mNewsArray;

    public NewsAdapter(ArrayList<News> newsArray)
    {
        mNewsArray = newsArray;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView title, section, date, time;
        private LinearLayout linearLayoutAuthorsList;
        private ImageView imageIconArrow;
        private ConstraintLayout constraintRoot;


        public ViewHolder(View view)
        {
            super(view);

            constraintRoot = view.findViewById(R.id.constraint_root);
            imageIconArrow = view.findViewById(R.id.image_icon_arrow);
            title = view.findViewById(R.id.text_title);
            section = view.findViewById(R.id.text_section);
            date = view.findViewById(R.id.text_date);
            time = view.findViewById(R.id.text_time);
            linearLayoutAuthorsList = view.findViewById(R.id.linear_authors_list);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
    {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        return new ViewHolder(listItem);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position)
    {
        LinearLayout linearLayoutAuthorsList = viewHolder.linearLayoutAuthorsList;
        News news = mNewsArray.get(position);
        viewHolder.title.setText(news.getTitle());
        viewHolder.section.setText(news.getSection());
        viewHolder.date.setText(subtractDate(news.getDate()));
        viewHolder.time.setText(subtractTime(news.getDate()));
        Context context = linearLayoutAuthorsList.getContext();
        List<String> arrayAuthors = news.getArrayAuthors();
        List<String> arrayAuthorsUrl = news.getArrayAuthorsUrl();

        viewHolder.imageIconArrow.setOnClickListener(createArrowListener(linearLayoutAuthorsList));

        /* For every author, inflate list_item_author and set TextView text to author name, and add
            listener to button using author's url*/
        for(int i = 0; i<arrayAuthors.size(); i++)
        {
            ConstraintLayout constraintLayoutAuthorItem = (ConstraintLayout) LayoutInflater.
                    from(context).inflate(R.layout.list_item_author, null);

            TextView textViewAuthor = constraintLayoutAuthorItem.findViewById(R.id.text_author_name);
            textViewAuthor.setText(arrayAuthors.get(i));

            Button buttonWebsite = constraintLayoutAuthorItem.findViewById(R.id.button_website);
            buttonWebsite.setOnClickListener(createBrowseWebsiteListener(context, arrayAuthorsUrl.get(i)));

            linearLayoutAuthorsList.addView(constraintLayoutAuthorItem);

            viewHolder.constraintRoot.setOnClickListener(createBrowseWebsiteListener(context, news.getWebUrl()));
        }
    }

    @Override
    public int getItemCount()
    {
        return mNewsArray.size();
    }

    private String subtractDate(String date)
    {
        return date.substring(0, date.indexOf("T"));
    }

    private String subtractTime(String date)
    {
        return date.substring(date.indexOf("T")+1, date.lastIndexOf(":")) + " (UTC)";
    }

    @Override
    public int getItemViewType(int position)
    {
        return (position == mNewsArray.size()  ? R.layout.list_item : R.layout.list_item);
    }

    private View.OnClickListener createBrowseWebsiteListener(final Context context, final String url)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            }
        };
    }

    private View.OnClickListener createArrowListener( final LinearLayout authorsList)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ImageView img = (ImageView) v;
                if(authorsList.getVisibility()==View.GONE)
                {
                    authorsList.setVisibility(View.VISIBLE);
                    img.setImageResource(R.drawable.arrow_up);
                }
                else
                {
                    authorsList.setVisibility(View.GONE);
                    img.setImageResource(R.drawable.arrow_down);
                }
            }
        };
    }
}
