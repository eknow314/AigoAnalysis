# AigoAnalysis

AigoStar内部数据统计

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
    implementation 'com.github.eknow314:AigoAnalysis:1.0.7'
}
```

### 使用

- 初始化：在 Application 的 onCreate() 进行初始化

```text
TrackerHelper.getInstance().init(this, "https://test.smartapi.aigostar.com:3443/analytics/", true);
        
```

- 如果要 fragment 上报页面

```text
public void onResume() {
    super.onResume();
    TrackerHelper.onFragmentStart(“页面名称”);
}

public void onPause() {
    super.onPause();
    TrackerHelper.onFragmentEnd(“页面名称”);
}
        
```

- 上报策略：这里会自动上报 activity 的停留时间，上报策略实时上报，在 WorkManage 里面执行即时上报

sample 里面有集成了友盟海外版的sdk

