package org.tron.core.services.http;

import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI;
import org.tron.core.Wallet;
import org.tron.protos.Protocol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlockCacheProvider {
  private final Wallet wallet;
  private final Deque<String> cache = new ArrayDeque<>(100);
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


  public BlockCacheProvider(Wallet wallet) {
    this.wallet = wallet;
    initCache();
    scheduler.scheduleAtFixedRate(this::updateCache, 3, 3, TimeUnit.SECONDS);
  }


  /**
   * 初始化缓存：加载最新的100个区块
   */
  private void initCache() {
    GrpcAPI.BlockList blockList = wallet.getBlockByLatestNum(100);
    lock.writeLock().lock();
    try {
      // 逆序遍历保证队列尾部存储最新区块
      for (int i = blockList.getBlockCount() - 1; i >= 0; i--) {
        Protocol.Block block = blockList.getBlockList().get(i);
        String json = JsonFormat.printToString(block, true);
        cache.addLast(json);
      }
    } catch (Exception e) {
      // 记录日志
    } finally {
      lock.writeLock().unlock();
    }
  }


  /**
   * 定时更新缓存：每3秒拉取最新区块
   */
  private void updateCache() {
    try {
      Protocol.Block latestBlock = wallet.getNowBlock();
      String json = JsonFormat.printToString(latestBlock, true);


      lock.writeLock().lock();
      try {
        cache.addLast(json);
        if (cache.size() > 100) {
          cache.removeFirst();
        }
      } finally {
        lock.writeLock().unlock();
      }
    } catch (Exception e) {
      // 记录日志
    }
  }


  /**
   * 查询最新的N个区块（JSON数组格式）
   */
  public String getBlockByLatestNum(long num) {
    long limit = Math.min(num, 100);
    StringBuilder sb = new StringBuilder((int) (limit * 1024)); // 预分配内存

    lock.readLock().lock();
    try {
      Iterator<String> it = cache.descendingIterator();
      sb.append("[");
      for (int i = 0; i < limit && it.hasNext(); i++) {
        if (i > 0) sb.append(",");
        sb.append(it.next());
      }
      sb.append("]");
    } finally {
      lock.readLock().unlock();
    }
    return sb.toString();
  }


  public String getNowBlock() {
    long startTime2 = System.currentTimeMillis();
    lock.readLock().lock();
    try {
      if (cache.isEmpty()) {
        // 降级回源调用
        Protocol.Block block = wallet.getNowBlock();
        return JsonFormat.printToString(block, true);
      }
      String result = cache.getLast();
      long endTime2 = System.currentTimeMillis();
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      lock.readLock().unlock();
    }
  }


  /**
   * 关闭缓存服务
   */
  public void shutdown() {
    scheduler.shutdown();
  }
}
