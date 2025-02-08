package org.tron.core.services.http;

import org.tron.api.GrpcAPI;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AssetIssueUtil {


  public static String serializeAssetList(GrpcAPI.AssetIssueList assetList,boolean visible) {
    if (assetList == null) {
      return "[]";
    }


    long startTime = System.currentTimeMillis();
    StringBuilder resultBuilder = new StringBuilder("\"assetIssue\":[");
    AtomicBoolean isFirst = new AtomicBoolean(true);
    AtomicInteger successCount = new AtomicInteger(0);


    assetList.getAssetIssueList().parallelStream()
        .map(asset -> {
          try {
            String json = JsonFormat.printToString(asset, visible);
            successCount.incrementAndGet();
            return json;
          } catch (Exception e) {
            return "";
          }
        })
        .filter(json -> !json.isEmpty())
        .forEachOrdered(json -> {
          if (!isFirst.get()) {
            resultBuilder.append(",");
          } else {
            isFirst.set(false);
          }
          resultBuilder.append(json);
        });


    resultBuilder.append("]");
    String result = resultBuilder.toString();


    long duration = System.currentTimeMillis() - startTime;
    System.out.printf("Serialized %d assets in %d ms%n",
        successCount.get(), duration);


    return result;
  }

}
