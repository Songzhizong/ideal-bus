# 1.0.0.RC2

### ⭐ New Features

- 客户端新增`EventBus`接口, 该接口将作为客户端和服务端通讯的推荐接口.

# 1.0.0.RC1

### ⭐ New Features

- 移除receiver模块

- batchPublish方法名修改为publish

- 单节点吞吐量大幅提升

### 🐞 Bug Fixes

- 修复延迟消费功能未生效的问题

- 修复SnowFlake最大序列/最大数据中心id/最大机器码计算错误的问题

### 🔨 Dependency Upgrades

- commons-pool2 2.9.0

- jackson 2.11.3



# 1.0.0.M3

### ⭐ New Features

- 支持通过http协议向第三方服务推送消息。

- 取消延迟发布, 改为延迟消费。

- 订阅关系精确到具体的listener。

- 修改外部应用的参数名称。

- 添加消息执行状态: DISCARD / WAITING / RUNNING / SUCCESS / FAILURE。

- 修改通用包路径。

### 🐞 Bug Fixes

- 修复broker启动过程中卡主的问题

### 🔨 Dependency Upgrades

