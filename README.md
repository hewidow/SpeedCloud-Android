# SpeedCloud-Android
本项目为移动应用开发课设，使用Kotlin语言开发，界面大致仿照百度网盘Android端APP。软件界面如[项目总结报告](https://github.com/hewidow/SpeedCloud-Android/blob/master/%E9%A1%B9%E7%9B%AE%E6%80%BB%E7%BB%93%E6%8A%A5%E5%91%8A.docx)里的所示，打包好的应用[SpeedCloud.apk](https://github.com/hewidow/SpeedCloud-Android/releases/latest)在这里，API的请求地址在[config.xml](https://github.com/hewidow/SpeedCloud-Android/blob/master/app/src/main/res/values/config.xml)中设置。

项目做得比较赶，文件上传和下载无法暂停和断点续传，都只支持单线程。代码写得也很粗糙，基本按怎么方便怎么来。

按场景分类，使用到如下组件或技术，基础的Layout就不列举了。
- 账户界面
    - Fragment（单例模式，懒加载）
    - SharedPreferences（共享参数）
    - RippleLinearLayout（自定义组件，背景波纹动画）
- 主界面
    - SingleTask（启动模式）
    - TabLayout（使用TabLayoutMediator）
    - ViewPage2（主界面左右页面切换）
    - RecyclerView（文件显示）
    - Menu（顶部工具栏）
    - Dialog
        - AlertDialog
        - PopupWindow
        - DialogFragment
    - FloatingActionButton
    - ClipboardManager
- 视频播放
    - VideoView
- 图片查看
    - ImageView
- 文件下载
    - DownloadManager（安卓原生，难顶）
    - Broadcast（广播）
    - Room（存储框架，SQLite之上的抽象层）
    - HttpURLConnection（安卓原生，难顶）
    - 分页
- 文件上传
    - Service（自定义绑定服务）
    - MessageDigest（计算MD5）
    - Room（存储上传和下载记录）
    - OkHttp（第三方Http框架，监听上传进度）
    - UriUtil（网上找的，Android不同版本权限不一致，难顶）
    - 分页、分片
- 协程
    - LifecycleScope（用来发送API请求）

## 功能展示

![用户登录](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/dd218636-8dd5-4623-b540-2bfb22f89801 "用户登录")
![网盘文件](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/d690f4ca-f32e-4055-a4a6-f75719cdd185 "网盘文件")
![上传](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/3f051422-bec8-42dd-8c76-ced4ba2a60a9 "上传")
![文件操作](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/498188a3-80d4-4dd6-ad5a-eda4a722015e "文件操作")
![下载](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/dd9a800c-3ff6-4ea9-8f29-c08bbdfa4a47 "下载")
![分享](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/fec1abf2-772e-4d39-a60d-9f45b52f79dd "分享")
![删除](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/fc0bce41-eb21-41c6-be1a-f322257ec669 "删除")
![改名](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/78e4dba4-88e4-4688-b65f-bad41d1e1fd9 "改名")
![移动](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/2d321a42-37b2-4a62-8f88-d1ea3d5cd744 "移动")
![在线查看](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/0f7adc2f-9edb-4e7b-bfa9-eb14f8a943de "在线查看")
![用户界面](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/c0f2e7fd-5d13-4c9b-97fc-8a1721526df8 "用户界面")
![回收站](https://github.com/hewidow/SpeedCloud-Android/assets/23414174/6f3edb78-1b8a-4ab0-89a0-00c01c1d83a0 "回收站")

## 配套
搭配web开发课设项目：  
前端用户侧：https://github.com/hewidow/SpeedCloud-UI  
前端管理测：https://github.com/hewidow/speed-cloud-boss  
后端：https://github.com/hewidow/speed-cloud

