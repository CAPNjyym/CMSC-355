//Call of Doodie Level

   import java.awt.*;
   import java.awt.image.*;
   import java.applet.AudioClip;

    public class Level
   {
      private Image background;
      private BufferedImage terrain;
      private Graphics gr; 				//used to draw on BufferedImage terrain
      private Component parent;
   
      private Player[] players, playersAlive;
      private int currentPlayer;
      private int playersLeft;
      private boolean turnOver;
      private boolean readyForNextPlayer;
      private boolean switchPlayer;
      private long startTime;
      private int oldClock;
   	
      private Projectile projectile;
      private Projectile nullProjectile;
      private Image[] finalScreen;
      private AudioClip clock;
      private AudioClip bgMusic;
      private AudioClip warcry;
      private AudioClip win;
      private AudioClip lose;
   
       public Level(String name, int ter, int cur, Player[] players, Component parent)
      {
         this.players = this.playersAlive = players;
         this.parent = parent;
         this.currentPlayer = cur;
         String temp=new String();
         startTime=0;
         if(ter<10)
            temp="0"+ter;
         else
            temp=ter+"";
         String[] tempString = {name,temp};
         initLandscape(tempString);
      
         initPlayers(initWeapons());
      }
   
       private void initLandscape(String[] temp)
      {
         LevelElement dummy = new LevelElement("dummy",0,0,parent);
         try{
            bgMusic = dummy.getAudio("tribal drums");
            warcry = dummy.getAudio("WarCry");
            win = dummy.getAudio("Ta Da");
            lose =dummy.getAudio("You Lose");
            clock =dummy.getAudio("ticktock");
         }
             catch(Exception e){System.out.println("Exception loading bgMusic");}
         warcry.play();
         bgMusic.loop();
         for(int t=0; t<temp.length; t++)
            System.out.println(temp[t]);
         Image[] tempImages = dummy.getImages(temp);
         background = tempImages[0];
         String [] finalTemp = {"victory"};
         finalScreen = dummy.getImages(finalTemp);
         terrain = new BufferedImage(tempImages[1].getWidth(null),tempImages[1].getHeight(null),BufferedImage.TYPE_INT_ARGB);
         gr = terrain.getGraphics();
         gr.drawImage(tempImages[1],0,0,null);
         for(int i = 0; i < terrain.getHeight(); i++)
         {
            for(int j = 0; j < terrain.getWidth(); j++)
            {
               if(terrain.getRGB(j, i) == Color.magenta.getRGB())
               {
                  terrain.setRGB(j, i, 0x8F1C1C);
               }
            }
         }
      }
   
       private void initPlayers(Weapon[] weapons)
      {
         playersLeft = players.length;
         for(int t=0; t<playersLeft; t++)
         {
            players[t].preGameConfig(parent,weapons,players);
         //	players[t].X(t*100 + 200+ (int)(Math.random()*200));
         	
            players[t].move(0, terrain);
            for(int u=0; u<t; u++)
               if (players[t].getCollisionBox().intersects(players[u].getCollisionBox()))
               {
                  System.out.println("-----"+players[t].getX()+"------"+players[t].getY());
                  System.out.println("Player "+(t+1)+" intersected Player "+(u+1)+".  Resetting Player "+(t+1));
                  //t--;
                  break;
               }
         }
      ///////////////////////////////////////////////////////////////
         players[currentPlayer].playerTurn(terrain);
      ///////////////////////////////////////////////////////////////
      }
   
       public void movePlayer(int index, int dx)
      {
         players[index].move(dx,terrain);
      }
   
       public void movePlayer(int dx)
      {
         players[currentPlayer].move(dx, terrain);
      }
   
       public void changeAngle(int da)
      {
         players[currentPlayer].changeAngle(da);
      }
   
       public void firePlayer(int pow)
      {
         players[currentPlayer].getCurrentWeapon().playFireSound();
         players[currentPlayer].fire(pow);
      }
   
       private Weapon[] initWeapons()
      {
         Weapon[] weapons = new Weapon[6];
         weapons[0] = new Weapon("Banana",parent);
         weapons[1] = new Weapon("Tomato",parent);
         weapons[2] = new Weapon("Plantain",parent);
         weapons[3] = new Weapon("BCannon",parent);
         weapons[4] = new Weapon("DeFlea",parent);
         weapons[5] = new Weapon("COD",parent);
         return weapons;
      }
   
       public void update()
      {
      
      }
   
       public void draw(Graphics2D g)
      {
      
      
         if (turnOver)
         {
            turnOver = false;
            for(int i=0;i<playersLeft;i++)
               players[i].move(0,terrain);
         }
      
         g.drawImage(background,0,0,null);
         g.drawImage(terrain,0,0,null);
      
         for(int t=players.length-1; t>=0; t--)
            if (currentPlayer != t)
               players[t].draw(g, 0);
         players[currentPlayer].draw(g, 1);
      
         if(players[currentPlayer].isFiring())
         {
            startTime = -1;
            projectile = players[currentPlayer].getProjectile();
            int collision = projectile.move(playersAlive, terrain);
         
            if(!projectile.getName().equals("De-Flea"))
               projectile.draw(g);
            else
            {
               collision = players[currentPlayer].getIndex() +1;
               players[currentPlayer].setHealth(players[currentPlayer].getHealth()+ 2*projectile.damage());
               players[currentPlayer].setDoodie(players[currentPlayer].getDoodie()+projectile.damage());
            }
         
            if(collision != 0)
            {
               turnOver = true;
               players[currentPlayer].getCurrentWeapon().stopFireSound();
               players[currentPlayer].getCurrentWeapon().playHitSound();
               projectileHitsTerrain(projectile);
               //System.out.println(projectile.getX()+","+projectile.getY());
            
            	// inflict damage on player
               if (collision < 5)
               {
                  players[collision-1].setHealth(players[collision-1].getHealth()-projectile.damage());
               
                  if(players[collision-1].getHealth() >= 100)
                     players[collision-1].setHealth(100);
               
                  if(!projectile.getName().equals("Call Of Doodie"));
                  players[collision-1].setDoodie(players[collision-1].getDoodie()+ 2 *projectile.damage());
               }
            
               players[currentPlayer].setCollision(collision);
               players[currentPlayer].setLandX(projectile.getHitX());
               projectile = nullProjectile;
               players[currentPlayer].updateAIBounds();
               players[currentPlayer].setIsFiring(false);
            
            	// if a player is "dead"
            	// move player to back of player array
            	// decrement playersLeft by 1
               for(int i=0;i<playersLeft;i++)
                  if (players[i].getHealth() <= 0)
                  {
                     playersLeft--;
                  
                     players[i].setHealth(0);
                     Player placeholder = players[i];
                     for(int j=i;j<playersLeft;j++)
                        players[j] = players[j+1];
                     players[playersLeft] = placeholder;
                  
                     if (playersLeft == 1)
                        break;
                  
                  	// updates players alive list
                     playersAlive = new Player[playersLeft];
                     for(int j=0;j<playersLeft;j++)
                        playersAlive[j] = players[j];
                     for(int j=0;j<4;j++)
                        players[j].updatePlayerList(playersAlive, j);
                  
                     i=0;
                  }
            
            	//change players!!!
               readyForNextPlayer=true;
               startTime = -1;
            
            	///////////////////////////////////////////
            	//players[currentPlayer].playerTurn();
            	/////////////////////////////////////////
            
               g.drawImage(background,0,0,null);
               g.drawImage(terrain,0,0,null);
               for(int i=0;i<playersLeft;i++)
                  if (collision-1 != i)
                     players[i].move(0,terrain);
               for(int t=players.length-1; t>=0; t--)
                  players[t].draw(g, 0);
             	
            }
         }
      
      	// displays player turn
         g.setFont(new Font("Serif", Font.BOLD, 20));
         g.setColor(Color.black);
         g.drawString("Turn:", 5, 677);
         g.setColor(players[currentPlayer].getColor());
         g.drawString((players[currentPlayer].getName()), 55, 677);
      
         g.setColor(Color.black);
         g.fillOval(940, 15, 60,40);
         g.setColor(Color.green);
         int clockCur = (15 - (int)((System.currentTimeMillis() - startTime))/1000);
         if (clockCur < 16 && clockCur > 0)
         {
            String drawStr = ":";
            if (clockCur < 10)
            {
               drawStr = drawStr + "0";
            }
            drawStr += clockCur;
            if(clockCur < 6)
            {
               if(oldClock != clockCur)
               {
                  clock.play();
                  oldClock = clockCur;
               }
               g.setColor(Color.red);
            }
            g.drawString(drawStr, 955, 42);
         }
         else
         {
            g.drawString(" --", 955, 42);
         }
      }
   
       public void endGame(Graphics2D g)
      {
         g.setColor(Color.white);
         g.drawImage(finalScreen[0],0,0,null);
         g.setFont(new Font("SansSerif", Font.BOLD, 30));
         g.drawString("Game Results:", 50, 50);
         for(int i=1;i<=players.length;i++)
         {
            g.drawString(i+".", 50, (i+1)*50);
            g.setColor(players[i-1].getColor());
            g.drawString(players[i-1].getName(), 125, (i+1)*50);
            g.setColor(Color.white);
         }
         g.setFont(new Font("Serif", Font.BOLD, 30));
         g.drawString("'ESC' to return to lobby", 50, 450);
      }
   
       public boolean turnOver()
      {
         return turnOver;
      }
   
       public void playVictory()
      {
         win.play();
      }
   
       public void playDefeat()
      {
         lose.play();
      }
   
       public boolean gameOver()
      {
         if(playersLeft <= 1)
         {      
            bgMusic.stop();
            return true;
         }
         return false;
      }
   
       public void projectileHitsTerrain(Projectile p)
      {
         gr.setColor(Color.magenta);
         gr.fillOval((int)(p.getHitX()+(p.width()-p.radius())/2.0),(int)(p.getHitY()+(p.width()-p.radius())/2.0),p.radius(),p.radius());
         gr.setColor(Color.green);
         for(int i = (int)(p.getHitY()+(p.width()-p.radius())/2.0); i < p.getY()+(p.width()+p.radius())/2.0; i++)
         {
            for(int j = (int)(p.getHitX()+(p.width()-p.radius())/2.0); j < p.getX()+(p.width()+p.radius())/2.0; j++)
            {
               if(j>=0 && j<Engine.WIDTH && i>=0 && i<Engine.HEIGHT && terrain.getRGB(j, i) == Color.magenta.getRGB())
                  terrain.setRGB(j, i, 0x8F1C1C);
            }
         }
      
      	// fills ground in under GUI
         for (int i=0;i<1024;i++)
            for(int j=Engine.HEIGHT-86;j<768;j++)
               terrain.setRGB(i, j, Color.black.getRGB());
      }
   
      /*public void makeRandomProjectileHit()
      {
         java.util.Random rand = new java.util.Random();
         Image[] temp = new Image[2];
         Projectile p = new Projectile("test",150+rand.nextInt(850), rand.nextInt(300)+300,5,0,0,0,rand.nextInt(150),temp,null);
         projectileHitsTerrain(p);
      }*/
   
       public int getCurrentPlayerIndex()
      {
         return currentPlayer;
      }
   
       public Player getCurrentPlayer()
      {
         return players[currentPlayer];
      }
   
       public Player[] getPlayers()
      {
         return players;
      }
   
       public void setProjectile(Projectile p)
      {
         projectile = p;
      }
   
       public boolean isReadyForNextPlayer()
      {
         return readyForNextPlayer;
      }
   
       public void setReadyForNextPlayer(boolean b)
      {
         readyForNextPlayer = b;
      }
   
       public void nextPlayer()
      {
         //readyForNextPlayer=false;
         currentPlayer++;
         if(currentPlayer >= playersLeft)
         {
            currentPlayer=0;
         }
         players[currentPlayer].playerTurn(terrain);
      }
   
       public boolean playerTurnTime()
      {
         if (startTime == -1)
         {
            startTime = System.currentTimeMillis();
         
         }
         if ((System.currentTimeMillis() - startTime) > 15000)
         {
            startTime = -1;
            //nextPlayer();
            return true;
         }
         return false;
      }
   }
