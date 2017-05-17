# WaveSideBar
github地址：https://github.com/lvzhihao100/WaveSideBar    


简书地址：http://www.jianshu.com/p/ce4e768cd12c 


  compile 'com.github.lvzhihao100:WaveSideBar:1.0.2' 
  
只有一个类，很简单，所以你可以直接拷走，根据自己的需求，再定制一番，如果赶时间，直接用，超简单 

1. 先上效果图
![x.gif](http://upload-images.jianshu.io/upload_images/4179767-f84f408ee69b9f81.gif?imageMogr2/auto-orient/strip)
2. 简要使用说明
设置recyclerview 

```
  public void setRecyclerView(RecyclerView recyclerView) 
```
设置数据源 

```
    /**
     *
     * @param datas item已按照字母顺序排好序的数据
     * @param onLetterGet 获取你排序所依照的属性
     * @param from 从RecyclerView的第几项开始，一般设置头部数量
     * @param <T>
     */
    public <T> void setData(List<T> datas, OnLetterGet<T> onLetterGet, int from)
```
使用方法很简单吧
