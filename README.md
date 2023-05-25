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
4. 添加自己的机器人: MidJourneyAidProxy/botSetting/

## 注意事项

1. 在https://github.com/Starry-Gaze/MidJourneyAidProxy/issues 中提出其他问题或建议

