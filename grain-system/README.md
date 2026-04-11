# 乡镇粮食代收点进销存系统

&#x20;

基于 Vue3 + SpringBoot 的粮食收购、仓储、销售一体化管理系统

## 项目介绍

本系统面向乡镇粮食代收点，实现农户预约收购、地磅称重自动采集、库存管理与盘点、销售订单与出库核销、硬件对接、数据统计与经营仪表盘。

## 技术栈

前端：Vue3 + Vite + Element Plus

后端：SpringBoot 3.2.x + Java 17

数据库：MySQL 8.0

缓存：Redis

文件存储：MinIO

硬件对接：Netty、MQTT、RTSP

## 模块说明

1. 基础管理
2. 收购管理
3. 仓储管理
4. 销售管理
5. 硬件数据对接
6. 统计分析

## 启动方式

### 后端

1. 创建数据库 grain\_purchase\_inventory\_system
2. 执行 docs/sql/init.sql
3. 配置 application.yml 数据库连接
4. 启动 SpringBoot
5. 后端启动命令： mvn spring-boot:run&#x20;
6. &#x20;后端编译命令： mvn compile&#x20;
7. &#x20;清理并重启： mvn clean spring-boot:run

### 前端

1. \## 前端启动（npm） npm install&#x20;
2. npm run dev&#x20;
3. \## 前端打包 npm run build
4. 访问 <http://localhost:5173>

## 开发规范

遵循 .trae/coding-and-testing-spec.md 编码与自测规范

遵循 TRAE\_TASK\_QUEUE.md 任务开发流程

遵循 docs / 乡镇粮食代收点进销存系统概要设计.md 需求开发
