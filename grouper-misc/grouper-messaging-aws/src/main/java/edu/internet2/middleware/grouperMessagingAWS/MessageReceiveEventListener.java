package edu.internet2.middleware.grouperMessagingAWS;

import java.util.EventListener;

public interface MessageReceiveEventListener extends EventListener {
  
  public void messageReceived(String messageBody);

}
