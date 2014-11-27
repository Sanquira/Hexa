package hexapaper.network.server;

import hexapaper.entity.HPEntity;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import network.command.interfaces.CommandListener;
import network.command.users.CommandServer;
import network.core.interfaces.ClientConnectListener;
import network.core.interfaces.ClientDisconnectListener;
import network.core.interfaces.PacketReceiveListener;
import network.core.source.ClientInfo;
import network.core.source.MessagePacket;

public class ServerListeners {
	private CommandServer server;
	private int gridSl,gridRa,RADIUS;
	private ArrayList<HPEntity> souradky=null;
	private ArrayList<HPEntity> DBArtefact=null;
	private ArrayList<HPEntity> DBCharacter=null;
	private ConcurrentMap<String,String> versions=new ConcurrentHashMap<String,String>();
	private ClientInfo PJ=null;
	//ClientConnectListeners
	ClientConnectListener connect=new ClientConnectListener(){
		public void clientConnect(ClientInfo c) {
			if(PJ!=null){
				Object[] o={gridSl,gridRa,RADIUS};
				c.send(o, "RadiusHexapaper");
				c.send(souradky, "EntityHexapaper");
				//PJ.send(c.getNick(),versions,"PlayerConnect");
				//c.send(Cursorloc,"PJcursor");
				//c.send(DBArtefact,"DBartefact");
				//c.send(DBCharacter,"DBcharacter");
			}
			System.out.println("Client připojen "+(String) c.getNick());
			server.rebroadcast(c.getNick(),versions,"PlayerConnect");
//			try {
//				c.send(0, "version");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}			
	};
	//ClientDisconnectListeners
	ClientDisconnectListener disconnect=new ClientDisconnectListener(){
		public void clientDisconnect(ClientInfo c) {
			String Message;
			Message="Client odpojen: "+c.getNick();
			if(PJ!=null){
				if(PJ.equals(c)){
					Message="PJ odpojen: "+c.getNick();
					PJ=null;					
				}
				else{
					PJ.send(c.getNick(), 0,"PlayerDisconnect");
				}
			}
			System.out.println(Message);
			
		}		
	};
	//ReceiveListeners
	PacketReceiveListener RadiusHexapaper=new PacketReceiveListener(){
		public void packetReceive(MessagePacket p) {
			System.out.println("Radius Hexapaperu přijat"); 
			Object[] List = (Object[]) p.getObject();
			gridSl=(int) List[0];
			gridRa=(int) List[1];
			RADIUS=(int) List[2];
			server.rebroadcast(p.getNick(), List,"RadiusHexapaper");
		}		
	};
	PacketReceiveListener EntityHexapaper=new PacketReceiveListener(){
		public void packetReceive(MessagePacket p) {
			System.out.println("Entity Hexapaperu přijaty"); 
			souradky=(ArrayList<HPEntity>) p.getObject();
			server.rebroadcast(p.getNick(), souradky,"EntityHexapaper");
		}		
	};
	PacketReceiveListener EntChangeName=new PacketReceiveListener(){
		@Override
		public void packetReceive(MessagePacket p) {
			Object[] table=(Object[]) p.getObject();
			System.out.println(table[0]+":"+table[1]+":"+table[2]);
			for(HPEntity ent:souradky){
				if(ent.loc.getX()==(Integer) table[0]&&ent.loc.getY()==(Integer) table[1]){
					ent.setTag((String) table[2]);
					System.out.println("Změnen nick a tag Entity");
				}
			}
			server.rebroadcast(p.getNick(), p.getObject(),"EntChangeTag");
		}		
	};
	PacketReceiveListener DBa=new PacketReceiveListener(){
		public void packetReceive(MessagePacket p) {
			System.out.println("Artefacty přijaty"); 
			DBArtefact=(ArrayList<HPEntity>) p.getObject();
			server.rebroadcast(p.getNick(), DBArtefact,"DBartefact");
		}		
	};
	PacketReceiveListener insertEnt=new PacketReceiveListener(){
		@Override
		public void packetReceive(MessagePacket p) {
			//System.out.println("Test ent!");
			Object[] table=(Object[]) p.getObject();
			if((Integer) table[0]<souradky.size()){
				souradky.set((Integer) table[0], ((HPEntity) table[1]).clone());
				server.rebroadcast(p.getNick(), p.getObject(),p.getHeader());
			}
		}		
	};
	PacketReceiveListener paintEnt=new PacketReceiveListener(){
		@Override
		public void packetReceive(MessagePacket p) {
			Object[] table=(Object[]) p.getObject();
			if((Integer) table[0]<souradky.size()){
				souradky.get((Integer) table[0]).setBcg((Color) table[1]);
				server.rebroadcast(p.getNick(), p.getObject(),p.getHeader());
			}
		}		
	};
	PacketReceiveListener DBc=new PacketReceiveListener(){
		public void packetReceive(MessagePacket p) {
			System.out.println("Postavy přijaty"); 
			DBCharacter=(ArrayList<HPEntity>) p.getObject();
			server.rebroadcast(p.getNick(), DBCharacter,"DBcharacter");
		}		
	};
	PacketReceiveListener rotateEnt=new PacketReceiveListener(){

		@Override
		public void packetReceive(MessagePacket p) {
			Integer[] table=(Integer[]) p.getObject();
			//System.out.println(table[0]+":"+table[1]+":"+table[2]);
			for(HPEntity ent:souradky){
				if(ent.loc.getX()==table[0]&&ent.loc.getY()==table[1]){
					ent.loc.setDir(table[2]);
					//System.out.println("Předělána entita");
				}
			}
			server.rebroadcast(p.getNick(), p.getObject(),"rotateEnt");
		}
		
	};
	PacketReceiveListener dice=new PacketReceiveListener(){
		@Override
		public void packetReceive(MessagePacket p) {
			Integer roll=((Integer[]) p.getObject())[0];
			Integer range=((Integer[]) p.getObject())[1];
			Integer modifier=((Integer[]) p.getObject())[2];
			String Message;
			if(modifier==0){
				Message=p.getNick()+" si hodil "+roll+" na "+range+" kostce.";
			}
			else{
				Message=p.getNick()+" si hodil "+(roll+modifier)+" na "+range+" kostce se základním hodem "+roll;
			}
			System.out.println(Message);
			if(PJ!=null){
				PJ.send(p.getNick(), p.getObject(), "dice");
			}
		}		
	};
	PacketReceiveListener versionReceive=new PacketReceiveListener(){
		@Override
		public void packetReceive(MessagePacket p) {
			versions.put(p.getNick(),(String) p.getObject());
			System.out.println(p.getNick()+" má verzi "+(String) p.getObject());
			if(PJ!=null){
				//PJ.send(versions,"versionUpdate");
			}
		}		
	};
	//CommandListeners
	private CommandListener setPJ=new CommandListener(){
		public void CommandExecuted(List<String> args) {
			ClientInfo c=server.getNetworkStorage().getClientByName(args.get(0));
			if (c!=null){
				if(PJ!=null){
					PJ.send(0, "removePJ");
				}	
				PJ=c;
				c.send(0, "requestPJInfo");
				System.out.println("Hráči " +c.getNick()+" byl nastaven PJ");
				return;
			}
			System.out.println("Player "+args.get(0)+" is not connected");
		}		
	};
	private CommandListener isPJ=new CommandListener(){
		public void CommandExecuted(List<String> args) {
			if(PJ!=null){
				System.out.println("PJ is "+PJ.getNick());
				return;
			}
			System.out.println("PJ is not defined");		
		}	
	};
	private CommandListener kick=new CommandListener(){
		@Override
		public void CommandExecuted(List<String> args) {
			ClientInfo client = server.getNetworkStorage().getClientByName(args.get(0));			
			String message=(String) args.get(1);
			client.kick(message);	
		}		
	};
	private CommandListener dicecmd=new CommandListener(){
		@Override
		public void CommandExecuted(List<String> args) {
			Integer[] o = {(Integer) Integer.valueOf(args.get(0)),(Integer) Integer.valueOf(args.get(1))};
			if(PJ!=null){
				System.out.println("(Příkaz)"+args.get(2)+" si hodil "+args.get(0)+" na "+args.get(1)+" kostce.");
				PJ.send(args.get(2), o, "dice");
				return;
			}
			System.out.println("PJ is not selected");
		}		
	};
	private CommandListener version=new CommandListener(){
		@Override
		public void CommandExecuted(List<String> args) {
			ClientInfo c=server.getNetworkStorage().getClientByName(args.get(0));
			c.send(0, "version");
		}		
	};
	public ServerListeners(CommandServer s){
		this.server=s;
		s.addClientConnectListener(connect);
		s.addClientDisconnectListener(disconnect);
		s.addReceiveListener(EntityHexapaper, "EntityHexapaper");
		s.addReceiveListener(RadiusHexapaper, "RadiusHexapaper");
		s.addReceiveListener(DBa, "DBartefact");
		s.addReceiveListener(DBc, "DBcharacter");
		s.addReceiveListener(rotateEnt, "rotateEnt");
		s.addReceiveListener(insertEnt, "insertEnt");
		s.addReceiveListener(paintEnt,"paintEnt");
		s.addReceiveListener(EntChangeName, "EntChangeTag");
		s.addReceiveListener(dice, "dice");
		s.addReceiveListener(versionReceive,"version");
		s.registerCommand("pj", 1, "pj <Name>", "Check if player is PJ", isPJ);
		s.registerCommand("setpj", 1, "setpj <Name>", "Set PJ", setPJ);
		s.registerCommand("kick", 2, "Kick <Name> <Reason>", "Kick player", kick);
		s.registerCommand("dice", 3, "Dice <Roll> <Side> <Player>", "Hodí za hráče", dicecmd);
		s.registerCommand("version", 1, "Version <Name>", "Požádá hráče o verzi clienta", version);
	}
}
