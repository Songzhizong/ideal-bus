package com.zzsong.bus.base.mongo.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Configuration
@EnableReactiveMongoRepositories("com.zzsong.bus.base.mongo.repository")
public class MongoStorageConfig {
}
