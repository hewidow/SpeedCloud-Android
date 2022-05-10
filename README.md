# SpeedCloud-Android
本项目为移动应用开发课设，使用Kotlin语言，仿照百度网盘Android端APP。按场景分类，使用到如下组件或技术，基础的Layout就不列举了。
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

项目做得比较赶，文件上传和下载无法暂停和断点续传，都只支持单线程。代码写得也很粗糙，基本按怎么方便怎么来

配套项目：  
前端web：https://gitee.com/speed-cloud/SpeedCloud-UI  
后端：https://gitee.com/speed-cloud/speed-cloud
