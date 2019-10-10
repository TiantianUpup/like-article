# redis-like

### redis-like项目介绍
基于`Redis`的点赞功能实现`demo`

### 开发环境
- windows10 os
- Maven 3.6.1
- JDK 1.8

### 技术栈
- MySQL
- Redis
- MyBatisPlus
- SpringBoot

### 程序运行
- 创建数据库&导入表  
1.创建`article-like`数据库  
2.数据库表结构在`sql`文件夹下  
- 启动本地`Redis`，进入`Redis`安装目录执行命令:  
    
    ```
    redis-server.exe redis.windows.conf 
    ```
- 运行`Run.java`

### API说明
###### 点赞文章
- `API` => `/{articleId}/{likedUserId}/{likedPoseId}`
- 请求方式 => `POST`
- 参数说明  

| 参数名称 | 参数类型 | 是否必填 | 参数说明 |  
| :-----: | :---: | :---: | :---: |
| articleId | Long | 是 | 文章ID |  
| likedUserId | Long | 是 | 被点赞用户ID |
| likedPoseId | Long | 是 | 点赞用户ID |

###### 取消点赞
- `API` => `/{articleId}/{likedUserId}/{likedPoseId}`
- 请求方式 => `DELETE`
- 参数说明

| 参数名称 | 参数类型 | 是否必填 | 参数说明 |  
| :-----: | :---: | :---: | :---: |
| articleId | Long | 是 | 文章ID |  
| likedUserId | Long | 是 | 被点赞用户ID |
| likedPoseId | Long | 是 | 点赞用户ID |

###### 获取用户点赞的文章
- `API` => `/user/{likedPoseId}/like`
- 请求方式 => `GET`
- 参数说明

| 参数名称 | 参数类型 | 是否必填 | 参数说明 |  
| :-----: | :---: | :---: | :---: |
| likedPoseId | Long | 是 | 点赞用户ID |

###### 统计用户总的文章点赞数
- `API` => `/total/user/{userId}`
- 请求方式 => `GET`
- 参数说明  

| 参数名称 | 参数类型 | 是否必填 | 参数说明 |  
| :-----: | :---: | :---: | :---: |
| likedUserId | Long | 是 | 被点赞用户ID |

###### 统计某篇文章总点赞数
- `API` => `/total/article/{articleId}`
- 请求方式 => `GET`
- 参数说明

| 参数名称 | 参数类型 | 是否必填 | 参数说明 |  
| :-----: | :---: | :---: | :---: |
| articleId | Long | 是 | 文章ID |  



