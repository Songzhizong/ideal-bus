package com.zzsong.bus.abs.mongo.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Configuration
@EnableReactiveMongoRepositories("com.zzsong.bus.abs.mongo.repository")
public class MongoStorageConfig {
}
