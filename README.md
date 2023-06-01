# MidJourneyAidProxy

代理 MidJourney 的discord频道，实现api形式调用AI绘图，公益项目

## 现有功能

- [x] 支持 Imagine、U、V 指令，绘图完成后回调
- [x] 支持 Describe 指令，根据图片生成 prompt
- [x] 支持中文 prompt 翻译，需配置百度翻译或 gpt
- [x] prompt 敏感词判断，支持覆盖调整
- [x] 任务队列，默认队列10，并发3。可参考 [MidJourney订阅级别](https://docs.midjourney.com/docs/plans) 调整mj.queue

## 使用前提

1. 科学上网
2. docker环境
3. 注册 MidJourney，创建自己的频道，参考 https://docs.midjourney.com/docs/quick-start
4. 添加自己的机器人: [MidJourneyAidProxy/discord-bot.md at main · Starry-Gaze/MidJourneyAidProxy · GitHub](https://github.com/Starry-Gaze/MidJourneyAidProxy/blob/main/botSetting/discord-bot.md)

## 风险须知

- 作图频繁等行为，触发midjourney验证码后，需尽快人工验证

## 快速启动

### 本地项目启动

修改application.yml参数

```
mj:
  #任务存储
  task-store:
    type: IN_MEMORY
    timeout: 30d
  #discord配置
  discord:
    #服务器id
    guild-id: xxx
    #频道id
    channel-id: xxx
    #登录token
    user-token: xxx
    #机器人token
    bot-token: xxx
  #代理配置
  proxy:
    #本地代理地址
    host: 127.0.0.1
    #本地代理端口
    port: 7890
  #翻译方式
  translate-way: xxx
  #任务队列配置
  queue:
    #任务超时时间(分钟)
    timeout-minutes: 5
    #并发数
    core-size: 3
    #等待队列长度
    queue-size: 10
  #百度翻译配置
  baidu-translate:
    #百度翻译的APP_ID
    appid: xxx
    #百度翻译的密钥
    app-secret: xxx
  #openai配置
  openai:
    #gpt的api-key
    gpt-api-key: xxx
    #使用的模型
    model: "gpt-3.5-turbo"
    #返回结果的最大分词数
    max-tokens: 2048
    #相似度，取值 0-2
    temperature: 0
  #任务状态变更回调地址(你实际中转的项目地址)
  notify-hook: http://127.0.0.1:4120/notify
spring:
  cache:
    type: redis
  redis:
    host: xxx.xxx.xxx.xxx
    port: xxx
    password: xxx
```

### Docker海外服务器部署启动

1. 在海外服务器上先装一个docker环境
2. 创建一个abc文件夹
3. 在abc文件夹下创建一个nginx.conf文件，创建一个docker-compose.yaml文件
4. 在abc文件夹下面创建一个目录 mkdir redis-data
5. 启动docker compose up -d

nginx.conf参数

```
events {}

http {
    upstream backend {
        ip_hash;
 server  backend_1:8080;
        server  backend_2:8080;
    }

    server {
        listen 80;

        location / {
            proxy_pass http://backend;
        }
    }
}
```

docker-compose.yaml 参数

```
version: '3'
services:
  redisa:
    image: redis:latest
    command: redis-server --requirepass 123456
    volumes:
      - ./redis-data:/data
    ports:
      - "6379:6379"

  nginx:
    image: nginx:latest
    ports:
      - "80:80"
    depends_on:
      - backend_1
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf

  frontfe:
    image: k8svip/mj_node_front_fe:v0.1
    environment:
      - MJ_PROXY_ENDPOINT=http://nginx:80/mj
      - BLOCK_WORDS="Test k8svip"
    ports:
      - "4120:4120"
      
  backend_1:
    image: k8svip/backend:v0.4
    ports:
      - "8081:8080"
    depends_on:
      - frontfe
      - redisa
    environment:
      - GUILD_ID=xxx
      - CHANNEL_ID=xxx
      - USER_TOKEN=xxx
      - BOT_TOKEN=xxx
      - TANSLATE_WAY=GPT
      - APPID=xxx
      - APP_SECRECT=xxx
      - GPT_API_KEY=xxx
      - NOTIFY_HOOK=http://frontfe:4120/notify
      - TASK_STORE_TYPE=REDIS
      - SPRING_CACHE_TYPE=redis
      - REDIS_HOST=redisa
      - REDIS_PORT=6379
      - REDIS_PW=123456
```

## 注意事项

1. 在https://github.com/StStarry-Gazearry-Gaze/MidJourneyAidProxy/issues 中提出其他问题或建议

2. 感兴趣的朋友也欢迎加入交流群讨论一下，扫码进群名额已满，加管理员微信邀请进群

   <img src="https://github.com/Starry-Gaze/MidJourneyAidProxy/blob/main/botSetting/team_code.jpg" alt="" style="zoom:25%;" align='left'/>

## 配置项

### 项目启动配置项：

| 变量名                        | 非空 | 描述                                                    |
| ----------------------------- | ---- | ------------------------------------------------------- |
| mj.discord.guild-id           | 是   | discord服务器ID                                         |
| mj.discord.channel-id         | 是   | discord频道ID                                           |
| mj.discord.user-token         | 是   | discord用户Token                                        |
| mj.discord.user-agent         | 否   | 调用discord接口，建议从浏览器network复制                |
| mj.discord.bot-token          | 否   | 自定义机器人Token                                       |
| mj.discord.mj-bot-name        | 否   | midjourney官方机器人名称，默认 "Midjourney Bot"         |
| mj.notify-hook                | 否   | 全局的任务状态变更回调地址                              |
| mj.task-store.type            | 否   | 任务存储方式，默认in_memory(内存\重启后丢失)，可选redis |
| mj.task-store.timeout         | 否   | 任务过期时间，过期后删除，默认30天                      |
| mj.queue.core-size            | 否   | 并发数，默认为3                                         |
| mj.queue.queue-size           | 否   | 等待队列，默认长度10                                    |
| mj.queue.timeout-minutes      | 否   | 任务超时时间，默认为5分钟                               |
| mj.proxy.host                 | 否   | 代理host，全局代理不生效时设置                          |
| mj.proxy.port                 | 否   | 代理port，全局代理不生效时设置                          |
| mj.translate-way              | 否   | 中文prompt翻译方式，可选null(默认)、baidu、gpt          |
| mj.baidu-translate.appid      | 否   | 百度翻译的appid                                         |
| mj.baidu-translate.app-secret | 否   | 百度翻译的app-secret                                    |
| mj.openai.gpt-api-key         | 否   | gpt的api-key                                            |
| mj.openai.timeout             | 否   | openai调用的超时时间，默认30秒                          |
| mj.openai.model               | 否   | openai的模型，默认gpt-3.5-turbo                         |
| mj.openai.max-tokens          | 否   | 返回结果的最大分词数，默认2048                          |
| mj.openai.temperature         | 否   | 相似度(0-2.0)，默认0                                    |
| spring.redis                  | 否   | 任务存储方式设置为redis，需配置redis相关属性            |

#### spring.redis配置参考：

```
spring:
  redis:
    host: xxx.xxx.xxx.xxx
    port: 6379
    password: xxx
```

### docker镜像制作配置项：

| 变量名            | 非空 | 描述                                           |
| ----------------- | ---- | ---------------------------------------------- |
| GUILD_ID          | 是   | discord服务器ID                                |
| CHANNEL_ID        | 是   | discord频道ID                                  |
| USER_TOKEN        | 是   | discord用户Token                               |
| BOT_TOKEN         | 是   | 自定义机器人Token                              |
| TANSLATE_WAY      | 是   | 中文prompt翻译方式，可选NULL(默认)、BAIDU、GPT |
| APPID             | 是   | 百度翻译的appid                                |
| APP_SECRECT       | 是   | 百度翻译的app-secret                           |
| GPT_API_KEY       | 是   | gpt的api-key                                   |
| NOTIFY_HOOK       | 是   | 全局的任务状态变更回调地址                     |
| TASK_STORE_TYPE   | 是   | 任务存储方式，默认redis（30天以后失效）        |
| SPRING_CACHE_TYPE | 是   | 缓存类型（redis）                              |
| REDIS_HOST        | 是   | redis的地址                                    |
| REDIS_PORT        | 是   | redis的端口                                    |
| REDIS_PW          | 是   | redis的密码                                    |

## 本地开发

- 依赖java17和maven
- 更改配置项: 修改src/main/application.yml
- 项目运行: 启动ProxyApplication的main函数
- 拉取代码后，启用maven的package进行打包 ，然后把Dockerfile移动到target目录下，构建镜像: `docker build -t MidJourneyAidProxy .`

## 应用项目

- **[WeChatAidProxy](https://github.com/Starry-Gaze/WeChatAidProxy)**[](https://github.com/Starry-Gaze/WeChatAidProxy) : 代理微信客户端，接入MidJourney，仅示例应用场景，后续会出台QQ等各大平台机器人

## 其他

如果觉得这个项目对你有所帮助，请帮忙点个star；也可以请Starry-Gaze团队喝杯奶茶～

<img src="https://github.com/Starry-Gaze/MidJourneyAidProxy/blob/main/botSetting/support_code.jpg" alt="image-20230602052733951" style="zoom: 25%;" align='left' />

