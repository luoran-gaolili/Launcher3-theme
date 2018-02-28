package com.rgk.theme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class SourceFactory {

     public static final String TAG="SourceFactory";
     public static final String THEME_PREVIEW="Preview/";
     public static final String THEME_PATH="/system/themes";
     public static final String THEME_PRE1="preview_launcher_1.png";
     public static final String THEME_PRE2="preview_launcher_2.png";
     public static final String THEME_KEYGUARD="preview_keyguard.png";

       private static List<Theme> mSource;
       public static List<Theme> getSource(){
           if(mSource==null){
               mSource = new ArrayList<Theme>();
                File f = new File(THEME_PATH);
                if(f.exists()&&f.isDirectory()){
                    Drawable preview=null;
                    Drawable launcher=null;
                    Drawable keyguard=null;
                    for(File tp:f.listFiles()){
                        preview = getDrawableFromZip(tp.getAbsolutePath(), THEME_PRE1);
                        launcher = getDrawableFromZip(tp.getAbsolutePath(), THEME_PRE2);
                        keyguard = getDrawableFromZip(tp.getAbsolutePath(), THEME_KEYGUARD);           
                        String themename= tp.getName();
                        if(themename.endsWith(".zip")){
                            themename=themename.substring(0, themename.length()-4);
                        }
                        Theme theme = new Theme(themename);
                        theme.setPreview(preview);
                        theme.setLauncher(launcher);
                        theme.setKeyguard(keyguard);
                        Log.d(TAG,"now preview property is:"+preview.getBounds().toString()+","+preview.getIntrinsicHeight()+","+preview.getIntrinsicWidth());
                        mSource.add(theme);
                        Log.d(TAG,"add theme,and theme name is:"+theme.getName());
                    }
                    preview=null;
                    launcher=null;
                    keyguard=null;
                }
           }
           return mSource;
       }

      private static Drawable getDrawableFromZip(String zipName,String pcName){
        Drawable temp=null;
           try {
               ZipFile zf = new ZipFile(zipName);
               ZipEntry ze = zf.getEntry(THEME_PREVIEW+pcName);
               InputStream is = null;
               if(ze!=null){
                   is = zf.getInputStream(ze);
                   temp=Drawable.createFromStream(is, null);
                   is.close();
               }

           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
           return temp;
       }

}
