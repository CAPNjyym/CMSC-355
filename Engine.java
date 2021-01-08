//In-game Engine class

   import java.awt.*;
   import java.awt.image.BufferStrategy;
   import java.awt.image.BufferedImage;
   import java.awt.event.*;
   import java.net.*;
   import java.io.*;
   import javax.swing.JOptionPane;
   import java.util.Random;
   import java.util.ArrayList;
   import java.text.DecimalFormatSymbols;
   import java.applet.AudioClip;
	
    public class Engine extends Frame implements Runnable, KeyListener, MouseListener
   {
   
      private boolean debug = true;
   
      private Graphics2D g;			//Used for double buffering in windowed mode
      private Image backbuffer;		//Used for double buffering in windowed mode
      private Thread gameUpdateThread;
      private Level level;
      private Player localPlayer, startPlayer;
      private Font angleFont, healthFont, infFont, ammoFont;
      private boolean fire, powerUp, disableKeys;
      private double power, inc;
      private boolean left, right, up, down;
      private int count;
      private Color Brown;
      private long prevTime;
      private int prevAngle, prevPower;
      private Server server;
      private Client client;
      private boolean host;
      private int lobbyIndex;
      private int phase=-1; //-1=startup, 0=titlescreen, 1=player name change, 2=host setup level, 3=waiting for players, 4=join, 5=playing
      private int oldPhase;
      private Image title;
      private final String HOST = "HOST NETWORK GAME";
      private final String JOIN = "JOIN NETWORK GAME";
      private final String PLAYER_NAME_CHANGE = "CHANGE PLAYER NAME";
      private final String CHANGE_TERRAIN = "CHANGE TERRAIN";
      private final String LAUNCH_SERVER = "LAUNCH SERVER";
      private final String CHANGE_LEVEL = "CHANGE LEVEL";
      private int menuSelected;
      private String playerName;
      private int menuX, menuY;
      private int numHumanPlayers;
      private int numAIPlayers;
      private Image[] background;
      private BufferedImage[] terrain;
      private int currentBackground, currentTerrain;
      private String error;
      private ArrayList<String> lobbyPlayers;
   	
      private AudioClip lobbyBG;
   
      private AudioClip back;
      private AudioClip moveCurser;
      private AudioClip selectSetting;
   
      private boolean fullscreen;
      private GraphicsDevice device;
      private Image GUI, GUIback, GUIcover, wep[], selwep[];
      private BufferStrategy bufferStrategy;
      private static DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[] {
         new DisplayMode(1024, 768, 32, 0),
         new DisplayMode(1024, 768, 16, 0),
         new DisplayMode(1024, 768, 8, 0)
         };
   
   
      public final static String IMAGE_PATH = "Resources/Images/";
      public final static int HEIGHT = 768;
      public final static int WIDTH = 1024;
      public final static double GRAVITY = 9.8;
   
       public Engine(boolean fullscreen, GraphicsDevice device, GraphicsConfiguration gc)
      {
         super(gc);
         this.fullscreen=fullscreen;
         this.device = device;
         initGraphics();
         //localPlayer=
         //startPlayer =
      	//initLevel(p,r);
         loadLevels();
         loadPreferences();
         this.addKeyListener(this);
         this.addMouseListener(this);
         count = 1;
         prevAngle = -1;
         prevPower = -1;
      
         lobbyPlayers= new ArrayList<String>();
         gameUpdateThread = new Thread(this);
         gameUpdateThread.start();
      }
   
       private static DisplayMode getBestDisplayMode(GraphicsDevice device) {
         for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
            DisplayMode[] modes = device.getDisplayModes();
            for (int i = 0; i < modes.length; i++) {
               if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
                   && modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight()
                   && modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth()
                   ) {
                  return BEST_DISPLAY_MODES[x];
               }
            }
         }
         return null;
      }
   
       public static void chooseBestDisplayMode(GraphicsDevice device) {
         DisplayMode best = getBestDisplayMode(device);
         if (best != null) {
            device.setDisplayMode(best);
         }
      }
   
       private void initGraphics()
      {
         error=new String();
         Brown = new Color(96, 57, 19);
         angleFont = new Font("Monospaced", Font.BOLD, 40);
         healthFont = new Font("Monospaced", Font.BOLD, 13);
         infFont = new Font("Monospaced", Font.PLAIN, 16);
         ammoFont = new Font("Monospaced", Font.BOLD, 20);
         this.setSize(WIDTH, HEIGHT);
         this.setTitle("Call of Doodie");
         setLocation((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2-this.getWidth()/2,(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-this.getHeight()/2);
         setResizable(false);
         Image logo=Toolkit.getDefaultToolkit().getImage(IMAGE_PATH+"logo_icon.gif");
         setIconImage(logo);
         setUndecorated(true);
         setVisible(true);
         if(fullscreen)
         {
            try
            {
               device.setFullScreenWindow(this);
               if (device.isDisplayChangeSupported())
                  chooseBestDisplayMode(device);
               this.createBufferStrategy(2);
               bufferStrategy = this.getBufferStrategy();
               g = (Graphics2D)bufferStrategy.getDrawGraphics();
            }
                catch(Exception e){ e.printStackTrace(); }
         
         }
         else
         {
            backbuffer = this.createImage(getWidth(), getHeight());
            g = (Graphics2D)backbuffer.getGraphics();
         }
      
         LevelElement dummy = new LevelElement("dummy",0,0,this);
      
      	//lobby sounds
         try
         {
            lobbyBG = dummy.getAudio("Ambient Jungle Lobby BG");
            back = dummy.getAudio("Back");
            moveCurser = dummy.getAudio("Move Curser");
            selectSetting = dummy.getAudio("Select Setting");
         	
         }
             catch(Exception e){System.out.println("Problem loading lobby noise");}
         lobbyBG.stop();
         lobbyBG.loop();
      	
      	// GUI Images and Weapon Icons
         wep = new Image[6];
         selwep = new Image[6];
         String[] tempS = {"title.gif","Game GUI.gif","Game GUI back.gif","GUI Cover.gif","Banana.gif","Tomato.gif","Plantain.gif","Cannon.gif","Flea.gif","Poo.gif","Banana_Selected.gif","Tomato_Selected.gif","Plantain_Selected.gif","Cannon_Selected.gif","Flea_Selected.gif","Poo_Selected.gif"};
         Image[] temp = dummy.getImages(tempS);
         title = temp[0];
         GUI = temp[1];
         GUIback = temp[2];
         GUIcover = temp[3];
         for(int i=0;i<6;i++)
         {
            wep[i]=temp[i+4];
            selwep[i]=temp[i+10];
         }
         menuX=400;
         menuY=340;
      }
   
       private void createLevelAndPlayers(String tempInput)
      {
         if(level==null)
         {
            int BGIndex = Integer.parseInt(tempInput.split("@")[1]),
               levelIndex = Integer.parseInt(tempInput.split("@")[2]),
               firstPlayerIndex = Integer.parseInt(tempInput.split("@")[3]);
         
            Player[] players = new Player[4];
            String name;
            int index, x, y, colorIndex, AI;
            for(int i=0;i<4;i++)
            {
               name = tempInput.split("@")[i+4].split(",")[0];
               index = Integer.parseInt(tempInput.split("@")[i+4].split(",")[1]);
               x = Integer.parseInt(tempInput.split("@")[i+4].split(",")[2]);
               y = Integer.parseInt(tempInput.split("@")[i+4].split(",")[3]);
               colorIndex = Integer.parseInt(tempInput.split("@")[i+4].split(",")[4]);
               AI = Integer.parseInt(tempInput.split("@")[i+4].split(",")[5]);
               players[i] = new Player(name, index, x, y, colorIndex+1, 0);
            }
            //System.out.println(BGIndex+" "+levelIndex+" "+firstPlayerIndex+" "+lobbyIndex+" ");
            initLevel(players, BGIndex+1, levelIndex+1, firstPlayerIndex, lobbyIndex);
            client.send(IP.SYNC_COMPLETE+"@ ");
         }
      }
   
       private void initLevel(Player[] p, int back, int terrain, int r,int localPlayerIndex)
      {
         localPlayer = p[localPlayerIndex];
         String temp = new String();
         if(back<10)
            temp = "back 0"+back;
         else
            temp = "back "+back;
         level = new Level(temp,terrain,r,p,this);
      }
   
       private String findServer()
      {
         try
         {
            IP.getServer();
         }
             catch(Exception e)
            {
               return "No Open Servers Running!";
            }
         return "";
      }
   
       private String startServer(String n)
      {
         try
         {
            IP.getServer();
            return "Another server already running on network!";
         }
             catch(Exception e)
            {
               try
               {
                  IP.setServer();
               }
                   catch(Exception er){}
            }
         if(server!=null)
            server.reinit();
         else
         {
            server = new Server(n+"'s Game");
            server.start();
         }
         return "";
      }
   
       private void closeServer()
      {
         if(server!=null)
         {
            try
            {
               IP.removeServer();
            }
                catch(Exception e){}
            server.close();
            server.setRunning(false);
            //server.stop();
            //server=null;
         }
      }
   
       private void refreshLobbyPlayers(String[] players)
      {
         lobbyPlayers.clear();
         for(int t=0; t<players.length; t++)
         {
            if(players[t].equals(playerName))
               lobbyIndex = t;
            //System.out.println(lobbyIndex+ " "+playerName);
            lobbyPlayers.add(players[t]);
         }
      }
   
       private void startGame()
      {
         lobbyBG.stop();
         try
         {
            IP.removeServer();
            Player[] players = new Player[4];
            for(int t=0; t<server.getAttached().length; t++)
            {
               if(t==0)
                  players[t]= new Player(lobbyPlayers.get(t),t,t+1,0);
               if(t==1)
                  players[t]= new Player(lobbyPlayers.get(t),t,t+1,0);
               if(t==2)
                  players[t]= new Player(lobbyPlayers.get(t),t,t+1,0);
               if(t==3)
                  players[t]= new Player(lobbyPlayers.get(t),t,t+1,0);
            }
            Random rand = new Random();
            for(int t=server.getAttached().length; t<4; t++)
            {
               if(t==1)
                  players[t]= new Player("COMP-"+t,t,t+1,rand.nextInt(4)+1);
               if(t==2)
                  players[t]= new Player("COMP-"+t,t,t+1,rand.nextInt(4)+1);
               if(t==3)
                  players[t]= new Player("COMP-"+t,t,t+1,rand.nextInt(4)+1);
            }
         	
            for(int i=0;i<4;i++)
            {
               players[i].Y(50);
               players[i].X(rand.nextInt(924)+50);
               for(int j=0;j<i;j++)
               {
                  if (players[i].getCollisionBox().intersects(players[j].getCollisionBox()))
                  {
                     //System.out.println("intersected:  " +players[i].getX()+" & "+players[j].getX());
                     players[i].X(rand.nextInt(924)+50);
                     j=i;
                     i--;
                     break;
                  }
               }
            } 
         	
            initLevel(players,currentBackground+1,currentTerrain+1,rand.nextInt(4),0);
            String tempMessage = new String();
            tempMessage+=currentBackground+"@"+currentTerrain+"@"+level.getCurrentPlayerIndex()+"@";
            for(int t=0; t<players.length; t++)
               tempMessage+=players[t].getName()+","+t+","+players[t].getX()+","+players[t].getY()+","+t+","+players[t].getAI()+"@";
            tempMessage = tempMessage.substring(0,tempMessage.length()-1);
            client.send(IP.GAME_START+"@"+tempMessage);
            client.send(IP.SYNC_COMPLETE+"@ ");
         }
             catch(Exception e){}
      }
   
       private void loadLevels()
      {
         String temp = new String("");
         int counter = 0, halfway=1;
         Image[] tempImages = new Image[1];
         MediaTracker watch=new MediaTracker(this);
         try
         {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("Settings/levels.ini")));
            while((temp=in.readLine())!= null )
            {
               if(counter==0)
               {
                  halfway=Integer.parseInt(temp);
                  background= new Image[halfway];
               }
               
               else if(counter>0 && counter <=halfway)
               {
                  background[counter-1] = Toolkit.getDefaultToolkit().getImage(IMAGE_PATH+temp.split(",")[1]).getScaledInstance(133,100,Image.SCALE_DEFAULT);
                  watch.addImage(background[counter-1],counter);
               }
               else if(counter==halfway+1)
               {
                  terrain = new BufferedImage[Integer.parseInt(temp)];
                  tempImages = new Image[Integer.parseInt(temp)];
               }
               else if(counter>halfway+1)
               {
                  tempImages[counter-halfway-2] = Toolkit.getDefaultToolkit().getImage(IMAGE_PATH+temp).getScaledInstance(133,100,Image.SCALE_DEFAULT);
                  watch.addImage(tempImages[counter-halfway-2],counter);
               }
               counter++;
            }
            in.close();
            watch.waitForAll();
         }
             catch(Exception e) {e.printStackTrace();}
      
         for(int t=0; t< terrain.length; t++)
         {
            terrain[t] = new BufferedImage(tempImages[t].getWidth(this),tempImages[t].getHeight(this),BufferedImage.TYPE_INT_ARGB);
            Graphics gr = terrain[t].getGraphics();
            gr.drawImage(tempImages[t],0,0,null);
            for(int i = 0; i < terrain[t].getHeight(); i++)
            {
               for(int j = 0; j < terrain[t].getWidth(); j++)
               {
                  if(terrain[t].getRGB(j, i) == Color.magenta.getRGB())
                  {
                     terrain[t].setRGB(j, i, 0x8F1C1C);
                  }
               }
            }
         }
      }
   
       private void loadPreferences()
      {
         String temp = new String("");
         int counter = 0;
         try
         {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("Settings/preferences.ini")));
            while((temp=in.readLine())!= null )
            {
               if(counter==0)
               {
                  playerName= new String(temp);
               }
               counter++;
            }
            in.close();
         }
             catch(Exception e) {e.printStackTrace();}
      }
   
       private void savePreferences()
      {
         try
         {
            String str = playerName;
            File f=new File("Settings/preferences.ini");
            FileOutputStream fop=new FileOutputStream(f);
         
            if(f.exists()){
               fop.write(str.getBytes());
               fop.flush();
               fop.close();
            }
         }
             catch(Exception e){}
      }
   
       public void keyPressed(KeyEvent e)
      {
         if(phase==0)
         {
            switch(e.getKeyCode())
            {
               case KeyEvent.VK_ESCAPE:
                  back.play();
                  shutDown();
                  break;
               case KeyEvent.VK_UP:
                  moveCurser.play();
                  menuSelected--;
                  if(menuSelected == -1)
                     menuSelected =2;
                  break;
               case KeyEvent.VK_DOWN:
                  moveCurser.play();
                  menuSelected++;
                  if(menuSelected == 3)
                     menuSelected =0;
                  break;
               case KeyEvent.VK_SPACE:
               	case KeyEvent.VK_ENTER:
                  selectSetting.play();
                  if(menuSelected==0)
                  {
                     menuSelected=0;
                     phase=2;
                  }
                  else if(menuSelected==1)
                  {
                     String temp = findServer();
                     if(temp.equals(""))
                     {
                        client = new Client();
                        client.start();
                        phase=4;
                     }
                     else
                     {
                        error=temp;
                     }
                  }
                  else if(menuSelected==2)
                     phase=1;
                  break;
            
            }
         }
         else if(phase ==2)
         {
            switch(e.getKeyCode())
            {
               case KeyEvent.VK_ESCAPE:
                  {
                     back.play();
                     menuSelected=0;
                     phase=0;
                  }
                  break;
               case KeyEvent.VK_LEFT:
                  selectSetting.play();
                  if(menuSelected==0)
                  {
                     currentBackground--;
                     if(currentBackground==-1)
                        currentBackground=background.length-1;
                  }
                  else if(menuSelected==1)
                  {
                     currentTerrain--;
                     if(currentTerrain==-1)
                        currentTerrain = terrain.length-1;
                  }
                  break;
               case KeyEvent.VK_RIGHT:
               	case KeyEvent.VK_ENTER:
               	case KeyEvent.VK_SPACE:
                  selectSetting.play();
                  if(menuSelected==0)
                  {
                     currentBackground++;
                     if(currentBackground==background.length)
                        currentBackground=0;
                  }
                  else if(menuSelected==1)
                  {
                     currentTerrain++;
                     if(currentTerrain==terrain.length)
                        currentTerrain = 0;
                  }
                  else if(menuSelected==2)
                  {
                     String temp = startServer(playerName);
                     if(temp.equals(""))
                     {
                        client = new Client();
                        client.start();
                        phase=3;
                     }
                     else
                     {
                        error=temp;
                     }
                  }
                  break;
               case KeyEvent.VK_UP:
                  moveCurser.play();
                  menuSelected--;
                  if(menuSelected == -1)
                     menuSelected =2;
                  break;
               case KeyEvent.VK_DOWN:
                  moveCurser.play();
                  menuSelected++;
                  if(menuSelected == 3)
                     menuSelected =0;
                  break;
            }
         }
         else if(phase ==1)
         {
            switch(e.getKeyCode())
            {
               case KeyEvent.VK_ESCAPE:
                  back.play();
                  {
                     loadPreferences();
                     phase=0;
                  }
                  break;
               case KeyEvent.VK_ENTER:
                  selectSetting.play();
                  if(!playerName.equals(""))
                  {
                     savePreferences();
                     phase=0;
                  }
                  break;
               case KeyEvent.VK_BACK_SPACE:
                  if(playerName.length()>0)
                     playerName= playerName.substring(0,playerName.length()-1);
                  break;
               default:
                  if(Character.isLetterOrDigit(e.getKeyChar()))
                     playerName+=e.getKeyChar();
                  break;
            
            }
         }
         else if(phase == 3)
         {
            switch(e.getKeyCode())
            {
               case KeyEvent.VK_ESCAPE:
                  back.play();
                  //closeServer();
                  client.close();
                  client.stop();
                  shutDown();  
               	//menuSelected=0;
                  //phase=0;
                  //System.exit(0);
                  break;
               case KeyEvent.VK_ENTER:
               case KeyEvent.VK_SPACE:
                  selectSetting.play();
                  startGame();
                  break;
            }
         }
         else if(phase == 4)
         {
            switch(e.getKeyCode())
            {
               case KeyEvent.VK_ESCAPE:
                  back.play();
                  client.send(IP.PLAYER_DISCONNECT+"@");
                  client.close();
                  client.stop();
                  menuSelected=0;
                  System.exit(0);
                  break;
               case KeyEvent.VK_ENTER:
               case KeyEvent.VK_SPACE:
                  startGame();
                  break;
            }
         }
         else if(phase ==5)
         {
            if(localPlayer == level.getCurrentPlayer()) //Is it my turn?
            {
               switch(e.getKeyCode())
               {
                  case KeyEvent.VK_ESCAPE:
                     //if (level.gameOver())
                       // shutDown();
                     error = "If you quit now you forfiet!  [ESC] again to quit, or any other key to continue!";
                     oldPhase=6;
                     phase=6;
                     break;
                  case KeyEvent.VK_LEFT:
                     if (!fire && !disableKeys)
                        left = true;
                     break;
                  case KeyEvent.VK_RIGHT:
                     if (!fire && !disableKeys)
                        right = true;
                     break;
                  case KeyEvent.VK_UP:
                     if (!fire && !disableKeys)
                        up = true;
                     break;
                  case KeyEvent.VK_DOWN:
                     if (!fire && !disableKeys)
                        down = true;
                     break;
                  case KeyEvent.VK_SPACE:
                     if (!fire && !disableKeys)
                     {
                        fire = true;
                        powerUp = true;
                        power = 1.0;
                        inc = 0.0;
                     }
                     break;
                  case KeyEvent.VK_1:
                     if (!fire)
                        localPlayer.setWeapon(0);
                     break;
                  case KeyEvent.VK_2:
                     if (!fire && localPlayer.getAmmo(1) > 0)
                        localPlayer.setWeapon(1);
                     break;
                  case KeyEvent.VK_3:
                     if (!fire && localPlayer.getAmmo(2) > 0)
                        localPlayer.setWeapon(2);
                     break;
                  case KeyEvent.VK_4:
                     if (!fire && localPlayer.getAmmo(3) > 0)
                        localPlayer.setWeapon(3);
                     break;
                  case KeyEvent.VK_5:
                     if (!fire && localPlayer.getAmmo(4) > 0)
                        localPlayer.setWeapon(4);
                     break;
                  case KeyEvent.VK_6:
                     if(!fire && localPlayer.getAmmo(5) > 0)
                        localPlayer.setWeapon(5);
                     break;
               //default:
                 //level.makeRandomProjectileHit();
                 //break;
               }
            }
            else
            {
               if(server!=null)
                  if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                  {
                     shutDown();
                  }              
            }
         }
         else if(phase==6)
         {
            if(e.getKeyCode()== KeyEvent.VK_ESCAPE)
            {
               client.send(IP.PLAYER_DISCONNECT+"@ ");
               shutDown();
            }
            else
               phase=5;
         }
         else if(phase==7)
         {
            if(e.getKeyCode()== KeyEvent.VK_ESCAPE)
            {
               client.close();
               if(server!=null)
                  //server.close();
                  shutDown();
               	//phase=0;
            }
         }
      
      }
   
       public void keyReleased(KeyEvent e)
      {
         if(phase==5)
         {
            switch(e.getKeyCode())
            {
               case KeyEvent.VK_LEFT:
                  left = false;
                  break;
               case KeyEvent.VK_RIGHT:
                  right = false;
                  break;
               case KeyEvent.VK_UP:
                  up = false;
                  break;
               case KeyEvent.VK_DOWN:
                  down = false;
                  break;
            }
         
            if (fire && e.getKeyCode() == KeyEvent.VK_SPACE)
            {
               String tempfire = new String();
               tempfire= level.getCurrentPlayerIndex()+"@"+level.getCurrentPlayer().getAngle()+"@"+power+"@"+level.getCurrentPlayer().getCurrentWeaponIndex()+"@"+level.getCurrentPlayer().getDirection();
               client.send(IP.PLAYER_SHOOT+"@"+tempfire);
               //level.firePlayer((int)power);
               fire = false;
               wait(2000);
               power = 0;
            }
         
            // used to keep monkeys on correct frame
            level.movePlayer(0);
         }
      }
   
       public void keyTyped(KeyEvent e){}
       public void mouseClicked(MouseEvent e){}
       public void mouseEntered(MouseEvent e){}
       public void mouseExited(MouseEvent e){}
       public void mousePressed(MouseEvent e){}
       public void mouseReleased(MouseEvent e){}
   
       public void run()
      {
      
         phase =0;
         while(true)
         {
            if(oldPhase!=phase)
            {
               error = "";
               oldPhase=phase;
            }
            if(phase == 3)
            {
               if(lobbyPlayers.size() != server.getAttached().length)
               {
                  String tempMessage = new String();
                  for(int t=0;t<server.getAttached().length; t++)
                  {
                     tempMessage+=server.getAttached()[t]+",";
                  }
                  tempMessage = tempMessage.substring(0,tempMessage.length()-1);
                  server.sendToAll(IP.SEND_LOBBY_PLAYERS+"@"+tempMessage);
               }
            }
            else if(phase==5)
            {
               if(level.getCurrentPlayer()==localPlayer)
               {
               }
               movement();
               if (level.turnOver())
                  wait(2000);
              
            }
            draw();
            try
            {
               Thread.currentThread().sleep(1);
            }
                catch(Exception e){}
         }
      }
   
       private void draw()
      {
      
         if(phase==0)
         {
            g.setFont(healthFont);
            g.drawImage(title,0,0,this);
            g.setColor(Color.yellow);
            g.drawString("Player Name: ",menuX-10,600);
            g.setColor(Color.white);
            g.drawString(playerName,menuX+95,600);
            g.setColor(Color.green);
            g.fillRect(menuX-15,menuY-20+(50*menuSelected),170,40);
            g.setColor(Color.white);
            g.drawString(HOST,menuX,menuY);
            g.drawString(JOIN,menuX,menuY+50);
            g.drawString(PLAYER_NAME_CHANGE,menuX,menuY+100);
         }
         else if(phase==1)
         {
            g.drawImage(title,0,0,this);
            g.setColor(Color.green);
            g.fillRect(menuX-15,600-15,105,20);
            g.setColor(Color.yellow);
            g.drawString("Player Name: ",menuX-10,600);
            g.setColor(Color.white);
            if((System.currentTimeMillis()/500)%2==0)
               g.drawString(playerName+"[]",menuX+95,600);
            else
               g.drawString(playerName,menuX+95,600);
            g.setColor(Color.white);
            g.drawString(HOST,menuX,menuY);
            g.drawString(JOIN,menuX,menuY+50);
            g.drawString(PLAYER_NAME_CHANGE,menuX,menuY+100);
         }
         else if(phase==2)
         {
            g.drawImage(title,0,0,this);
            g.setColor(Color.green);
            if(menuSelected==2)
               g.fillRect(menuX-15,menuY-20+250,170,40);
            else
               g.fillRect(menuX-15,menuY-20+(50*menuSelected),170,40);
            g.setColor(Color.yellow);
            g.drawString(CHANGE_LEVEL,menuX,menuY);
            g.drawString(CHANGE_TERRAIN,menuX,menuY+50);
            g.drawImage(background[currentBackground],menuX,menuY+100,this);
            g.drawImage(terrain[currentTerrain],menuX,menuY+100,this);
            g.drawString(LAUNCH_SERVER,menuX,menuY+250);
         
         }
         else if(phase==3)
         {
            g.drawImage(title,0,0,this);
            g.setColor(Color.yellow);
            g.drawString("Waiting for players...",menuX-15,menuY);
            g.drawString("Press ENTER to start game.",menuX-15,menuY+20);
            g.drawString("Open slots will be filled with computer players.", menuX-80,menuY+40);
            g.setColor(Color.red);
            g.drawString("SLOT 1: ",menuX-60, menuY+80);
            g.setColor(Color.green);
            g.drawString("SLOT 2: ",menuX-60, menuY+80+20);
            g.setColor(Color.yellow);
            g.drawString("SLOT 3: ",menuX-60, menuY+80+40);
            g.setColor(Color.pink);
            g.drawString("SLOT 4: ",menuX-60, menuY+80+60);
            //for(int t=0; t<server.getAttached().length;t++)
               //g.drawString(server.getAttached()[t],menuX,menuY+80+(t*20));
            for(int t=0; t<lobbyPlayers.size();t++)
               g.drawString(lobbyPlayers.get(t),menuX,menuY+80+(t*20));
         }
         else if(phase==4)
         {
            g.drawImage(title,0,0,this);
            g.setColor(Color.yellow);
            g.drawString("Waiting for players...",menuX-15,menuY);
            g.drawString("Host will start game when ready.",menuX-15,menuY+20);
            //g.drawString("If there are open slots they will be filled by computer players.", menuX-80,menuY+40);
            g.setColor(Color.red);
            g.drawString("SLOT 1: ",menuX-60, menuY+80);
            g.setColor(Color.green);
            g.drawString("SLOT 2: ",menuX-60, menuY+80+20);
            g.setColor(Color.yellow);
            g.drawString("SLOT 3: ",menuX-60, menuY+80+40);
            g.setColor(Color.pink);
            g.drawString("SLOT 4: ",menuX-60, menuY+80+60);
            for(int t=0; t<lobbyPlayers.size();t++)
               g.drawString(lobbyPlayers.get(t),menuX,menuY+80+(t*20));
         }
         else if(phase==5 || phase==6)
         {
            g.setFont(healthFont);
            int tempDx=0;
            if(level.getCurrentPlayer().isAI())
            {
               tempDx = level.getCurrentPlayer().getMovement();
               if (tempDx != 0)
                  client.send(IP.PLAYER_MOVE+"@"+level.getCurrentPlayerIndex()+"@"+tempDx);
               else if (level.getCurrentPlayer().getAngle() != prevAngle && ((int)level.getCurrentPlayer().getPower()) != prevPower)
               {
                  level.getCurrentPlayer().findVelAng();
                  prevAngle = level.getCurrentPlayer().getAngle();
                  prevPower = (int)level.getCurrentPlayer().getPower();
               }
               
               //resets frame
               else if (tempDx == 0)
                  client.send(IP.PLAYER_MOVE+"@"+level.getCurrentPlayerIndex()+"@0");
            	//System.out.print(prevAngle+","+prevPower);
            
               if(!level.getCurrentPlayer().isFiring() && level.getCurrentPlayer().isReadyToFire())
               {
                  String tempfire = new String();
                  tempfire= level.getCurrentPlayerIndex()+"@"+level.getCurrentPlayer().getAngle()+"@"+level.getCurrentPlayer().getPower()+"@"+level.getCurrentPlayer().getCurrentWeaponIndex()+"@"+level.getCurrentPlayer().getDirection();
                  client.send(IP.PLAYER_SHOOT+"@"+tempfire);
               
                  //level.getCurrentPlayer().fire(0);
               }
            }
            if(level.playerTurnTime())
               client.send(IP.TURN_FINISHED+"@ ");
            if(level.gameOver())
            {
               if(level.getCurrentPlayer()==localPlayer)
                  level.playVictory();
               else
                  level.playDefeat();
            
               phase = 7;
            }        
            level.draw(g);
         
            drawGUI();
         }
         else if(phase==7)
         {
            level.endGame(g);
         }
      
         if(error.length()>0)
         {
            g.setColor(Color.red);
            g.setFont(healthFont);
            g.drawString(error,menuX-error.length()*2,menuY-50);
         }
         if(!fullscreen)
         {
            getGraphics().drawImage(backbuffer,0, 0, null); //line for double buffering - ALL DRAW CODE MUST COME BEFORE THIS
         }
         else
         {
            bufferStrategy.show(); //for fullscreen mode
         }
      
      }
   
       private void movePlayer(int index, int amt)
      {
         //level.movePlayer(index, amt);
         level.movePlayer(amt);
      }
   
       private void playerShoot(int angle, double power, int weaponIndex, int d)
      {
         level.getCurrentPlayer().setAngle(angle);
         level.getCurrentPlayer().setCurrentWeapon(weaponIndex);
         level.getCurrentPlayer().setDirection(d);
         level.firePlayer((int)power);
      }
      
       private void changePlayer()
      {
         level.nextPlayer();
         //if (level.turnOver())
         wait(2000);
      }
   
       private void movement()
      {
         if (left)
            client.send(IP.PLAYER_MOVE+"@"+localPlayer.getIndex()+"@-1");
            //level.movePlayer(-1);
         if (right)
            client.send(IP.PLAYER_MOVE+"@"+localPlayer.getIndex()+"@1");
            //level.movePlayer(1);
      	
      	//resets frame
         if (!left && !right)
            client.send(IP.PLAYER_MOVE+"@"+level.getCurrentPlayerIndex()+"@0");
      	
         if (up)
         {
            count++;
            if (count >= 3)
            {
               level.changeAngle(1);
               count -= 3;
            }
         }
         if (down)
         {
            count--;
            if (count <= 0)
            {
               level.changeAngle(-1);
               count += 3;
            }
         }
      
         if (fire)
            fire();
      		
         if(level.isReadyForNextPlayer())
         {
         
            client.send(IP.TURN_FINISHED+"@ ");
            level.setReadyForNextPlayer(false);
         }
      }
   
       private void fire()
      {
         if (powerUp)
         {
            g.setColor(localPlayer.getColor());
            if(System.nanoTime() - prevTime > 1000)
            {
               prevTime = System.nanoTime();
               inc += .0888;
               power += inc;
               if (power > 100.0)
               {
                  power = 100.0;
                  powerUp = false;
               }
            }
         }
      }
   
       private void drawGUI()
      {
      	//draw gui backdrop
         g.drawImage(GUIback, 0, HEIGHT - 86, null);
      
      	//draw weps
         for (int i=0;i<6;i++)
            g.drawImage(wep[i], i*85+263, HEIGHT - 86, null);
         g.drawImage(selwep[localPlayer.getWeapon()], localPlayer.getWeapon()*85+263, HEIGHT - 86, null);
      
      	//draw gui
         g.drawImage(GUI, 0, HEIGHT - 86, null);
      
      	//draw ammo
         g.setColor(Color.white);
         g.setFont(infFont);
         g.drawString((new DecimalFormatSymbols().getInfinity()), 263, HEIGHT - 73);
         g.setFont(ammoFont);
         for(int i=1;i<6;i++)
            g.drawString(""+localPlayer.getAmmo(i), i*85+263, HEIGHT - 71);
      
      	//get angle
         String angle = localPlayer.getAngle()+"\u00b0";
         if (angle.length() == 2)
            angle = "0" + angle;
      
      	// draw angle, power bar, doodie bar, & gui cover
         g.setFont(angleFont);
         g.setColor(localPlayer.getColor());
         g.drawString(angle, 10, HEIGHT - 54);
         g.fillRect(80, HEIGHT - 84, (int) (1.69*power), 35);
         g.setColor(Brown);
         g.fillRect(8, HEIGHT - 42, (int) (2.40*localPlayer.getDoodie()), 35);
         g.drawImage(GUIcover, 0, HEIGHT - 86, null);
         g.setColor(Color.black);
      }
   
       public void wait(int time)
      {
         disableKeys = true;
         try
         {
            Thread.currentThread().sleep(time);
         }
             catch(InterruptedException e){}
         disableKeys = false;
      }
   
       private void shutDown()
      {
         closeServer();
         this.setVisible(false);
         device.setFullScreenWindow(null);
         System.exit(0);
      }
   
       public static void main(String[] args) throws Exception
      {
         boolean fullscr = false;
         if(args.length > 0)
            fullscr = true;
         try
         {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            Random rand = new Random();
            new Engine(fullscr,device,gc);
            //new Engine(false,players,0,InetAddress.getLocalHost(),false,device,gc,rand.nextInt(4));
         }
             catch (Exception e)
            {
               e.printStackTrace();
            }
      }
   
   
   //******************************************************************************************************
       class Client extends Thread
      {
      
         private Socket clientSocket;
         private PrintStream serverOut;
         private BufferedReader serverIn;
      
      
          public Client()
         {
            initNet();
         }
      
          public void run()
         {
            String tempInput = new String();
            while(true)
            {
               send(IP.PLAYER_CONNECT+"@"+playerName);
               try
               {
                  if((tempInput=serverIn.readLine())!=null)
                  {
                     switch(Byte.parseByte(tempInput.split("@")[0]))
                     {
                        case IP.SEND_LOBBY_PLAYERS:
                           refreshLobbyPlayers(tempInput.split("@")[1].split(","));
                           break;
                        case IP.SEND_PLAYER_SHOOT:
                           //System.out.println("A SHOOT");
                           playerShoot(Integer.parseInt(tempInput.split("@")[2]), Double.parseDouble(tempInput.split("@")[3]),Integer.parseInt(tempInput.split("@")[4]),Integer.parseInt(tempInput.split("@")[4]));
                           break;
                        case IP.SEND_PLAYER_MOVE:
                           movePlayer(Integer.parseInt(tempInput.split("@")[1]),Integer.parseInt(tempInput.split("@")[2]));
                           break;
                        case IP.SEND_GAME_SYNC:
                           createLevelAndPlayers(tempInput);
                           break;
                        case IP.SEND_START_ROUND:
                           phase=5;
                           break;
                        case IP.SEND_NEXT_TURN:
                           //System.out.println("next turn!");
                           changePlayer();
                           break;
                        case IP.SEND_PLAYER_KILL:
                           if(level!=null)                  
                              level.getCurrentPlayer().setHealth(0);
                           break;
                     }
                  }
               }
                   catch(SocketException er)
                  {
                     error = "Server Disconnected!";
                     oldPhase=0;
                     phase = 0;
                     client.close();
                     this.stop();
                  }
                   catch(Exception e){e.printStackTrace();}
            }
         
         }
      
          public void send(String message)
         {
            serverOut.println(message);
         }
      
          public void close()
         {
            try
            {
               serverOut.close();
               serverIn.close();
               clientSocket.close();
            }
                catch(Exception e){}
         }
      
          private void initNet()
         {
            try
            {
               clientSocket = new Socket(IP.getServer(), IP.SERVER_PORT);
               serverOut = new PrintStream(clientSocket.getOutputStream());
               serverIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }
                catch(Exception e){
                  System.exit(0);
               }
         }
      
      }
   
   }