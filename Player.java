//Player


   import java.awt.*;
   import java.awt.event.*;
   import java.util.ArrayList;
   import java.awt.geom.*;
   import javax.sound.sampled.*;
   import java.io.*;
   import java.util.Random;
   import java.net.InetAddress;
   import java.awt.image.*;
   import java.awt.image.BufferedImage;

    public class Player extends LevelElement
   {
      protected int health;
      protected int doodieMeter;
      private int counter = 2;  //Used in the looping of animations to keep track of what frame is next.
      private int incDec = 1;   //Same as above.
      private int monkeyFace = 1; //Determines which direction the monkey is facing. 1 = left. 2 = right.
      private int movX = 0;
      protected int currentWeapon;
      private float anim;
      private int index;
      private long timer;
      private boolean moving;
      private int ground;
      private Font healthFont;
      private Image[] walkSprites;
      private Image[] aimSprites;
      private Image curSprite;
      private Clip[] hitSounds, dieSounds;
      private Color playerColor;
      private Weapon[] weaponSet;
      private int[] ammo;
      private Projectile weapon;
      private Projectile nullWeapon;
      private boolean readyToFire;
      private boolean firing;
      private int AI;   //0=human, 1-5 = skill level
      private Player target;			//if AI; target to shoot at
      private Player[] targetList;	//if AI; target list to choose from
      private double power;			//if AI; random power to fire with
      private int angle;				//if AI; angle needed to hit target
      private int high, low; 			//if AI; power range to hit target
      private int landx;				//if AI; x value where projectile hit
      private int collision;			//if AI; type of collision
      private int movement;			//if AI; ammount to move
      private int previousHealth;	//if AI; ammount of health turn before
      private int moveDirection = 1;		//if AI; 1 for move left, 2 for move right
      private int allyIndex;			//if AI; holds the index of ally player
		private int moveCounter;		//if AI; after 5 turns, move a random number of steps
      private InetAddress IP;
   
      private GraphicsConfiguration gc; // Needed for arm rotation.
   
      /*Loads all necessary images for movement.
      private final Image MONKEYSTILL_L = Toolkit.getDefaultToolkit().getImage("monkeystill_L.gif");
      private final Image MONKEY1_L = Toolkit.getDefaultToolkit().getImage("monkeymove1_L.gif");
      private final Image MONKEY2_L = Toolkit.getDefaultToolkit().getImage("monkeymove2_L.gif");
      private final Image MONKEY3_L = Toolkit.getDefaultToolkit().getImage("monkeymove3_L.gif");
      private final Image MONKEYSTILL_R = Toolkit.getDefaultToolkit().getImage("monkeystill_R.gif");
      private final Image MONKEY1_R = Toolkit.getDefaultToolkit().getImage("monkeymove1_R.gif");
      private final Image MONKEY2_R = Toolkit.getDefaultToolkit().getImage("monkeymove2_R.gif");
      private final Image MONKEY3_R = Toolkit.getDefaultToolkit().getImage("monkeymove3_R.gif");
   //AI Difficulty level has been factored in!
   	/*
   //	-20x + x^2 + 85
   		AI Level	|	movement %	| change target %
   			1		|		74			|			50
   			2		|		55			|			40
   			3		|		38			|			30
   			4		|		34			|			20
   			5		|		10			|			10
   	*/
       public Player(String name)
      {
         super(name,0,0,null);
         try
         {
            IP = InetAddress.getLocalHost();
         }
             catch(Exception e){e.printStackTrace();}
      }
   
       public Player(String name, InetAddress ip)
      {
         super(name,-1,-1,null);
         this.name = name;
         IP = ip;
      }
   
    	//takes name, playerID (index), color, if AI
       public Player(String name, int index, int col, int AI)
      {
         super(name,-1,-1,null);
      
         this.index = index;
         try
         {
            IP = InetAddress.getLocalHost();
         }
             catch(Exception e){e.printStackTrace();}
         this.AI = AI;
         readyToFire = false;
         firing = false;
         playerColor = getColor(col);
         collisionBox = new Rectangle(x,y,41,48);
      
         //Necessary for rotation.
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gs = ge.getDefaultScreenDevice();
         gc = gs.getDefaultConfiguration();
      }
   
   	// name, playerID, x, y, Color index, and if AI
       public Player(String name, int index, int x, int y, int col, int AI)
      {
         super(name,0,0,null);
      
         this.index = index;
         this.x = x;
         this.y = y;
      
         this.AI = AI;
         readyToFire = false;
         firing = false;
         playerColor = getColor(col);
         collisionBox = new Rectangle(x,y,41,48);
      
         //Necessary for rotation.
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gs = ge.getDefaultScreenDevice();
         gc = gs.getDefaultConfiguration();
      }
   
       public Color getColor(int col)
      {
         if (col == 1)
            return Color.red;
         else if (col == 2)
            return Color.green;
         else if (col == 3)
            return Color.blue;
         else if (col == 4)
            return Color.yellow;
         else if (col == 5)
            return Color.orange;
         else if (col == 6)
            return Color.cyan;
         else if (col == 7)
            return new Color(102,45,145); // purple
         else if (col == 8)
            return Color.pink;
         return Color.black;
      }
   
   	//inits player object for gameplay
   	//ONLY CALL FROM HOST
   	//NEED TO MAKE NEW CONSTRUCTOR THAT ACCEPTS p, set, and (name,x,y,colorInt,index,AI) from send string
       public void preGameConfig(Component p, Weapon[] set, Player[] pset)
      {
      
         parent = p;
         Random rand = new Random();
         if(x==-1 && y ==-1)
         {
            y = 50;
            //x=index*200 + 150;
            //x = rand.nextInt(924)+50;
         }
         previousHealth = health = 100;
         weaponSet = set;
         ammo = new int[weaponSet.length];
         for(int i=0;i<weaponSet.length;i++)
            ammo[i] = set[i].getAmmo();
         currentWeapon=0;
         targetList = pset;
      //DEFAULT TESTING VALUES
         angle = 0;
         power = 1.0;
         allyIndex = -1;
      ///////////////////////
		
			moveCounter = 0;
			
      	// sets walkSprite[0...7]
         String[] tempString = {"monkeystill_L","monkeymove1_L","monkeymove2_L","monkeymove3_L","monkeystill_R","monkeymove1_R","monkeymove2_R","monkeymove3_R","monkeyhit_L","monkeyhit_R","monkeyout_L","monkeyout_R"};
         walkSprites = getImages(tempString);
         curSprite = walkSprites[0];
      
         String[] tempString2 = {"monkeyaim_L","monkeyarmless_L","monkeyaim_R","monkeyarmless_R", "monkeyaimcan_L" , "monkeyaimcan_R"};
         aimSprites = getImages(tempString2);
      
         collisionBox.setRect(x,y,curSprite.getWidth(p)-1,curSprite.getHeight(p)-1);
      }
   
   	//call this method to initialize anything for the start of the turn
   	//this is mostly to help set up AI for the turn but also other stuff
       public void playerTurn(BufferedImage terrain)
      {
         if(AI!=0)  //AI setup
         {
            currentWeapon = (int)(Math.random()*4);
         
            while(ammo[currentWeapon] <= 0 && currentWeapon!=0)
               currentWeapon = (int) (Math.random()*4);
         
         	//System.out.println(currentWeapon);
            if(ammo[4]!=0 && AI==1 && Math.random()<0.1)
               currentWeapon = 4;
            else if(ammo[4]!=0 && health<=(AI*10+10))
               currentWeapon = 4;
         
         	//got poo?
            if(ammo[5]>0)
               currentWeapon = 5;
         
         	//the harder the ai, the less likely the target changes
            double changePercent = 0.6 - 0.1 * AI;
         
            if((target == null) || (Math.random() < changePercent))
               getNewTarget();
            //System.out.println("AI Changed targets to: "+target);
         
            setAImove(terrain);
            previousHealth = health;
         }
         else if(AI==0)
         {
         	//do human player initialization
         	////set picture to standstill
         	//otherstuff?
         }
      }
   
       public void fire(int vel)
      {
         int v;
         if(AI !=0)
         {
            //findVelAng();
            v = (int) power;
         }
         else
            v = vel;
         weapon = weaponSet[currentWeapon].getProjectile();
      
         weapon.setPlayerIndex(index);
         weapon.X(x+(41-weapon.width())/2);
         weapon.Y(y);
      
         if(currentWeapon==4)
         {
            int fleaChance = AI*10-10;
            v = (int)(Math.random()*(51-fleaChance) + fleaChance);
         }
         weapon.setAngleAndPower(((monkeyFace==1) ? (180+angle) : (-1*angle)), v);
         //System.out.println("angle: "+( (monkeyFace==1) ? (180-angle) : (angle) ));
      	
         //System.out.println(weapon.getX()+" "+weapon.getY()+" "+(angle)+" "+v);
      	
         if (currentWeapon != 0)
         {
            ammo[currentWeapon]--;
            if (currentWeapon == 5)
               doodieMeter = 0;
         }
         if (ammo[currentWeapon] <= 0)
            currentWeapon = 0;
      
         firing = true;
         readyToFire = false;
      }
   
       private void getNewTarget()
      {
      		//AI 1 is an idiot and is mostly random
      		//AI 2 burrows
      		//AI 3 shoots at last person who shot at them last
      		//AI 4 shoots at player with lowest health
      		//AI 5 shoots at last person who shot at them last
      	
			int targetIndex=0;
         if((target==null || (AI==2 && target.isDead())) || (AI!=0 && AI!=2))
         {
            targetIndex = (int)(Math.random()*(targetList.length));
         
            while(targetList.length > 1 && (targetIndex == index || targetIndex == allyIndex))
               targetIndex = (int)(Math.random()*(targetList.length));
         }
      
         if(AI == 2)
         {
         //i can't have this AI unless collision with projectiles and terrain works perfectly
         	//go after one target untill they die!
         	//if has been running into wall, then shoot at wall
         }
         else if(AI == 3 || AI == 5)
         {
            for(int a=0; a<targetList.length; a++)
            {
               if(a != allyIndex && a!=index && targetList[a].getCollision()==index)
                  targetIndex=a;
            }
         }
         
         else if(AI == 4)
         {
            int lowestHealth=100;
            for(int a=0; a<targetList.length; a++)
            {
               if(a != allyIndex && a!=index && targetList[a].getHealth()<lowestHealth)
               {
					   targetIndex = a;
						lowestHealth = targetList[a].getHealth();
					}
            }
         }
      
         if((target==null) || (!target.equals(targetList[targetIndex])))
         {
            target = targetList[targetIndex];
            low = 1; high = 100;
            double range =Math.abs(x - target.getX());
            for(int a = low; a <high; a++)
            {
               double temp = range * 9.8 / (a*a);
               if(temp <=1 && a <= high)
               {
                  low = a;
                  a+=100;
               }
            }
         }
      	
         if(x > target.getX())
            monkeyFace = moveDirection = 1;
         else if(x < target.getX())
            monkeyFace = moveDirection = 2;
      }
   
       public void findVelAng()
      {
         //System.out.println("findvelangle");
      	
			if (target.getHealth() <= 0)
				getNewTarget();
			
         if(x > target.getX())
            monkeyFace = moveDirection = 1;
         else if(x < target.getX())
            monkeyFace = moveDirection = 2;
      	
         double range = Math.abs(x - target.getX());
         double height = 16+Math.abs(y - target.getY());
         range = Math.sqrt(range*range + height*height);
      
      	//distance can't be greater than 1020 pixels for a 1 to 1 scale
      
         power = Math.random() * (high - low +1) + low;
      
         double temp = range * 9.8 / (power*power);
         double tempsin = Math.asin(temp);
         double degrees =  180/Math.PI * tempsin;
         double halfDeg = degrees / 2;
         angle = (int)(.5 + halfDeg);
      
         if((y - target.getY())>120 && !(angle+45>=90))
            angle+=45;
         else if((y - target.getY())>90 && !(angle+30>=90))
            angle+=30;
         else if((y - target.getY())>60 && !(angle+15>=90))
            angle+=15;
         
         else if((target.getY() - y)>120 && !(angle-15<0))
            angle-=15;
         else if((target.getY() - y)>120 && !(angle-10<0))
            angle-=10;
         else if((target.getY() - y)>120 && !(angle-5<0))
            angle-=5;
			
			//System.out.println(name + " targets " + target.getIndex() + " at " + (int)power + ", " + angle);
      }
   
       public void updateAIBounds()
      {
         weapon=nullWeapon;
      
         if(AI!=0)
         {
            if(collision == 5 || (collision == (allyIndex+1)));
            {
               int tx = target.getX();
            
               if( ((landx < tx) && (x>tx)) || ((landx > tx) && (x<tx)) )
               {
                  if(high == (int)power) high--;
                  else high = (int)power;
               }
            
               if( ((landx < tx) && (x<tx)) || ((landx > tx) && (x>tx)) )
               {
                  if((int)power == high)
                  {
                     moveDirection = ( ((tx-landx) > 0) ? (2) : (1) );
                     movement = Math.abs(tx-landx);
                  }
               
                  if(low == (int)power) low++;
                  else low = (int)power;
               }
            }
         }
      }
   
   
       public void move(int dx, BufferedImage terrain)
      {
         if (!(moving = collisionDetect(x+dx, terrain)))
         {
            x+=dx;
            int dy = ground - y;
            if (dx != 0 || ground > y)
            {
               y = ground;
               collisionBox.translate(dx,dy);
               center.translate(dx,dy);
            }
         }
      
         moving = !moving && dx != 0;
      
         if (dx < 0)
            monkeyFace = 1;
         else if (dx > 0)
            monkeyFace = 2;
      
         if (movX == 5 || curSprite == walkSprites[0] || curSprite == walkSprites[4])
         {
            movX = 0;
         
            if (moving)
            {
               counter = counter + incDec;
               if (counter == 1 || counter == 3)
                  incDec = incDec * -1;
               if (monkeyFace == 1)
                  curSprite = walkSprites[counter];
               else
                  curSprite = walkSprites[counter + 4];
            }
            else
               curSprite = walkSprites[4*monkeyFace-4];
         }
         else
         {
            movX++;
         }
      
         if(dx ==0)
            curSprite = walkSprites[4*monkeyFace-4];
      }
   
   	//true if collision, false otherwise
       public boolean collisionDetect(int X, BufferedImage terrain)
      {
         if (X < 10 || X > 974)
            return true;
      
         int i, j;
         if (terrain.getRGB(X+20, y-1) == 0x8F1C1C)
         {
            for(i=y;i<y+48-10;i++)
               if (i < 768 && terrain.getRGB(X+20, i) != 0x8F1C1C)
                  return true;
            for(i=y-3;i<720;i++)
               if (i > 0 && i < 720 && terrain.getRGB(X+20, i+48) != 0x8F1C1C)
               {
                  ground = i;
                  return false;
               };
            for(i=y;i<768;i++)
               if (i < 720 && terrain.getRGB(X+20, i+48) != 0x8F1C1C)
               {
                  ground = i;
                  return false;
               }
         }
         for(i=y-1;i>0&&i<720;i++)
            for(j=i;j<i+48;j++)
            {
               if (j < 0 && terrain.getRGB(X+20, j) != 0x8F1C1C)
                  i=j-1;
               else if (j == i+48)
               {
                  ground = i;
                  return false;
               }
            }
         return true;
      }
   
       public int AIcollisionDetect(int X, BufferedImage terrain)
      {
         Rectangle possibleBox = new Rectangle(collisionBox);
         if (targetList != null)
            for (int p=0;p<targetList.length;p++)
            {
               Rectangle box = targetList[p].getCollisionBox();
               if (p != index && possibleBox.intersects(box))
               {
                  return 2;}
            }
      
         if(collisionDetect(X, terrain))
            return 1;
         return 0;
      }
   
   	//determines how/where AI moves
   	//updates dy/dx variables
       private void setAImove(BufferedImage terrain)
      {
      //set movement and moveDirection
         			//decide which direction to go
         			//how long to move given time per turn
         if(movement != 0)
         {
            readyToFire = false;
         
            for(int a=1; a<=movement; a++)
            {
               int collision = AIcollisionDetect(( (moveDirection==1) ? (x-a) : (x+a)), terrain);
               if(collision != 0)
               {
               //if collides with player, see if AI can move past the player, if not, then stop in front of.
                  if(collision == 2)
                  {
                     movement=a+45;
                     for(int b=a; b<=(60+a); b++)
                     {
                        if(AIcollisionDetect(( (moveDirection==1) ? (x-a) : (x+a)), terrain) != 0)
                        {
                           movement-=45;
                           b+=(60+a+1);
                           a+=(movement+1);
                        }
                     }
                  }
                  movement = a-15;
                  if(movement <=0)
                  {
                     getNewTarget();
                     movement = 0;
                  }
                  a+=movement+1;
               }
            }
         	moveCounter++;
            return;
         }
      	//	-20x + x^2 + 85
         double chance = (-20 * AI + AI*AI + 85)/100;
         if((previousHealth != health) || (high == low) || (Math.random() < chance) )
         {
            movement =(int)(Math.random() * 64);
            moveDirection = (int)(Math.random() * (2)+1);
            readyToFire = false;
         
            for(int a=1; a<=movement; a++)
            {
               if(collisionDetect(( (moveDirection==1) ? (x-a) : (x+a)), terrain))
               {
                  movement = a-15;
                  if(movement <=0)
                  {
                     movement = Math.abs(movement);
                     getNewTarget();
                  }
                  a+=movement+1;
               }
            }
         
         }
			else if(moveCounter >=5)
			{
				moveCounter = 0;
				movement = (int)(Math.random() * 20);
			}
      }
   
       public void draw(Graphics2D g, int isCur)
      {
         //if isCur is 1, the current player is being drawn
         g.setColor(playerColor);
      
      	// finds where to and displays health
         String h = health + "";
         int c = x + 21 - 4*h.length();
         g.drawString(h, c, y);
      
      	// finds where to and displays player name
         c = x + 21 - 4*name.length();
         g.drawString(name, c, y-12);
			
			if (health <= 0)
			{
				g.drawImage(walkSprites[9+monkeyFace],x,y,parent);
				return;
			}
      
      	//g.drawRect((int)collisionBox.getX(),(int)collisionBox.getY(),(int)collisionBox.getWidth(),(int)collisionBox.getHeight());
         if (!moving && isCur == 1)
         {
            if (monkeyFace == 1)
            {
               g.drawImage(aimSprites[1], x,y,parent);
               if (getWeapon() == 3)
               {
                  g.drawImage(aimRotate(4), x-30,y-31,parent);
               }
               else
               {
                  g.drawImage(aimRotate(0), x-30,y-31,parent);
               }
            }
            else
            {
               g.drawImage(aimSprites[3], x,y,parent);
               if (getWeapon() == 3)
               {
                  g.drawImage(aimRotate(4), x-30,y-31,parent);
               }
               else
               {
                  g.drawImage(aimRotate(2), x-30,y-31,parent);
               }
            }
         
         }
         else
         {
            g.drawImage(curSprite, x, y, parent);
         }
      }
   
       public BufferedImage aimRotate(int i)
      {
         Image image = aimSprites[i];
      
         BufferedImage bi = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.BITMASK);
         Graphics2D g = bi.createGraphics();
         AffineTransform originAT = g.getTransform();
      
         AffineTransform rot = new AffineTransform();
         if (monkeyFace == 1)
         {
            rot.rotate(Math.toRadians((270 + angle)), image.getWidth(null)/2, image.getHeight(null)/2);
         }
         else
         {
            rot.rotate(Math.toRadians((90 - angle)), image.getWidth(null)/2, image.getHeight(null)/2);
         }
         g.transform(rot);
         g.drawImage(image, 0, 0, null);
         g.setTransform(originAT);
         g.dispose();
         return bi;
      }
   
   
       public int getHealth()
      {
         return health;
      }
   
       public void setHealth(int h)
      {
         health = h;
         curSprite = walkSprites[7+monkeyFace];
         if (health == 0)
            curSprite = walkSprites[9+monkeyFace];
      }
   
       public int getIndex()
      {
         return index;
      }
   
       public Weapon getWeapon(int i)
      {
         return weaponSet[i];
      }
   
       public int getCurrentWeaponIndex()
      {
         return currentWeapon;
      }
   
       public void setCurrentWeapon(int t)
      {
         currentWeapon = t;
      }
   
       public void setLandX(int x)
      {
         landx=x;
      }
   
       public int getDoodie()
      {
         return doodieMeter;
      }
   
       public void setDoodie(int d)
      {
         doodieMeter = d;
         if (doodieMeter >= 100)
         {
            doodieMeter = 100;
            ammo[5] = 1;
         }
      }
   
       public int getAmmo(int a)
      {
         return ammo[a];
      }
   
       public Projectile getProjectile()
      {
         return weapon;
      }
   
       public void changeAngle(int da)
      {
         if(da==1)
         {
            if (angle+da != 91)
            {
               angle = angle+da;
            }
         }
      
         if(da==-1)
         {
            if (angle+da != -1)
            {
               angle = angle+da;
            }
         
         }
      }
   
       public void setAngle(int ang)
      {
         angle=ang;
      }
   
       public int getAngle()
      {
         return angle;
      }
   
       public void setCollision(int c)
      {
         collision=c;
         if (collision >= 100)
         {
            collision = 100;
            ammo[5] = 1;
         }
      }
   
       public int getCollision()
      {
         return collision;
      }
   
       public InetAddress getIP()
      {
         return IP;
      }
   
       public boolean isAI()
      {
         return ( (AI!=0) ? (true) : (false) );
      }
   
       public int getMovement()
      {
         if(movement==0)
         {
            readyToFire = true;
            return 0;
         }
      
         readyToFire = false;
         movement--;
         return ( (moveDirection == 1) ? (-1) : (1) );
      }
   
       public boolean isReadyToFire()
      {
         return readyToFire;
      }
   
       public void setReadyToFire(boolean s)
      {
         readyToFire=s;
      }
   
       public boolean isFiring()
      {
         return firing;
      }
   
       public void setIsFiring(boolean f)
      {
         firing = f;
      }
   
       public int getWeapon()
      {
         return currentWeapon;
      }
   	
       public Weapon getCurrentWeapon()
      {
         return weaponSet[currentWeapon];
      }
   	
       public int getFacing()
      {
         return monkeyFace;
      }
   	
       public void setFacing(int m)
      {
         monkeyFace=m;
      }
   	
       public int getDirection()
      {
         return moveDirection;
      }
   	
       public void setDirection(int d)
      {
         moveDirection=d;
      }
   	
       public void setWeapon(int w)
      {
         currentWeapon = w;
      }
   
       public Color getColor()
      {
         return playerColor;
      }
   
       public void updatePlayerList(Player[] list, int newIndex)
      {
         targetList = list;
         index = newIndex;
         getNewTarget();
      }
   
       public boolean isDead()
      {
         return ( (health<=0) ? (true) : (false) );
      }
   
       public int getAI()
      {
         return AI;
      }
   
       public double getPower()
      {
         return power;
      }
   
   }
