//Projectile

   import java.awt.*;
   import javax.sound.sampled.*;
   import java.awt.geom.*;
   import java.awt.image.BufferedImage;
   import javax.swing.JOptionPane;

    public class Projectile extends LevelElement
   {
      private int damage;
      private int radius;
      private int angle;
      private int power;
      private int width;
      private int displacement;
      private double distanceModifier;
      private double dx,dy, possibleX, possibleY;
      private double doubleX, doubleY;
      private Image[] projectileSprites;
      private int currentSpriteIndex;
      private int hitX, hitY;
      private Clip hitsound;
      private double SCALE_METERS = 1; //1020width/1020distance
      private GraphicsConfiguration gc; //needed to rotate sprites
      private int collision;
      private int playerIndex;
   
       public Projectile(String name, int x, int y, int playerIndex, int dmg, double modifier, int displace, int rad, Image[] sprites, Component parent)
      {
         super(name,x,y,parent);
         damage = dmg;
         distanceModifier = modifier;
         displacement = displace;
         radius = rad;
         projectileSprites = sprites;
         currentSpriteIndex=0;
      
      	// angle, power, and dy dx
         angle = 0;
         power = 0;
         findDYDX();
         angle = -999;
         power = -1;
      
      	// collision box & hit variables
         width = 32;//sprites[0].getWidth(parent);
         collisionBox = new Rectangle(x,y,width,width);
         hitX = -1;
         hitY = -1;
      
         collision = 0;
         this.playerIndex = playerIndex; // used for determining who fired
      
      	//create a graphics configuration for the screen.
      	//needed for using AffineTransform to rotate images
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gs = ge.getDefaultScreenDevice();
         gc = gs.getDefaultConfiguration();
      }
   
   	//returns
   		//0 for no collision
   		//1 for player collision
   		//2 for ground collision
       private int collisionDetect(Player[] players, BufferedImage terrain)
      {
      	//add dx to x
      	//update dy with gravity
      	//add dy from projectile to projectile y
         possibleX=dx;
         dy=addGravity();
         possibleY=dy;
      
      	// finds xFactor and yFactor to check in a diagonal line
         double xFactor = possibleX / Math.abs(possibleY);
         double yFactor = possibleY / Math.abs(possibleX);
         if (Math.abs(xFactor) > 1)
            xFactor = possibleX / Math.abs(possibleX);
         else if (Math.abs(yFactor) > 1)
            yFactor = possibleY / Math.abs(possibleY);
      
      	// acquire vertices to check
         double V1x=0, V1y=0, V2x=0, V2y=0, V3x=0, V3y=0, corner = ((Math.sqrt(2.0)*width) / 2.0);
      
         if (possibleX < 0)
         {
            V1x = V2x = corner;
            V3x = width - corner;
            V1y = corner;
            V2y = width - corner;
         
            if (possibleY < 0)
               V3y = corner;
            else if (possibleY > 0)
               V3y = width - corner;
            else
            {
               V3x = V1x;
               V3y = V1y;
            }
         }
         else if (possibleX > 0)
         {
            V1x = V2x = width - corner;
            V3x = corner;
            V1y = corner;
            V2y = width - corner;
         
            if (possibleY < 0)
               V3y = corner;
            else if (possibleY > 0)
               V3y = width - corner;
            else
            {
               V3x = V1x;
               V3y = V1y;
            }
         }
         else
         {
            V1x = V3x = corner;
            V2x = width - corner;
            if (possibleY >= 0)
               V1y = V2y = V3y = corner;
            else if (possibleY < 0)
               V1y = V2y = width - corner;
         }
      
         double i = x, j = y;
         boolean case1 = true, case2 = true;
      
      	// checks for collisions
         while (case1 && case2)
         {
            collisionBox.setLocation((int)i, (int)j);
         
            if (players != null)
               for (int p=0;p<players.length;p++)
               {
                  Rectangle box = players[p].getCollisionBox();
                  if (p != playerIndex && (box.contains(i+V1x, j+V1y) || box.contains(i+V2x, j+V2y) || box.contains(i+V3x, j+V3y)))
                  {
                     hitX = x = (int)i;
                     hitY = y = (int)j;
                  	//System.out.println("hit player " + (p+1) + " at " + (int) x + "," + (int) y);
                     return collision = p+1;
                  }
               }
         
            if (i+width >= 1024 || i <= 0 || j+width >= 768 || j <= -300) // off map
            {
               hitX = x = (int)i;
               hitY = y = (int)j;
            	//System.out.println("hit boundry at " + (int) x + "," + (int) y);
               return collision = 5;
            }
         
            if ((j>0) && (terrain.getRGB((int)(i+V1x),(int)(j+V1y))!=0x8F1C1C || terrain.getRGB((int)(i+V2x),(int)(j+V2y))!=0x8F1C1C || terrain.getRGB((int)(i+V3x),(int)(j+V3y))!=0x8F1C1C))
            {
               hitX = x = (int)i;
               hitY = y = (int)j;
            	//System.out.println("hit ground at " + (int) x + "," + (int) y);
               return collision = 5;
            }
         
            i += xFactor;
            j += yFactor;
         
            if (possibleX > .0000000001)
               case1 = i < x + possibleX;
            else if (possibleX < .0000000001)
               case1 = i > x + possibleX;
            else
               case1 = true;
            if (possibleY > .0000000001)
               case2 = i < y + possibleY;
            else if (possibleY < .0000000001)
               case2 = i > y + possibleY;
            else
               case2 = true;
         }
      
         x += (int)possibleX;
         y += (int)possibleY;
         collisionBox.setLocation(x, y);
         return 0;
      }
   
    	//returns
   		//0 for no collision
   		//1-4 for player collision
   		//5 for ground collision
       public int move(Player[] players, BufferedImage terrain)
      {
         int collision = collisionDetect(players, terrain);
         if (collision == 0)
         {
            //add dx to x
         	//add dy from projectile to projectile y
         
            currentSpriteIndex++;
            if(currentSpriteIndex==projectileSprites.length)
               currentSpriteIndex=0;
         }
         return collision;
      }
   
   	//to show the image rotating, the rotate method must be called to get the image to paint
       public BufferedImage rotate()
      {
         Image image = projectileSprites[currentSpriteIndex];
         BufferedImage bi = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.BITMASK);
         Graphics2D g = bi.createGraphics();
         AffineTransform originAT = g.getTransform();
      
         AffineTransform rot = new AffineTransform();
         rot.rotate(Math.atan(dy/dx), image.getWidth(null)/2, image.getHeight(null)/2);
         g.transform(rot);
         g.drawImage(image,0,0,null);
         g.setTransform(originAT);
         g.dispose();
         return bi;
      }
   
       private void findDYDX()
      {
         if(power==-1||angle==-999)
         {
            JOptionPane.showMessageDialog(null,"power/angle isn't set, projectile can't fire!!", "error! set Angle first!", JOptionPane.ERROR_MESSAGE);
            return;
         }
      	//find xvelocity
         double xVel = power * distanceModifier * Math.cos(Math.toRadians(angle)) / 10.0;
         dx = xVel*SCALE_METERS;
      
      	//find yvelocity
         double yVel = power * distanceModifier * Math.sin(Math.toRadians(angle)) / 10.0;
         dy = yVel*SCALE_METERS;
      
         if(name.equals("Banana Cannon"))
         {
            dx*=100.0/power;
            dy*=100.0/power;
         }
      }
   
       public double addGravity()
      {
         double gPix = 0;
         if(!name.equals("Banana Cannon"))
            gPix = (-.098)*SCALE_METERS;
         return dy-gPix;
      }
   
       public void setAngleAndPower(int a, int p)
      {
         angle = a;
         power = p;
         if (name.equals("De-Flea"))
            damage = (int) (.30 * (50.0 - Math.abs(power - 50.0)));
         findDYDX();
      }
   
       public int getAngle()
      {
         return angle;
      }
   
       public int getPower()
      {
         return power;
      }
   
       public int width()
      {
         return width;
      }
   
       public void setPlayerIndex(int p)
      {
         playerIndex = p;
      }
   
       public int getHitX()
      {
         return hitX;
      }
   
       public int getHitY()
      {
         return hitY;
      }
   
       public int damage()
      {
         return damage;
      }
   
       public int radius()
      {
         return radius;
      }
   
       public int getCurrentSpriteIndex()
      {
         return currentSpriteIndex;
      }
   
       public Image getCurrentImage()
      {
         return projectileSprites[currentSpriteIndex];
      }
   
       public int getCollision()
      {
         return collision;
      }
   
       public void draw(Graphics2D g)
      {
      	//g.setColor(Color.red);
      	//g.fillRect((int)collisionBox.getX(),(int)collisionBox.getY(),(int)collisionBox.getWidth(),(int)collisionBox.getHeight());
      
      	//rotate() returns the rotated image
         g.drawImage(rotate(), x,y, parent);
      }
   
       public void explode(BufferedImage terrain)
      {
      	//explode animation
      
      	//destroy terrain
      
      }
   }