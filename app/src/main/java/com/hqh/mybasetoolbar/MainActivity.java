package com.hqh.mybasetoolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hqh.mtoolbar.MToolBar;

public class MainActivity extends AppCompatActivity implements MToolBar.SearchViewListener{

    private MToolBar mToolBar;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolBar = findViewById(R.id.main_mtoolbar);

        mToolBar.changeShowview(false); // true：中间显示搜索框, false: 显示文字标题
        mToolBar.setBackgroundColor(ContextCompat.getColor(this,R.color.colorRed));
        // 搜索框
        // mToolBar.setShowEditPage(true); // true 当页面跳转
        // mToolBar.setSearchViewListener(this);
        // mToolBar.addRightSearchImage(R.drawable.search_icon);


        mToolBar.setTitle("标题");
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.addLeftImage(R.mipmap.sback, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mToolBar.addRightImage(R.mipmap.serma, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void onRefreshAutoComplete(String text) {

    }

    @Override
    public void onSearch(String text) {
        Toast.makeText(this,"=="+text, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onSearchClick() {
        Toast.makeText(this,"==pp", Toast.LENGTH_LONG).show();
    }
}
