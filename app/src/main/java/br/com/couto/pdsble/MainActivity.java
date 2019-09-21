package br.com.couto.pdsble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.os.Bundle;

import br.com.couto.pdsble.databinding.ActivityMainBinding;
import br.com.couto.pdsble.view_model.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.getPermission.observe(this, string ->{
            if(string.equals(MainViewModel.PERMISSION)){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        });
        binding.setViewModel(mViewModel);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
