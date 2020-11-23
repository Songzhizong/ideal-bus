package com.zzsong.bus.abs.generator;

/**
 * @author 宋志宗 on 2020/9/2
 */
public class SnowFlake implements IDGenerator {
  /**
   * 起始时间戳
   */
  public static final long START_TIMESTAMP = 1600348294001L;
  /**
   * 序列号位数, 单节点每毫秒最多生成 4096个唯一ID
   */
  private static final int SEQUENCE_BIT = 12;
  /**
   * 机器码占用的位数, 最多128个几点
   */
  private static final int MACHINE_BIT = 7;
  /**
   * 数据中心占用的位数, 最多8个数据中心
   */
  private static final int DATA_CENTER_BIT = 3;
  /**
   * 序列号最大值
   */
  private static final int MAX_SEQUENCE_NUM = (1 << SEQUENCE_BIT) - 1;
  /**
   * 机器码最大值
   */
  static final int MAX_MACHINE_NUM = (1 << MACHINE_BIT) - 1;
  /**
   * 数据中心最大值
   */
  static final int MAX_DATA_CENTER_NUM = (1 << DATA_CENTER_BIT) - 1;
  /**
   * 机器码向左的位移
   */
  private static final int MACHINE_LEFT = SEQUENCE_BIT;
  /**
   * 数据中心向左的位移
   */
  private static final int DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
  /**
   * 时间戳向左的位移
   */
  private static final int TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;

  /**
   * 数据中心id
   */
  private final long dataCenterId;

  /**
   * 机器id
   */
  private final long machineId;

  private long sequence = 0L;
  private long lasTimestamp = -1L;

  public SnowFlake(long dataCenterId, long machineId) {
    this.dataCenterId = dataCenterId;
    this.machineId = machineId;
  }


  @Override
  public synchronized long generate() {
    long currTimestamp = System.currentTimeMillis();
    if (currTimestamp < lasTimestamp) {
      throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
    }
    if (currTimestamp == lasTimestamp) {
      sequence = (sequence + 1) & MAX_SEQUENCE_NUM;
      if (sequence == 0L) {
        currTimestamp = getNextMill();
      }
    } else {
      sequence = 0L;
    }
    lasTimestamp = currTimestamp;
    return (currTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT
        | dataCenterId << DATA_CENTER_LEFT
        | machineId << MACHINE_LEFT
        | sequence;
  }

  private long getNextMill() {
    long mill = System.currentTimeMillis();
    while (mill <= lasTimestamp) {
      mill = System.currentTimeMillis();
    }
    return mill;
  }

  /**
   * 给定一个时间戳来计算该时间戳对应的最小编号
   *
   * @param timestamp 时间戳
   * @return 该时间戳计算出的最小编号
   * @author 宋志宗 on 2020/11/23
   */
  public static long calculateMinId(long timestamp) {
    if (timestamp <= START_TIMESTAMP) {
      throw new RuntimeException("时间戳不合法, 最小应为: " + START_TIMESTAMP);
    }
    return (timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT;
  }

  /**
   * 通过id还原时间戳
   *
   * @param id snowflake生成的id
   * @return 该id产生的时间戳
   * @author 宋志宗 on 2020/11/23
   */
  public static long reductionTimestamp(long id) {
    long difference = id >> TIMESTAMP_LEFT;
    if (difference <= 0) {
      throw new RuntimeException("非snowflake生成id");
    }
    return difference + START_TIMESTAMP;
  }
}
