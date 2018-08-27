package tomaszmarzec.udacity.android.newsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements FetchNewsCallback
{
    private NetworkFragment mNetworkFragment;
    private NewsAdapter mAdapter;
    private List<News> mNewsArray = new ArrayList<>();

    @BindView(R.id.recycler) protected RecyclerView recyclerView;
    @BindView(R.id.empty_view) protected TextView emptyView;
    @BindView(R.id.indeterminate_loading_indicator) protected ProgressBar loadingIndicator;
    @BindView(R.id.input_text) protected MultiAutoCompleteTextView inputView;

    @OnClick(R.id.image_icon_search)
    protected void search()
    {
        mNetworkFragment.handleSearchQuery(inputView.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mNetworkFragment = NetworkFragment.getInstance(this.getSupportFragmentManager());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager((getApplicationContext()));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new NewsAdapter((ArrayList<News>) mNewsArray);
        recyclerView.setAdapter(mAdapter);
        //Unfortunately, divider is not visible
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.HORIZONTAL));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implementation of FetchNewsCallback interface
    @Override
    public void onFetchedNews(List<News> newsList)
    {
        mNewsArray.clear();
        mNewsArray.addAll(newsList);
        loadingIndicator.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFetchedSections(String[] sectionsArray)
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, sectionsArray);

        inputView.setAdapter(adapter);
        inputView.setTokenizer(new SpaceTokenizer());
    }

    @Override
    public void onNoNetworkConnection()
    {
        clearRecycler();
        loadingIndicator.setVisibility(View.GONE);
        emptyView.setText(R.string.main_activity_no_connection);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFoundNothing()
    {
        clearRecycler();
        emptyView.setText(R.string.main_activity_no_results);
        emptyView.setVisibility(View.VISIBLE);
    }


    private void clearRecycler()
    {
        mNewsArray.clear();
        mAdapter.notifyDataSetChanged();
    }

}
