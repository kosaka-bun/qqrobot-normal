# QQ Robot Normal
![Java](./docs/img/Java-11-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.7.20-brightgreen?logo=Kotlin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.5-brightgreen?logo=Spring)<br />
[![License](https://img.shields.io/github/license/kosaka-bun/qqrobot-normal?label=License&color=blue&logo=GitHub)](./LICENSE)
![GitHub Stars](https://img.shields.io/github/stars/kosaka-bun/qqrobot-normal?label=Stars&logo=GitHub)
[![Release](https://img.shields.io/github/release/kosaka-bun/qqrobot-normal?label=Release&logo=GitHub)](../../releases)

## 前言
本项目是一个简单的群聊互动机器人，用户可以通过浇水操作提升经验值和等级。<br />
浇水有概率获得各种各样的道具，它们有各种各样的效果，可以对自己或他人使用，以影响自己或他人的经验值或道具仓库。

本项目采用AGPL-3.0 License，其要求：
- 本项目的衍生项目需采用AGPL-3.0 License。
- 必须在修改的文件中附有明确的说明，须包含所修改的部分及具体的修改日期。
- 通过任何形式发布衍生项目的可执行程序时，或对衍生项目进行部署，并通过网络提供服务时，必须同时附带或公布衍生项目的源代码。

请参阅：[更新日志](./docs/changelog.md)

## 构建与运行

本项目基于[qqrobot-sdk](https://github.com/kosaka-bun/qqrobot-sdk)开发，请阅读该项目的文档以了解如何构建、运行和部署本项目。

[![qqrobot-sdk](https://github-readme-stats.vercel.app/api/pin/?username=kosaka-bun&repo=qqrobot-sdk)](https://github.com/kosaka-bun/qqrobot-sdk)

### 注意事项
1. 项目使用MyBatis Plus，SQL方言为MySQL，若需要更换其他数据源，请自行修改[mapper](./src/main/resources/mapper)目录中所有文件中的SQL语句中与其他数据源的SQL方言所不匹配的内容。
2. 项目使用JPA自动创建数据表格，不需要事先在数据库中创建数据表。
