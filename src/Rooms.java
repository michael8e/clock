import java.io.IOException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.List;

public class Rooms extends Composite {
	private Text text;
	private final static String EXCHANGE_NAME = "MyZone";
	private StyledText txt;
	private String roomname;
	private String name;
	private String serverip;
	private Channel channel;
	private Connection connection;
	private Thread thread;
	private boolean leave;
	private List list;
	private Button btnBroadcast;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public Rooms(Composite parent, int style, final String name, String ip, final String selfname) {
		super(parent, style);
		roomname = name;
		serverip = ip;
		this.name = selfname;
		leave = false;
		
		text = new Text(this, SWT.BORDER);
		text.setBounds(10, 10, 306, 25);
		
		Button button = new Button(this, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (text.getText() != "") {
					String message = selfname + ":" + text.getText();
					String to = "";
					if (list.getSelectionCount() > 0) 
						to = list.getSelection()[0];
					SendMessage(message, btnBroadcast.getSelection(), to);
					text.setText("");
				}
			}
		});
		button.setText("send");
		button.setBounds(337, 10, 80, 25);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setBounds(10, 41, 306, 193);
		
		txt = new StyledText(scrolledComposite, SWT.BORDER);
		txt.setEditable(false);
		scrolledComposite.setContent(txt);
		scrolledComposite.setMinSize(txt.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Leave();
			}
		});
		btnNewButton.setBounds(236, 240, 80, 25);
		btnNewButton.setText("Leave");
		
		list = new List(this, SWT.BORDER);
		list.setBounds(337, 43, 80, 191);
		
		btnBroadcast = new Button(this, SWT.CHECK);
		btnBroadcast.setSelection(true);
		btnBroadcast.setBounds(337, 244, 98, 16);
		btnBroadcast.setText("broadcast");
		
		thread = new Thread(new Runnable(){
			public void run() {
				startServer();
			}
		});
		thread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		SendMessage(selfname + " enter the room", true, "");
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected void SendMessage(String message, boolean broadcast, String to){
		if (leave) 
			return;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(serverip);
		Connection connection;
		try {
			connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, "direct");
			if (broadcast)
				channel.basicPublish(EXCHANGE_NAME, roomname, null, message.getBytes());
			else 
				channel.basicPublish(EXCHANGE_NAME, roomname + "." + to, null, message.getBytes());
			channel.close();
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void startServer() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(serverip);
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			String queuename = channel.queueDeclare().getQueue();
			channel.exchangeDeclare(EXCHANGE_NAME, "direct");
			channel.queueBind(queuename, EXCHANGE_NAME, roomname);
			channel.queueBind(queuename, EXCHANGE_NAME, roomname + "." + name);
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queuename, true, consumer);
			
			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody());
				int index = message.indexOf(":");
				if (index == -1) {
					if (message.endsWith("enter the room")) {
						String to = message.substring(0, message.indexOf(" "));
						add(to);
						SendMessage("update " + name, false, to);
						addMessage(message);
					}
					else if (message.endsWith("leave the room")) {
						remove(message.substring(0, message.indexOf(" ")));
						addMessage(message);
					}
					else if (message.startsWith("update"))
						add(message.substring(message.indexOf(" ") + 1));
					else if (message.startsWith("+showuser")) {
						int index1 = message.indexOf(" ");
                        String to = message.substring(index1 + 1);
                        SendMessage("-" + name, false,to);
					}
				}
				else 
					addMessage(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void Leave() {
		try {
			if (channel.isOpen()) {
				channel.close();
				connection.close();
			}
			thread.interrupt();
			SendMessage(name + " leave the room", true, "");
			leave = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected String getRoom(){
		return roomname;
	}
	
	protected void addMessage(final String message) {
		Display.getDefault().syncExec(new Runnable(){
			public void run() {
				txt.setText(txt.getText() + message + "\n");
			}
		});
	}
	protected void add(final String nm) {
		Display.getDefault().syncExec(new Runnable(){
			public void run() {
				if (list.indexOf(nm) == -1)
					list.add(nm);
			}
		});
	}
	protected void remove(final String nm) {
		Display.getDefault().syncExec(new Runnable(){
			public void run() {
				list.remove(nm);
			}
		});
	}
}
