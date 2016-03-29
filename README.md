# ViewCore
### 主要是常用自定义控件的类库，该项目已经上传到 jCenter 仓库，可以直接使用

欢迎大家下载体验本项目，如果使用过程中遇到什么问题，欢迎反馈

 * 邮箱地址： liaojeason@126.com
 * 微信号： Lzy_19920216


##用法
```java
    compile 'com.lzy.widget:view-core:0.1.5'
```
##注意
该库需要项目的编译版本为 23，使用时请在 build.gradle 文件中，将其改为23
```java
    compileSdkVersion 23
```

## 主要包含以下几个控件，一次导包，永久使用
### HexagonView 
 * 项目地址：[https://github.com/jeasonlzy0216/HexagonView](https://github.com/jeasonlzy0216/HexagonView)，六边形带圆角的自定义View，支持图文混排，点击区域，水平垂直方向切换，圆角大小等各种属性

### AlphaIndicatorView 
 * 项目地址：[https://github.com/jeasonlzy0216/AlphaIndicatorView](https://github.com/jeasonlzy0216/AlphaIndicatorView)，仿微信底部tab标签，滑动的时候颜色渐变，使用极其简单，只需要两行代码

### OverScrollDecor 
 * 项目地址：[https://github.com/jeasonlzy0216/OverScrollDecor](https://github.com/jeasonlzy0216/OverScrollDecor)，类似IOS的over-scrolling效果，即对于滑动到顶部的View继续滑动时会超出，松手后自动还原到原始位置。支持ListView，GridView，ScrollView，WebView，RecyclerView，以及其他的任意View和ViewGroup

### PullZoomView 
 * 项目地址：[https://github.com/jeasonlzy0216/PullZoomView](https://github.com/jeasonlzy0216/PullZoomView)，类似QQ空间，新浪微博个人主页下拉头部放大的布局效果，支持ListView，GridView，ScrollView，WebView，RecyclerView，以及其他的任意View和ViewGroup。支持头部视差动画，阻尼下拉放大，滑动过程监听

### VerticalSlideView 
 * 项目地址：[https://github.com/jeasonlzy0216/VerticalSlideView](https://github.com/jeasonlzy0216/VerticalSlideView)，类似淘宝的商品详情页，继续拖动查看详情，其中拖动增加了阻尼，并且重写了ListView，GridView，ScrollView，WebView，RecyclerView 的 dispatchTouchEvent 方法，使用的时候无须额外的代码，可以任意嵌套使用

### HeaderViewPager 
 * 项目地址：[https://github.com/jeasonlzy0216/HeaderViewPager](https://github.com/jeasonlzy0216/HeaderViewPager)，具有共同头部的 ViewPager，支持与ListView，GridView，ScrollView，WebView，RecyclerView 嵌套使用。具有连续的滑动事件 和 滑动监听， 支持下拉刷新

###CircleImageView
 * 圆形的ImageView，用法同ImageView一样

###LoopViewPager
 * 可以循环滑动和自动轮播的ViewPager

###TabTitleIndicator
 * ViewPager的指示器

###CircleIndicator
 * ViewPager的点状指示器
