//Weapon

   import java.awt.*;
   import javax.sound.sampled.*;
   import java.io.*;
   import java.awt.geom.*;
   import java.util.ArrayList;
   import java.applet.AudioClip;

    public class Weapon extends LevelElement
   {
      protected Projectile projectile;
   
      protected int ammo, firetype;
      protected AudioClip fireSound, hitSound;
   
   
       public Weapon(String name, Component p)
      {
         super(name,0,0,p);
         loadVars(name);
      }
   
       protected void loadVars(String file)
      {
      
         String temp = new String("");
         int cc=0, dmg=0,displacement=0,radius=0;
         double mod=0;
         String tempHitSound = new String(), tempFireSound = new String(), anim1 = new String(), anim2 = new String();
         if(!file.endsWith(".wpn"))
            file+=".wpn";
         try
         {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("Weapons/"+file)));
            while((temp=in.readLine())!= null )
            {
               if(cc==0)
                  name = temp;
               else if(cc==1)
                  ammo=Integer.parseInt(temp);
               else if(cc==2)
                  firetype=Integer.parseInt(temp);
               else if(cc==3)
                  dmg=Integer.parseInt(temp);
               else if(cc==4)
                  mod=Double.parseDouble(temp);
               else if(cc==5)
                  radius=Integer.parseInt(temp);
               else if(cc==6)
                  tempFireSound = temp;
               else if(cc==7)
                  tempHitSound = temp;
               else if(cc==8)
                  displacement=Integer.parseInt(temp);
               else if(cc==9)
                  anim1=temp;
               else if(cc==10)
                  anim2=temp;
               cc++;
            }
            in.close();
         
            String[] tempString = {anim1, anim2};
         					//System.out.println("animation loaded");
            Image[] tempImages = getImages(tempString);
         					//System.out.println("images got");
            fireSound = getAudio(tempFireSound);
            hitSound = getAudio(tempHitSound);
            System.out.println("sound got");
            projectile = new Projectile(name,0,0,0,dmg,mod,displacement,radius,tempImages,parent);
         	//System.out.println("projectle set");
         }
             catch(Exception e){System.out.println("exception");}
      }
   
       public int getAmmo()
      {
         return ammo;
      }
   
       public void playFireSound()
      {
         if(fireSound!=null) fireSound.play();}
       public void stopFireSound()
      {
         if(fireSound!=null) fireSound.stop();}
       public void playHitSound()
      {
         if(hitSound!=null) hitSound.play();}
       public void stopHitSound()
      {
         if(hitSound!=null) hitSound.stop();}
   
       public Projectile getProjectile()
      {
         return projectile;
      }
   
   }