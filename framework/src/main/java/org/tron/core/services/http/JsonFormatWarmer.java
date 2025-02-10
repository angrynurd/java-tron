package org.tron.core.services.http;

import com.google.protobuf.ByteString;
import org.tron.api.GrpcAPI;
import org.tron.protos.Protocol;
import org.tron.protos.contract.AssetIssueContractOuterClass;

import static org.reflections.Reflections.log;

//JsonFormat预热类
public class JsonFormatWarmer {
  public static void warmupBlock() {
    // 构造一个最小的Block对象即可
    Protocol.Block sampleBlock = Protocol.Block.newBuilder()
        .setBlockHeader(Protocol.BlockHeader.newBuilder()
            .setRawData(Protocol.BlockHeader.raw.newBuilder()
                .setNumber(1L)
                .build())
            .build())
        .build();

    // 预热2-3次
    for (int i = 0; i < 3; i++) {
      JsonFormat.printToString(sampleBlock, true);
      JsonFormat.printToString(sampleBlock, false);
    }
  }


  public static void warmuptrc10() {
    // 构造简单的AssetIssue对象
    AssetIssueContractOuterClass.AssetIssueContract assetIssue = AssetIssueContractOuterClass.AssetIssueContract.newBuilder()
        .setName(ByteString.copyFromUtf8("TestAsset"))
        .setOrder(1)
        .setId("1")
        .build();

    // 构造AssetIssueList对象
    GrpcAPI.AssetIssueList assetIssueList = GrpcAPI.AssetIssueList.newBuilder()
        .addAssetIssue(assetIssue)
        .build();

    // 预热2-3次
    for (int i = 0; i < 3; i++) {
      // 预热单个AssetIssue
      JsonFormat.printToString(assetIssue, true);
      JsonFormat.printToString(assetIssue, false);

      // 预热AssetIssueList
      JsonFormat.printToString(assetIssueList, true);
      JsonFormat.printToString(assetIssueList, false);
    }
  }


  public static void warmupTx() {
    // 构造Transaction对象
    Protocol.Transaction.Builder transactionBuilder = Protocol.Transaction.newBuilder()
        .setRawData(Protocol.Transaction.raw.newBuilder()
            .setTimestamp(System.currentTimeMillis())
            .setExpiration(System.currentTimeMillis() + 60_000)
            .build());

    // 使用 addRet 替代 setRet
    transactionBuilder.addRet(Protocol.Transaction.Result.newBuilder()
        .setContractRet(Protocol.Transaction.Result.contractResult.SUCCESS)
        .build());

    Protocol.Transaction transaction = transactionBuilder.build();

    // 构造TransactionInfo对象
    Protocol.TransactionInfo transactionInfo = Protocol.TransactionInfo.newBuilder()
        .setId(ByteString.copyFromUtf8("test_transaction_id"))
        .setBlockNumber(1234L)
        .setBlockTimeStamp(System.currentTimeMillis())
        .setResult(Protocol.TransactionInfo.code.SUCESS)
        .build();

    // 构造TransactionInfoList对象
    GrpcAPI.TransactionInfoList transactionInfoList = GrpcAPI.TransactionInfoList.newBuilder()
        .addTransactionInfo(transactionInfo)
        .build();

    // 预热3次
    for (int i = 0; i < 3; i++) {
      // 预热Transaction序列化
      JsonFormat.printToString(transaction, true);
      JsonFormat.printToString(transaction, false);

      // 预热TransactionInfo序列化
      JsonFormat.printToString(transactionInfo, true);
      JsonFormat.printToString(transactionInfo, false);

      // 预热TransactionInfoList序列化
      JsonFormat.printToString(transactionInfoList, true);
      JsonFormat.printToString(transactionInfoList, false);
    }
  }


  public static void warmupBlocklist() {
    try {
      // 创建一个最小的测试数据
      GrpcAPI.BlockList sampleData = GrpcAPI.BlockList.newBuilder()
          .addBlock(Protocol.Block.newBuilder()
              .setBlockHeader(Protocol.BlockHeader.newBuilder()
                  .setRawData(Protocol.BlockHeader.raw.newBuilder()
                      .setTimestamp(System.currentTimeMillis())
                      .setNumber(1L)
                      .build())
                  .build())
              .build())
          .build();

      // 预热两种场景
      JsonFormat.printToString(sampleData, true);
      JsonFormat.printToString(sampleData, false);
    } catch (Exception e) {
      log.warn("JsonFormat warmup failed", e);
    }
  }


}
