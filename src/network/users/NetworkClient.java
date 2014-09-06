package network.users;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import network.interfaces.ConnectListener;
import network.interfaces.DisconnectListener;
import network.source.MessagePacket;
import network.source.PacketReceiveHandler;

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
        try{	
	    	socket.connect(new InetSocketAddress(host, port), 1000);
	    	this.nick=nick;
            i=new ObjectInputStream(socket.getInputStream());
            o=new ObjectOutputStream(socket.getOutputStream());
            Thread t=new Thread(new PacketReceiveHandler(i,socket));
            t.start();
            //o.writeObject(null);
            //o.writeObject(new MessagePacket(nick, "initial"));
            send("initial");
            sk.callConnectEvent(socket);
        } catch (IOException e) {
            close();
        }
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
}


