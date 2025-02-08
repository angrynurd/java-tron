package org.tron.core.services.http;

import com.google.protobuf.ByteString;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.common.utils.ByteArray;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;

import static org.tron.core.services.http.AssetIssueUtil.serializeAssetList;


@Component
@Slf4j(topic = "API")
public class GetAssetIssueByAccountServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  @PostConstruct
  public void init() {
    // 预热特定场景
    JsonFormatWarmer.warmuptrc10();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      String address = request.getParameter("address");
      if (visible) {
        address = Util.getHexAddress(address);
      }
      fillResponse(visible, ByteString.copyFrom(ByteArray.fromHexString(address)), response);
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      PostParams params = PostParams.getPostParams(request);
      Account.Builder build = Account.newBuilder();
      JsonFormat.merge(params.getParams(), build, params.isVisible());
      fillResponse(params.isVisible(), build.getAddress(), response);
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  private void fillResponse(boolean visible, ByteString address, HttpServletResponse response)
      throws Exception {
    AssetIssueList reply = wallet.getAssetIssueByAccountParallel(address);
    if (reply != null) {
      //String result = JsonFormat.printToString(reply, visible);
      String result=  serializeAssetList(reply,visible);
      response.getWriter().println(result);
    } else {
      response.getWriter().println("{}");
    }
  }
}
