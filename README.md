## Android Pull To Refresh

* Support Pull Down To Refresh And Pull Up to Refresh. 

* 支持下拉＋上拉刷新。

## Download
Gradle:
```
compile 'com.biao:pulltorefresh:1.0.0-beta2'
```

## TODO
1. add more default headers
2. go on optimize


##How to use!

### Step 1 :	Just use PtrLayout like FrameLayout!!

```
<com.biao.pulltorefresh.PtrLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    ..RecyclerView
    ..ListView
    ..BTW, now only support one child!
</com.biao.pulltorefresh.PtrLayout>
```


### Step 2 :	set HeaderView and FooterView, two options now, as below!
    * DefaultRefreshView
    * MaterialRefreshView

```
public void setHeaderView(View view); 
public void setFooterView(View view);
```

BTW, if you custom the refresh view, as a general rule, you need to impl the PtrHandler..

```
// which impl PtrHandler is not refresh view, you need to set that:
public void setHeaderPtrHandler(PtrHandler ptrHandler);
public void setFootererPtrHandler(PtrHandler ptrHandler);

public interface PtrHandler {

    /** when refresh begin */
    void onRefreshBegin();

    /** when refresh end */
    void onRefreshEnd();

    /** when refresh pulling */
    void onPercent(float percent);
}

```

### Step 3 :	set refresh listener and at last you need call onRefreshComplete().  As the name implies .

```
public void setOnPullDownRefreshListener(OnRefreshListener onRefreshListener);
public void setOnPullUpRefreshListener(OnRefreshListener onRefreshListener);
```

```
ptrLayout.setOnPullDownRefreshListener(new OnRefreshListener() {
    @Override
    public void onRefresh() {
        headerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ptrLayout.onRefreshComplete();
            }
        }, 3000);
    }
});
```


### Optional :	set Mode.(default = PtrLayout.MODE_ALL_MOVE)

```
public void setMode(int mode);
PtrLayout.MODE_ALL_MOVE
PtrLayout.MODE_ONLY_CONTENT_NOT_MOVE
PtrLayout.MODE_ONLY_FOOTER_NOT_MOVE
PtrLayout.MODE_ONLY_HEADER_NOT_MOVE
PtrLayout.MODE_ONLY_CONTENT_MOVE
```

### Optional :	set release distance.(BTW, default = the height of refresh view).

```
public void setHeaderReleaseDist(int dist);
public void setFooterReleaseDist(int dist);

```

### Optional :	set duration distance.(default = 400).

```
public void setDuration(int duration)

```


## Demos
### Just only use PtrLayout like a FrameLayout. Only set the step 1 ! yes ! 
![image](https://github.com/BiaoWu/Resource/blob/master/PullToRefresh/NoRefreshView.gif)

### Default Mode = PtrLayout.MODE_ALL_MOVE, show as below !
![image](https://github.com/BiaoWu/Resource/blob/master/PullToRefresh/AllMove.gif)

### Other Modes, show as below !
![image](https://github.com/BiaoWu/Resource/blob/master/PullToRefresh/others.gif)

