package rubi.dev.pictoarray;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

public class crop extends AppCompatActivity {
    public Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        uri = ((Globals) this.getApplication()).getUri();
        openCropActivity(uri, uri);
      }

    public void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(2000, 2000)
                .withAspectRatio(5f, 5f)
                .start(this);
        finish();
        }
}