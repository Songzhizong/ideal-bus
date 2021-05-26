# 1.2.0

### ⭐ New Features

- 重构服务端实现
- 重构客户端实现
- 支持按topic为事件设置存储过期时间

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- spring boot 2.4.6
- project reactor 2020.0.7

# 1.1.0

### ⭐ New Features

- 客户端开始消费时间点由启动过程中修改为启动完成后

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- spring boot 2.4.3
- project reactor 2020.0.5

# 1.1.0-M2

### ⭐ New Features

- EventMessage新增entity字段保存聚合根的类型

### 🐞 Bug Fixes

- 修复启动时报循环依赖的问题
- 修复交付失败情况下的无限重试问题

### 🔨 Dependency Upgrades

- guava 30.1-jre
- spring boot 2.4.2
- project reactor 2020.0.3

# 1.1.0-M1

### ⭐ New Features

- API变更
- 代码重构
- 数据存储结构变更

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- broker -> jdk 15
- project reactor 2020.0.1
- spring boot 2.4.0
- guava 30.0-jre

# 1.0.0.RELEASE

### ⭐ New Features

- 为EventInstanceDo.timestamp 添加降序索引

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- spring-boot 2.3.5.RELEASE

# 1.0.0.RC3

### ⭐ New Features

- EventContext添加新属性

### 🐞 Bug Fixes

- 修复cglib代理的情况下无法正确获取EventListener的问题

### 🔨 Dependency Upgrades

- lombok 1.18.16
- reactor 2020.0.0

# 1.0.0.RC2

### ⭐ New Features

- 客户端新增`EventBus`接口.

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

