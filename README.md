# MyBaseToolBar
一个通用的toolbar,可以设置左边返回键按钮，中间文字标题或搜索框，最右边设置文字图片
![效果图1](https://github.com/HQH2017/MyBaseToolBar/blob/master/img01.png)
![效果图2](https://github.com/HQH2017/MyBaseToolBar/blob/master/img02.png)
一些方法：
mToolBar.setBackButton(图片资源)  // 返回的图标
mToolBar.changeShowview(false); // true：中间显示搜索框, false: 显示文字标题
mToolBar.setTitle("标题");    
mToolBar.setTitleTextColor(Color.WHITE);  // 标题颜色
mToolBar.addLeftImage(R.mipmap.sback, new View.OnClickListener() { // 增加左边的按钮
            @Override
            public void onClick(View view) {

            }
        });
 mToolBar.addRightImage(R.mipmap.serma, new View.OnClickListener() { // 增加右边的按钮
            @Override
            public void onClick(View view) {

            }
        });
