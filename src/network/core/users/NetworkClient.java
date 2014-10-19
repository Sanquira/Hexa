package network.core.users;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import network.core.interfaces.ConnectListener;
import network.core.interfaces.DisconnectListener;
import network.core.source.MessagePacket;
import network.core.source.PacketReceiveHandler;

public class NetworkClient extends AbstractNetworkUser{
    private Socket socket=null;
    private String nick;
    private ObjectOutputStream o;
    private ObjectInputStream i;
	    public void addDisconnectListener(DisconnectListener l){
	    	sk.disconnectListeners.add(l);
	    }
	    public void addConnectListener(ConnectListener l){
	    	sk.connectListeners.add(l);
	    }
	    public NetworkClient(){
	    	socket = new Socket();
	    }
	    public void connect(String host, int port, String nick) throws IOException,UnknownHostException{
  	    	socket.connect(new InetSocketAddress(host, port), 1000);
	    	this.nick=nick;
            i=new ObjectInputStream(socket.getInputStream());
            o=new ObjectOutputStream(socket.getOutputStream());
            Thread t=new Thread(new PacketReceiveHandler(i,socket));
            t.start();
            o.writeObject(null);
            o.writeObject(new MessagePacket(nick,"connect", null));
            o.writeObject(null);            
            sk.callConnectEvent(socket);
	    }
	    public void close() throws IOException{
	    	socket.close();
	    }
	    public Socket getSocket(){
	    	return socket;
	    }
	    public void send(Object ob) throws IOException{
	    	o.writeObject(new MessagePacket(nick, ob));
	    }
	    public void send(Object ob,String header) throws IOException{
	    	o.writeObject(new MessagePacket(nick,header, ob));
	    }
	    public void disconnect() throws IOException{
			i.close();
			o.close();	    	    	
	    }
}

