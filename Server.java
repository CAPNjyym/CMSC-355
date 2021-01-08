   import java.io.*;
   import java.net.*;
   import java.util.ArrayList;
   import java.util.HashMap;

    public class Server extends Thread
   {
      
      private ServerSocket socket;
      private Thread connector;
      private ArrayList<ReceiveThread> receiveThreads;
      private int phase;
      private boolean waitState;
      private boolean running;
   
       public Server(String name)
      {
         super(name);
         waitState=false;
         phase =0;
         initNet();
         connector = 
             new Thread()
            {
                public void run()
               {
                  while(true)
                  {
                     try
                     {
                        Socket tempSocket = socket.accept();
                        BufferedReader tempReader = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
                        PrintStream tempOut = new PrintStream(tempSocket.getOutputStream());
                        
                        receiveThreads.add(new ReceiveThread(receiveThreads.size(),tempSocket,tempReader,tempOut));
                        receiveThreads.get(receiveThreads.size()-1).start();
                     }
                         catch(Exception e){e.printStackTrace();}
                  }
               }
            };
         connector.start();
      }
    
       public void reinit()
      {
         initNet();
      }
     
       public void setRunning(boolean b)
      {
         running = b;
      }
   	
       private void initNet()
      {
         try 
         {
            receiveThreads = new ArrayList<ReceiveThread>();
            if(socket==null)
               socket = new ServerSocket(IP.SERVER_PORT);
            running = true;
         }
             catch(Exception e){ e.printStackTrace(); }
      }
    
       public void close()
      {
         if (connector != null)
         {
            connector.stop();
            connector=null;
         }
         for(int t=0; t<receiveThreads.size(); t++)
         {
            // check this here IndexOutOfBoundsException: Index 0, Size 0
            try {receiveThreads.get(t).close();}
                catch (java.lang.IndexOutOfBoundsException e){};
            try {receiveThreads.get(t).stop();}
                catch (java.lang.IndexOutOfBoundsException e){};
         }
      }  
   	
       public void run()
      {
         while(running)
         {
            //System.out.println(phase+" " +receiveThreads.size());
            if(receiveThreads.size()!=0)
            {
               if(phase<receiveThreads.size())
               {
                  for(int t=0; t< receiveThreads.size(); t++)
                  {
                     if(receiveThreads.get(t).isReadyToPlay())
                        phase++;
                     else
                        phase=0;
                     
                  }
               }
               else if(phase == receiveThreads.size())
               {
                  sendToAll(IP.SEND_START_ROUND+"@ ");
                  for(int t=0; t< receiveThreads.size(); t++)
                     receiveThreads.get(t).setReadyToPlay(false);
                  phase = 5;
               }
               else if(phase==5)
               {
                  waitState = false;
                  for(int t=0; t< receiveThreads.size(); t++)
                     if(!receiveThreads.get(t).isReadyToPlay())
                        waitState=true;
                  if(!waitState)
                  {
                     for(int t=0; t< receiveThreads.size(); t++)
                        receiveThreads.get(t).setReadyToPlay(false);
                     sendToAll(IP.SEND_NEXT_TURN+"@ ");
                     waitState=true;
                  }     
               }
            }
            try
            {
               this.sleep(100);
            }
                catch(Exception e){}
         }
      }
    
       public String[] getAttached()
      {
         String[] temp = new String[receiveThreads.size()];
         for(int t=0; t<receiveThreads.size(); t++)
         {
            //temp[t]=receiveThreads.get(t).getSocket().getInetAddress().toString().split("/")[1];
            temp[t] = receiveThreads.get(t).getPlayerName();
            while(temp[t].equals(" "))
               temp[t] = receiveThreads.get(t).getPlayerName();
           // System.out.println(temp[t]);
         }
         return temp;
      }  
   	   
       public void sendToAll(String message)
      {
         for(int t=0; t<receiveThreads.size(); t++)
         {
            receiveThreads.get(t).getOut().println(message);
         }
      }
   	
   	//****************************************************************************8
   	
       class ReceiveThread extends Thread
      {
         private Socket socket;
         private BufferedReader in;
         private PrintStream out;
         private int index;
         private String playerName;
         private boolean readyToPlay;
      
          public ReceiveThread(int t, Socket s, BufferedReader i, PrintStream o)
         {
            super(t+"");
            playerName = new String(" ");
            readyToPlay = false;
            index=t;
            socket = s;
            in = i;  
            out= o;
         }
       
          private void removeClient()
         {
            receiveThreads.remove(index);
            close(); 
         }  
         
          public String getPlayerName()
         {
            return playerName;
         }
      	
          public boolean isReadyToPlay()
         {
            return readyToPlay;
         }
      	
          public void setReadyToPlay(boolean b)
         {
            readyToPlay = b;
         }
      	
          public PrintStream getOut()
         {
            return out;
         }
       
          public Socket getSocket()
         {
            return socket;
         }  
      	
          public void close()
         {
            try
            {
               
               out.close();
               in.close();  
               socket.close();
            }
                catch(Exception e){}
         }
      	
          public void run()
         {
            byte tempType = 0;
            String tempRead = new String();
            while(true)
            {
               try{
                  if((tempRead=in.readLine())!=null)
                  {
                     tempType = Byte.parseByte(tempRead.split("@")[0]);
                     switch(tempType)
                     {
                        case IP.PLAYER_CONNECT:
                           playerName = tempRead.split("@")[1];
                           break;
                        case IP.PLAYER_DISCONNECT:
                           sendToAll(IP.SEND_PLAYER_KILL+"@ ");
                           removeClient();
                           break;
                        case IP.TURN_FINISHED:
                           readyToPlay=true;
                           break;
                        case IP.PLAYER_MOVE:
                           sendToAll(IP.SEND_PLAYER_MOVE+"@"+tempRead.split("@")[1]+"@"+tempRead.split("@")[2]);
                           break;
                        case IP.PLAYER_SHOOT:
                        //receieved an X,Y,projectile index,power,angle,direction
                           sendToAll(IP.SEND_PLAYER_SHOOT+"@"+tempRead.substring(2));
                           break;
                        case IP.GAME_START:
                           sendToAll(IP.SEND_GAME_SYNC+"@"+tempRead.substring(2));
                           break;
                        case IP.SYNC_COMPLETE:
                           readyToPlay=true;
                           System.out.println(playerName+" at "+socket.getInetAddress()+" is ready to play!");
                           break;
                     }
                  }
               }
                   catch(SocketException e){removeClient();}
                   catch(Exception e){this.stop();}
            }
         }
      	
      }
   }