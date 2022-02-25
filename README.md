# AigoAnalysis

- AigoSmart 内部数据统计

- 上报策略：120s间隔启动上报任务，每次产生的事件缓存在本地

## 接入指引

最新版本：[![](https://jitpack.io/v/eknow314/AigoAnalysis.svg)](https://jitpack.io/#eknow314/AigoAnalysis)


### 依赖配置

```groovy
allprojects {
    repositories {
        maven { url 'https://www.jitpack.io' }
    }
}
```

```groovy
dependencies {
    implementation 'com.github.eknow314:AigoAnalysis:+'
}
```

### 使用

- 初始化：在 Application 的 onCreate() 进行初始化

```text
AigoAnalysisHelper.getInstance()
                //配置目标地址、站点、租户id
                .config(this, "https://xxxxxxxxxxxxxxxx", 1, "租户id")
                //是否打印日志
                .showLog(BuildConfig.DEBUG)
                //自动上报 activity
                .autoActivityPage()
                //初始化，调用服务端逻辑
                .init();
        
```

- 低内存优化处理

```text
@Override
public void onLowMemory() {
    super.onLowMemory();
    AigoAnalysisHelper.getInstance().getTracker().onLowMemory();
}

@Override
public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    AigoAnalysisHelper.getInstance().getTracker().onTrimMemory(level);
}
```

- 如果要 fragment 上报页面

```text
public void onResume() {
    super.onResume();
    AigoAnalysisHelper.getInstance().onFragmentStart(“页面名称”);
}

public void onPause() {
    super.onPause();
    AigoAnalysisHelper.getInstance().onFragmentEnd(“页面名称”);
}
        
```

- 自定义事件上报

```text
TrackHelper.track()
        .custom("一级事件", "二级事件")
        //拓展参数，可不传
        .setExtension("key1", "value1")
        .setExtension("key2", "value2")
        .with(AigoAnalysisHelper.getInstance().getTracker());
        
```



