package com.alatheer.missing.Authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alatheer.missing.Countries.CountriesActivity;
import com.alatheer.missing.Data.Remote.GetDataService;
import com.alatheer.missing.Data.Remote.Model.Terms.Term;
import com.alatheer.missing.Data.Remote.RetrofitClientInstance;
import com.alatheer.missing.Helper.LocaleHelper;
import com.alatheer.missing.R;
import com.alatheer.missing.Utilities.Utilities;

import java.util.List;

public class LegalLetterActivity extends AppCompatActivity {
    String language;
    Context context;
    Resources resources;
    @BindView(R.id.txt_legal_letter)
    TextView txt_legal_letter;
    @BindView(R.id.terms_recycler)
    RecyclerView terms_recycler;
    RecyclerView.LayoutManager layoutManager;
    TermsAdapter termsAdapter;
    boolean active= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_letter);
        ButterKnife.bind(this);
        Paper.init(this);
        language = Paper.book().read("language");
        updateview(language);
        getterms();

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

    private void getterms() {
        if(Utilities.isNetworkAvailable(this)){
            GetDataService getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<List<Term>> call = getDataService.get_terms();
            call.enqueue(new Callback<List<Term>>() {
                @Override
                public void onResponse(Call<List<Term>> call, Response<List<Term>> response) {
                    if(response.isSuccessful()){
                        init_recyclerview(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<Term>> call, Throwable t) {

                }
            });
        }
    }

    private void init_recyclerview(List<Term> body) {
        termsAdapter = new TermsAdapter(this,body);
        layoutManager = new LinearLayoutManager(this);
        terms_recycler.setAdapter(termsAdapter);
        terms_recycler.setLayoutManager(layoutManager);
        terms_recycler.setHasFixedSize(true);

    }

    private void updateview(String language) {
        context = LocaleHelper.setLocale(this,language);
        resources = context.getResources();
        txt_legal_letter.setText(resources.getString(R.string.legalletter));
    }

    public void Back(View view) {
        onBackPressed();
    }
    private BroadcastReceiver mhandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");
            //message.setText(msg);

            if(active == true){
                Utilities.showNotificationInADialog(LegalLetterActivity.this,msg);
            }

        }
    };
}
