package org.tron.core.services.http;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.core.Wallet;

import static org.tron.core.services.http.AssetIssueUtil.serializeAssetList;

@Component
@Slf4j(topic = "API")
public class GetAssetIssueListServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  @PostConstruct
  public void init() {
    // 预热特定场景
    JsonFormatWarmer.warmuptrc10();
  }


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      AssetIssueList reply = wallet.getAssetIssueList();
      if (reply != null) {
        //String result = JsonFormat.printToString(reply, visible);
        String result=  serializeAssetList(reply,visible);
        response.getWriter().println(result);
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }
}
