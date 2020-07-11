package rubi.dev.pictoarray;

import android.app.Application;
import android.net.Uri;

public class Globals extends Application {
   private Uri uri;

   public Uri getUri(){
       return this.uri;
   }

   public void setUri(Uri d){
       this.uri=d;
   }
}
