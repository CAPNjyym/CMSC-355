    /* 
     * 9/17/09 6:00 Alex
     * Added packet type definitions to this class
     *
     */
    
    public class IP
   {
      private static String url = "http://svn.webkraller.com/CoD/update.php";
      public static int SERVER_PORT = 5081;
      public static int CLIENT_PORT = 5082;
    
      //SERVER RECIEVE PACKET TYPES
      public static final byte PLAYER_CONNECT = 0;   //PLAYERNAME
      public static final byte PLAYER_DISCONNECT = 1;  //<nothing>
      public static final byte PLAYER_MOVE = 2;  //PLAYER INDEX @ DX
      public static final byte PLAYER_SHOOT = 3; //PLAYER INDEX @ ANGLE @ POWER @ PROJECTILE_INDEX
      public static final byte PROJECTILE_MOVE = 4; //hopefully don't need
      public static final byte PROJECTILE_HIT = 5; //hopefully don't need
      public static final byte SYNC_COMPLETE = 6; //<nothing>
      public static final byte GAME_START = 7; //background@terrain@startIndex@Playername,index,x,y,color,ai@...@...@...
      public static final byte TURN_FINISHED = 8; //make sure everyone is in sync. host sends SEND_PLAYERS_HEALTH then client sends this
   
      //SERVER SEND PACKET TYPES
      public static final byte SEND_PLAYERS_HEALTH = 20; //player[0].health @ player[1].health @ player[2].health @ player[3].health
      public static final byte SEND_NEXT_TURN = 21;  //currentPlayerTurn
      public static final byte SEND_PLAYER_MOVE = 22; //PLAYERNAME @ DX @ DIRECTION
      public static final byte SEND_PLAYER_SHOOT = 23; //PLAYERNAME @ ANGLE @ POWER @ PROJECTILE_INDEX
      public static final byte SEND_PROJECTILE_MOVE = 24; //hopefully don't need
      public static final byte SEND_PROJECTILE_HIT = 25; //hopefully don't need
      public static final byte SEND_LOBBY_PLAYERS = 26; //PLAYERNAME , PLAYERNAME , ...
      public static final byte SEND_GAME_SYNC = 27; //background@terrain@startIndex@Playername,index,x,y,color,ai@...@...@...
      public static final byte SEND_START_ROUND = 28;
		public static final byte SEND_PLAYER_KILL = 29; //used if a client disconnects mid-game
      	
       public static java.net.InetAddress getServer(String s) throws Exception
      {
         return java.net.InetAddress.getLocalHost();
      }  
   
       public static java.net.InetAddress getServer() throws Exception
      {
         return ipFromString(wget(url));
      }
   	
       public static void setServer() throws Exception
      {
         wget(url+"?address="+java.net.InetAddress.getLocalHost().toString().split("/")[1]);
      }
   	
       public static void removeServer() throws Exception
      {
         wget(url+"?address=none");
      }
       
       public static String wget(String url)
      {
         String s = "";
         try
         {
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(new java.net.URL(url).openStream()));
            String line;
            while( (line=in.readLine()) != null)
               s+=line;
            in.close();
         }
             catch(java.io.IOException e) {}
         return s;
      }
   
       public static java.net.InetAddress ipFromString(String ip) throws Exception
      {
         int t;
         byte[] x = new byte[4];
         x[0]=new Integer(ip.substring(0,t=ip.indexOf("."))).byteValue();
         x[1]=new Integer(ip.substring(++t,t=ip.indexOf(".",t))).byteValue();
         x[2]=new Integer(ip.substring(++t,t=ip.indexOf(".",t))).byteValue();
         x[3]=new Integer(ip.substring(++t)).byteValue();
         return java.net.InetAddress.getByAddress(x);
      }  
   }