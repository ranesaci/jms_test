import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSDemo {

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

	private QueueSender qsender;

	private Queue queue;

	private TextMessage msg;

	public static void main(String[] args) throws NamingException, JMSException {

		//Step 1:  get initial context with JNDI factory and Provider url
		InitialContext initialContext = getInitialContext();
		
		//step 2: initialise queue and create session for sending text message to queue
		JMSDemo jmsDemo = new JMSDemo();
		jmsDemo.initAndStartQueue(initialContext);
		
		//step 3: send message
		jmsDemo.sendMessage("This is message 22" );
		
		
		//step 4: close opened connections
		jmsDemo.close();
		
		
		

	}

	private void close() throws JMSException {
		qsender.close();
		 
		qsession.close();
		 
		qcon.close();
		
	}

	private  void sendMessage(String message) throws JMSException {
		msg.setText(message);
		qsender.send(msg);
		System.out.println("Message sent:"+message);
		
	}

	private  void initAndStartQueue(InitialContext initialContext) throws NamingException, JMSException {
		qconFactory = (QueueConnectionFactory) initialContext.lookup(JMS_CON_FACTORY);
		 
		qcon = qconFactory.createQueueConnection();
		 
		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		 
		queue = (Queue) initialContext.lookup(QUEUE);
		 
		qsender = qsession.createSender(queue);
		 
		msg = qsession.createTextMessage();
		 
		qcon.start();
		
	}

	private static InitialContext getInitialContext() throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();

		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);

		env.put(Context.PROVIDER_URL, PROVIDER_URL);

		return new InitialContext(env);
	}

}
