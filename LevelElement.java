//Level Element

   import java.awt.*;
   import java.awt.geom.*;
   import javax.sound.sampled.*;
   import java.io.*;
   import java.applet.Applet;
   import java.applet.AudioClip;

   import java.awt.image.*;


    public class LevelElement
   {
      protected int x,y;
      protected String name;
      protected Component parent;
      protected Rectangle collisionBox;
      protected Point center;
      
       public LevelElement()
      {
      }
       
       public LevelElement(String name, int x, int y, Component parent)
      {
         this.name=name;
         this.x=x;
         this.y=y;
         this.parent=parent;
         center = new Point(x,y);
      }
      
       public void X(int x)
      {
         this.x=x;
         collisionBox.move(x, y);
      }
   	
       public void Y(int y)
      {
         this.y=y;
         collisionBox.move(x, y);
      }
      
       public int getX()
      {
         return x;
      }
   	
       public int getY()
      {
         return y;
      }
   
       public String getName() 	
      {
         return name;
      }
   	
       public void scroll(int dx, int dy)
      {
         x+=dx;
         y+=dy;
         collisionBox.translate(dx,dy);
         center.translate(dx,dy);
      }
      
       public int distance(LevelElement g)
      {
         return (int)Math.sqrt(((Math.pow(getX(),2)-Math.pow(g.getX(),2))+(Math.pow(getY(),2)-Math.pow(g.getY(),2))));
      }
   
   
       public Rectangle getCollisionBox()
      {
         return collisionBox;
      }
   
       public boolean collision(Line2D.Double other)
      {
         return other.intersects(collisionBox);
      }
   
       public boolean collision(Rectangle other)
      {
         return other.intersects(collisionBox);
      }
   
       protected boolean visible()
      {
         if(x>-10 && x<810 && y>-10 && y<610)
            return true;
         else
            return false;
      }
   
    /*   public Clip getAudio(String filename) throws Exception
      {
   	System.out.println("getting stream");
         AudioInputStream stream = AudioSystem.getAudioInputStream(new File("Resources/Sounds/"+filename+".au"));
   	System.out.println("getting format");
         AudioFormat format = stream.getFormat(); 
   	System.out.println("getting info");
         DataLine.Info info = new DataLine.Info( Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize())); 
   	System.out.println("getting line");
         Clip fire = (Clip) AudioSystem.getLine(info);
   	System.out.println("opening");
         fire.open(stream);
         return fire;
      }  */
       public AudioClip getAudio(String filename) throws Exception
      {
         System.out.println(filename);
         if(filename.equals("nothing")) 
            return null;
         AudioClip fire = Applet.newAudioClip(getClass().getResource("Resources/Sounds/"+filename+".au"));
         return fire;
      }  
      
       public Image[] getImages(String[] filenames)
      {
         Image[] ret = new Image[filenames.length];
         MediaTracker watch=new MediaTracker(parent);
         for(int t=0; t<filenames.length;t++)
         {
            if(filenames[t].endsWith(".gif"))
               ret[t] = Toolkit.getDefaultToolkit().getImage(Engine.IMAGE_PATH+filenames[t]);
            else
               ret[t] = Toolkit.getDefaultToolkit().getImage(Engine.IMAGE_PATH+filenames[t]+".gif");
         	//System.out.println(ret[t]);        
            watch.addImage(ret[t],t);
         }
         try {
            watch.waitForAll();
         } 
             catch (InterruptedException i) {}
         return ret;
      }  
   	
       public String toString()
      {
         return new String(name+" x: "+x+" y: "+y);
      }
   	
   	
   	
   }