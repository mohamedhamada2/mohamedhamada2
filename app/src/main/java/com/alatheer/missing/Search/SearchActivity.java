package com.alatheer.missing.Search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alatheer.missing.Categories.Category;
import com.alatheer.missing.Comments.AddComment;
import com.alatheer.missing.Countries.CityModel;
import com.alatheer.missing.Countries.CountriesActivity;
import com.alatheer.missing.Countries.CountryModel;
import com.alatheer.missing.Data.Remote.GetDataService;
import com.alatheer.missing.Data.Remote.Model.Items.Item;
import com.alatheer.missing.Data.Remote.RetrofitClientInstance;
import com.alatheer.missing.Helper.LocaleHelper;
import com.alatheer.missing.R;
import com.alatheer.missing.Utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.category_recycler)
    RecyclerView category_recycler;
    @BindView(R.id.spinner_govern_type)
    Spinner spinner_govern_type;
    @BindView(R.id.spinner_city_type)
    Spinner spinner_city_type;
    @BindView(R.id.items_recycler)
    RecyclerView items_recycler;
    @BindView(R.id.btn_missing)
    Button btn_missing;
    @BindView(R.id.btn_existing)
    Button btn_existing;
    @BindView(R.id.txt_search)
    TextView txt_search;
    @BindView(R.id.txt_govern)
    TextView txt_govern;
    @BindView(R.id.txt_city)
    TextView txt_city;
    CategoryAdapter2 categoryAdapter2;
    RecyclerView.LayoutManager layoutManager;
    List<Category> categoryList;
    List<CountryModel>countryModelList;
    List<String> countrynamesList;
    List<CityModel>cityModelList;
    List<String> citynamesList;
    int govern_id = 1;
    int city_id;
    String type = "1";
    int category_id ;
    List<Item>itemList;
    ItemsAdapter itemsAdapter;
    String language;
    RecyclerView.LayoutManager itemlayoutManager;
    Context context;
    Resources resources;
    boolean active = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        Paper.init(this);
        language = Paper.book().read("language");
        if(language == null){
            Paper.book().write("language","ar");
        }

        updateview(language);
        countrynamesList = new ArrayList<>();
        citynamesList = new ArrayList<>();
        getCategories();
        getgoverns();
        LocalBroadcastManager.getInstance(this).registerReceiver(mhandler,new IntentFilter("com.alatheer.missing_FCM-MESSAGE"));
        spinner_govern_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                govern_id = countryModelList.get(i).getId();
                getcities();
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
            spinner_city_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        city_id = cityModelList.get(i).getId();
                        TextView textView = (TextView) view;
                        textView.setTextColor(getResources().getColor(R.color.colorPrimary));

                    }catch (Exception r){
                        TextView textView = (TextView) view;
                        textView.setVisibility(View.INVISIBLE);
                        citynamesList.clear();
                        Toast.makeText(SearchActivity.this, "لا يوحد مدينة", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    private void updateview(String language) {
        context = LocaleHelper.setLocale(this,language);
        resources = context.getResources();
        txt_search.setText(resources.getString(R.string.search));
        txt_city.setText(resources.getString(R.string.city));
        txt_govern.setText(resources.getString(R.string.choosegovern));
        et_name.setHint(resources.getString(R.string.search));
        btn_existing.setText(resources.getString(R.string.existing));
        btn_missing.setText(resources.getString(R.string.missing));
    }


    private void getgoverns() {
        GetDataService getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Call<List<CountryModel>> call = getDataService.get_governs();
        call.enqueue(new Callback<List<CountryModel>>() {
            @Override
            public void onResponse(Call<List<CountryModel>> call, Response<List<CountryModel>> response) {
                if(response.isSuccessful()){
                    countryModelList = response.body();
                    for(CountryModel countryModel : countryModelList){
                       countrynamesList.add(countryModel.getTitle());
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SearchActivity.this,R.layout.spinner_item2,countrynamesList);
                    spinner_govern_type.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<CountryModel>> call, Throwable t) {

            }
        });
    }
    private void getcities() {
        if(Utilities.isNetworkAvailable(this)){
            GetDataService getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<List<CityModel>> call = getDataService.get_cities(govern_id);
            call.enqueue(new Callback<List<CityModel>>() {
                @Override
                public void onResponse(Call<List<CityModel>> call, Response<List<CityModel>> response) {
                    if(response.isSuccessful()){
                        cityModelList = response.body();
                        citynamesList.clear();
                            for (CityModel cityModel : cityModelList){
                                citynamesList.add(cityModel.getTitle());
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SearchActivity.this,R.layout.spinner_item2,citynamesList);
                            spinner_city_type.setAdapter(arrayAdapter);
                    }
                }

                @Override
                public void onFailure(Call<List<CityModel>> call, Throwable t) {

                }
            });
        }

    }

    private void getCategories() {
        if(Utilities.isNetworkAvailable(this)){
            GetDataService getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<List<Category>> call = getDataService.get_categories();
            call.enqueue(new Callback<List<Category>>() {
                @Override
                public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                    if(response.isSuccessful()){
                     categoryList = response.body();
                     initrecycler();
                    }
                }

                @Override
                public void onFailure(Call<List<Category>> call, Throwable t) {

                }
            });
        }
    }

    private void initrecycler() {
        category_recycler.setHasFixedSize(true);
        categoryAdapter2 = new CategoryAdapter2(categoryList,this);
        category_recycler.setAdapter(categoryAdapter2);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        category_recycler.setLayoutManager(layoutManager);
    }

    public void Back(View view) {
        onBackPressed();
    }
    @OnClick(R.id.btn_missing)
    public void GetMissingItems(View view) {
        type = "1";
        btn_missing.setBackgroundResource(R.drawable.btn_background);
        btn_missing.setTextColor(getResources().getColor(R.color.white));
        btn_existing.setBackgroundResource(R.drawable.et_search_background);
        btn_existing.setTextColor(getResources().getColor(R.color.colorPrimary));
        if(Utilities.isNetworkAvailable(this)){
            GetDataService getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<List<Item>> call = getDataService.get_all_items(category_id+"",et_name.getText().toString(),type,govern_id+"",city_id+"");
            call.enqueue(new Callback<List<Item>>() {
                @Override
                public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                    if(response.isSuccessful()){
                        itemList = response.body();
                         initrecycleritem();
                    }
                }

                @Override
                public void onFailure(Call<List<Item>> call, Throwable t) {

                }
            });
        }
    }

    private void initrecycleritem() {
        items_recycler.setHasFixedSize(true);
        itemsAdapter = new ItemsAdapter(itemList,this);
        items_recycler.setAdapter(itemsAdapter);
        itemlayoutManager = new LinearLayoutManager(this);
        items_recycler.setLayoutManager(itemlayoutManager);
    }

    @OnClick(R.id.btn_existing)
    public void GetExistingItems(View view) {
        type = "2";
        btn_missing.setBackgroundResource(R.drawable.et_search_background);
        btn_missing.setTextColor(getResources().getColor(R.color.colorPrimary));
        btn_existing.setBackgroundResource(R.drawable.btn_background);
        btn_existing.setTextColor(getResources().getColor(R.color.white));
        if(Utilities.isNetworkAvailable(this)){
            GetDataService getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<List<Item>> call = getDataService.get_all_items(category_id+"",et_name.getText().toString(),type,govern_id+"",city_id+"");
            call.enqueue(new Callback<List<Item>>() {
                @Override
                public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                    if(response.isSuccessful()){
                        itemList = response.body();
                        initrecycleritem();
                    }
                }

                @Override
                public void onFailure(Call<List<Item>> call, Throwable t) {

                }
            });
        }
    }

    public void sendData(Integer id) {
        category_id = id;
    }

    public void add_comment(Integer id, Integer type, Integer userId, String img, String name,String city,String govern,String date) {
        Intent intent = new Intent(SearchActivity.this, AddComment.class);
        intent.putExtra("item_id",id);
        intent.putExtra("type",type);
        intent.putExtra("userId",userId);
        intent.putExtra("img",img);
        intent.putExtra("name",name);
        intent.putExtra("city",city);
        intent.putExtra("govern",govern);
        intent.putExtra("date",date);
        startActivity(intent);
    }
    private BroadcastReceiver mhandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");
            //message.setText(msg);
            if(active == true){
                Utilities.showNotificationInADialog(SearchActivity.this,msg);
            }

        }
    };
}
