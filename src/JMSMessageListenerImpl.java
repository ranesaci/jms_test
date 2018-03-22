import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSMessageListenerImpl implements MessageListener {

	// for JNDI_FACTORY
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

	// for PROVIDER_URL
	public final static String PROVIDER_URL = "t3://localhost:7001";

	// for JMS Connection factory set in weblogic server
	public final static String JMS_CON_FACTORY = "CF1";

	// Queue JNDI name set in weblogic server
	public final static String QUEUE = "Queue-0";

	private QueueConnectionFactory qconFactory;

	private QueueConnection qcon;

	private QueueSession qsession;

	private QueueReceiver qreceiver;

	private Queue queue;

	private TextMessage msg;

	private boolean flag = false;

	public static void main(String[] args) throws NamingException, JMSException, InterruptedException {
		InitialContext initialContext = getInitialContext();

		JMSMessageListenerImpl listenerImpl = new JMSMessageListenerImpl();
		listenerImpl.initAndStartQueue(initialContext);

		// receive queue in loopp till we break condition to go out of listening

		synchronized (listenerImpl) {
			while (!listenerImpl.flag) {

				listenerImpl.wait();
			}

		}
		listenerImpl.close();

	}

	private void initAndStartQueue(InitialContext initialContext) throws NamingException, JMSException {
		qconFactory = (QueueConnectionFactory) initialContext.lookup(JMS_CON_FACTORY);

		qcon = qconFactory.createQueueConnection();

		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

		queue = (Queue) initialContext.lookup(QUEUE);

		qreceiver = qsession.createReceiver(queue);

		qreceiver.setMessageListener(this);

		qcon.start();

	}

	private void close() throws JMSException {
		qreceiver.close();

		qsession.close();

		qcon.close();

	}

	private static InitialContext getInitialContext() throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();

		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);

		env.put(Context.PROVIDER_URL, PROVIDER_URL);

		return new InitialContext(env);
	}

	@Override
	public void onMessage(Message msg) {
		
		String msgText = null;
		 
		if (msg instanceof TextMessage) {
		 
			try {
				msgText = ((TextMessage)msg).getText();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		 
		} else {
		 
		msgText = msg.toString();
		 
		}
		 
		System.out.println("Message Received: "+ msgText );
		if (msgText.equalsIgnoreCase("quit")) {
			 
			synchronized(this) {
			flag = true;
			this.notifyAll(); // Notify main thread to quit
			 
			}
		}
		

	}

}
