# base img
FROM k8svip/mjjdk:v0.2

LABEL maintainer="k8svip@gmail.com"

ARG JAR_FILE=MidJourneyAidProxy.jar
#ADD MidJourneyAidProxy.jar /app/MidJourneyAidProxy.jar

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

ADD ${JAR_FILE} /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
#ENTRYPOINT ["java","-jar","/app/MidJourneyAidProxy.jar"]
