package cn.itcast.core.listener;

import cn.itcast.core.service.staticpage.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener implements MessageListener {

    @Resource
    private StaticPageService staticPageService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
        try {
            //取出消息
            String id = activeMQTextMessage.getText();
            //消费消息
            staticPageService.getHtml(Long.parseLong(id));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
