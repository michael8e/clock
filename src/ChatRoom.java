import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DisposeEvent;

public class ChatRoom extends Shell {

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		
		try {
			Display display = Display.getDefault();
			ChatRoom shell = new ChatRoom(display, SWT.CLOSE);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Text nameEnter;
	private Text ipEnter;
	private Text roomName;
	private Button btnLogIn;
	private Text text;
	private CTabFolder tabFolder;
	private ArrayList<Rooms> room;
	
	public ChatRoom(Display display, int style) {
		super(display, style);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				for (int i = 0; i < room.size(); i++)
					room.get(i).Leave();
			}
		});
		setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		setLocation(new Point(490, 134));
		
		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Pattern pattern = Pattern.
						compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
								"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
								"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
								"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");

				Matcher matcher = pattern.matcher(ipEnter.getText());
				if (roomName.getText() != "" && !btnLogIn.isEnabled() && matcher.matches())
					createRoom(roomName.getText(), ipEnter.getText());
				else if (roomName.getText() == "")
					MessageDialog.openError(getShell(), "Room Name Wrong", "Room Name cannot be empty");
				else if (btnLogIn.isEnabled())
					MessageDialog.openError(getShell(), "Log in Wrong", "You must log in first");
				else if (!matcher.matches())
					MessageDialog.openError(getShell(), "Ip Format Wrong", "ip format wrong");
			}
		});
		btnNewButton.setBounds(459, 182, 109, 28);
		btnNewButton.setText("Create Room");
		
		Button btnJoinRoom = new Button(this, SWT.NONE);
		btnJoinRoom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Pattern pattern = Pattern.
						compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
								"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
								"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
								"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");

				Matcher matcher = pattern.matcher(ipEnter.getText());
				if (matcher.matches() && text.getText() != "" && !btnLogIn.isEnabled()) 
					createRoom(text.getText(), ipEnter.getText());
				else if (!matcher.matches())
					MessageDialog.openError(getShell(), "Ip Format Wrong", "ip format wrong");
				else if ( btnLogIn.isEnabled())
					MessageDialog.openError(getShell(), "Log in Wrong", "You must log in first");
				else if (text.getText() == "")
					MessageDialog.openError(getShell(), "Room Name Wrong", "Room Name cannot be empty");
			}
		});
		btnJoinRoom.setText("Join Room");
		btnJoinRoom.setBounds(459, 284, 109, 28);
		
		nameEnter = new Text(this, SWT.BORDER);
		nameEnter.setBounds(495, 10, 73, 21);
		
		btnLogIn = new Button(this, SWT.NONE);
		btnLogIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (nameEnter.getText() != "") {
					btnLogIn.setEnabled(false);
					nameEnter.setEditable(false);
				}
			}
		});
		btnLogIn.setBounds(488, 37, 80, 25);
		btnLogIn.setText("log in");
		
		ipEnter = new Text(this, SWT.BORDER);
		ipEnter.setText("127.0.0.1");
		ipEnter.setBounds(459, 89, 109, 21);
		
		Label lblIp = new Label(this, SWT.NONE);
		lblIp.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblIp.setBounds(459, 68, 61, 15);
		lblIp.setText("serverip");
		
		tabFolder = new CTabFolder(this, SWT.BORDER);
		tabFolder.setBounds(10, 10, 443, 302);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmWelcome = new CTabItem(tabFolder, SWT.CLOSE);
		tbtmWelcome.setText("Welcome");
		tabFolder.setSelection(tbtmWelcome);
		
		Label lblHello = new Label(tabFolder, SWT.NONE);
		lblHello.setImage(SWTResourceManager.getImage("D:\\program files\\eclipse\\project\\NetChat\\assets\\welcome.png"));
		tbtmWelcome.setControl(lblHello);
		
		roomName = new Text(this, SWT.BORDER);
		roomName.setBounds(459, 155, 109, 21);
		
		Label lblRoomName = new Label(this, SWT.NONE);
		lblRoomName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblRoomName.setBounds(459, 134, 80, 15);
		lblRoomName.setText("Room Name");
		
		Label lblName = new Label(this, SWT.NONE);
		lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblName.setBounds(454, 13, 35, 15);
		lblName.setText("Name");
		
		Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(459, 126, 109, 2);
		
		Label label_1 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setBounds(459, 216, 109, 2);
		
		Label lblRoomName_1 = new Label(this, SWT.NONE);
		lblRoomName_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblRoomName_1.setBounds(459, 224, 80, 15);
		lblRoomName_1.setText("Room Name");
		
		text = new Text(this, SWT.BORDER);
		text.setBounds(459, 245, 109, 21);
		createContents();
		
		room = new ArrayList<Rooms>();
		
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Chat Room");
		setSize(594, 361);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected void createRoom(String roomID, String ip) {
		for(int i = 0; i < room.size(); i++) {
			if (room.get(i).getRoom().equals(roomID)) {
				MessageDialog.openError(getShell(), "Room Name Wrong", "Room Name cannot be created twice");
				return;
			}
		}
		final Rooms room = new Rooms(tabFolder, SWT.NONE, roomID, ip, nameEnter.getText());
		CTabItem tbtmNewItem = new CTabItem(tabFolder, SWT.CLOSE);
		tbtmNewItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				ChatRoom.this.room.remove(room);
				room.Leave();
			}
		});
		tbtmNewItem.setText(roomID);
		tbtmNewItem.setControl(room);
		tabFolder.setSelection(tbtmNewItem);
		this.room.add(room);
	}
}
