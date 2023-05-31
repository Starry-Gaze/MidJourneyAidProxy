# base img
#这一行表示构建的新镜像以 k8svip/mjjdk:v0.2 这个镜像作为基础。
FROM k8svip/mjjdk:v0.2

#这一行设置了镜像的维护者信息。
LABEL maintainer="k8svip@gmail.com"

#：这一行定义了一个构建参数，其默认值为 "MidJourneyAidProxy.jar"。
ARG JAR_FILE=MidJourneyAidProxy.jar
#ADD MidJourneyAidProxy.jar /app/MidJourneyAidProxy.jar

#ENV 命令用于设置环境变量，这些环境变量在运行时可供容器内的应用使用。
ENV GUILD_ID=""
ENV CHANNEL_ID=""
ENV USER_TOKEN=""
ENV BOT_TOKEN=""
ENV TANSLATE_WAY=""
ENV APPID=""
ENV APP_SECRECT=""
ENV GPT_API_KEY=""
ENV NOTIFY_HOOK=""
ENV TASK_STORE_TYPE=""
ENV SPRING_CACHE_TYPE=""
ENV REDIS_HOST=""
ENV REDIS_PORT=""
ENV REDIS_PW=""

#这一行将本地的 ${JAR_FILE} 文件（即 "MidJourneyAidProxy.jar" 文件）添加到镜像中的 /app.jar。
ADD ${JAR_FILE} /app.jar

#这一行定义了容器启动后默认运行的命令，这里是启动 Java 应用。
ENTRYPOINT ["java","-jar","/app.jar"]
#ENTRYPOINT ["java","-jar","/app/MidJourneyAidProxy.jar"]
