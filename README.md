## Android Pull To Refresh

* Support Pull Down To Refresh And Pull Up to Refresh. 

* 支持下拉＋上拉刷新。

## Download
Gradle:
```
compile 'com.biao:pulltorefresh:1.0.0-beta2'
```


##How to use!

1. Just use PtrLayout like FrameLayout!!

```
<com.biao.pulltorefresh.PtrLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</com.biao.pulltorefresh.PtrLayout>
```


2. set HeaderView and FooterView

```
public void setHeaderView(View view); 
public void setFooterView(View view);
```

ps: as a general rule, your header or footer view need impl PtrHandler to 

```
public interface PtrHandler {

    /** when refresh begin */
    void onRefreshBegin();

    /** when refresh end */
    void onRefreshEnd();

    /** when refresh pulling */
    void onPercent(float percent);
}

```

3. set Mode. As the name implies .

```
public void setMode(int mode);
PtrLayout.MODE_ALL_MOVE
PtrLayout.MODE_ONLY_CONTENT_NOT_MOVE
PtrLayout.MODE_ONLY_FOOTER_NOT_MOVE
PtrLayout.MODE_ONLY_HEADER_NOT_MOVE
PtrLayout.MODE_ONLY_CONTENT_MOVE
```

4. set refresh listener.  As the name implies .

```
public void setOnPullDownRefreshListener(OnRefreshListener onRefreshListener);
public void setOnPullUpRefreshListener(OnRefreshListener onRefreshListener);
```

## Demos
### Just only use PtrLayout like a FrameLayout. Only set the step 1 ! yes ! 
![image](https://github.com/BiaoWu/Resource/blob/master/PullToRefresh/NoRefreshView.gif)


