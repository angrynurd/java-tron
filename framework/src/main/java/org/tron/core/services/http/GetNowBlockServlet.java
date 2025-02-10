package org.tron.core.services.http;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Block;

@Component
@Slf4j(topic = "API")
public class GetNowBlockServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  private BlockCacheProvider blockCacheProvider;

  @PostConstruct
  public void init() {
    blockCacheProvider = new BlockCacheProvider(wallet);
    // 预热特定场景
    JsonFormatWarmer.warmupBlock();
  }


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      if (visible) {
        response.getWriter().println(blockCacheProvider.getNowBlock());
        return;
      }
      Block reply = wallet.getNowBlock();
      if (reply != null) {
        response.getWriter().println(JsonFormat.printToString(reply, visible));
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