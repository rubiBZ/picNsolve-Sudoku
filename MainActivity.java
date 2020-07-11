package rubi.dev.pictoarray;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;



public class MainActivity extends AppCompatActivity {

    public int CAMERA_ACTION_PICK_REQUEST_CODE = 1;
    public String currentPhotoPath;
    public Uri photoURI = null;
    public Bitmap bitmap;
    public InputImage image;
    public File photoFile;
    public Task<Text> result;
    public String txt = "";
    static final int PIC_CROP = 2;
    static final int REQUEST_TAKE_PHOTO = 1;
    public Uri uri;
    public Intent crop;

    int oldBT;
    int newBT;
    public int[][][] sdk = new int[9][9][12];
    int i = 0;
    int j = 0;
    int[] thatReturn = new int[3];
    public int resolt = 2;
    int numCheck = 0;
    int VectorsCheck = 1;
    public Date d = new Date();
    public long startTime;
    public long currentTime;
    public long diff;
    boolean OK = true;
    boolean find = false;
    public boolean HintOn=false;
    public int InputCount=0;
    public boolean cellErr=false;
     private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
               @Override
               public void onInitializationComplete(InitializationStatus initializationStatus) {
               }
          });

          mAdView = findViewById(R.id.adView);
          AdRequest adRequest = new AdRequest.Builder().build();
          mAdView.loadAd(adRequest);

        InputCount=0;
        oldBT = findViewById(R.id.b11).getId();
        newBT = oldBT;
        findViewById(R.id.b11).setBackgroundResource(R.drawable.grey_affter);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sdk[i][j][0] = 0;
                sdk[i][j][1] = 0;
            }
        }

        ImageButton bt1 = findViewById(R.id.IBcam);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private File createImageFile() throws IOException { // Create an image.xml file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = "file:" + image.getAbsolutePath(); // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    public void dispatchTakePictureIntent() {

        InputCount=0;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // Ensure that there's a camera activity to handle the intent
            photoFile = null; // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {// Error occurred while creating the File
                Toast.makeText(this, "Failed to crate a file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) { // Continue only if the File was successfully created
                photoURI = FileProvider.getUriForFile(this,
                        "rubi.dev.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);//=1
                crop = new Intent(MainActivity.this, crop.class);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_ACTION_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = Uri.parse(currentPhotoPath);
            ((Globals) this.getApplication()).setUri(uri);
        }

        switch (requestCode) {
            case 1:
                startActivityForResult(crop, PIC_CROP); //=2
                break;
            case 2:
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runTextRecognition();
                break;
        }
    }

    public void runTextRecognition() {

        image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        try {
                            tackresulds();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }// Task completed successfully
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Can't recognize text, Try Again", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    public void tackresulds() throws IOException {
        View z= findViewById(android.R.id.content);
        clearBoard(z);
        Integer[][] call = new Integer[81][5];
        int i = 0;
        int PicSize = bitmap.getHeight();
        int CellSize = (PicSize - 40) / 9;
        try {
            for (Text.TextBlock block : result.getResult().getTextBlocks()) {
                for (Text.Line line : block.getLines()) {
                    for (Text.Element element : line.getElements()) {
                        String elementText = element.getText();
                        call[i][1] = element.getBoundingBox().centerX();
                        call[i][2] = element.getBoundingBox().centerY();
                        if(elementText.length()>1) {
                            elementText=String.valueOf(elementText.charAt(0));
                            call[i][1]=call[i][1]-(int)CellSize/2;
                           // call[i][2]=call[i][2]-(int) CellSize/2;
                            Toast.makeText(this, "double "+elementText+" i="+i, Toast.LENGTH_LONG).show();
                       }
                        call[i][0] = Integer.valueOf(elementText);
                        call[i][3] = (int) ((call[i][1] - 20) / CellSize);
                        call[i][4] = (int) ((call[i][2] - 20) / CellSize);

                        sdk[call[i][4]][call[i][3]][0] = call[i][0];
                        sdk[call[i][4]][call[i][3]][1] = 3;
                        if (call[i][0]>0) InputCount++;
                        String buttonID = "b" + (call[i][4] + 1) + (call[i][3] + 1);
                        int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                        Button b = (Button) findViewById(resID);
                        b.setText(elementText);
                        b.setTextColor(Color.BLACK);

                        i++;
                    }
                }
            }
            Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
        }
        catch (Exception all) {
            Toast.makeText(this, "Sorry, couldn't recognize the numbers, pleas try again.", Toast.LENGTH_SHORT).show();
        }

    }


    public void getInput(View v) {
        char oldCollor = findViewById(oldBT).getTag().toString().charAt(3);
        char newCollor = v.getTag().toString().charAt(3);
        int newBT = v.getId();
        if (oldCollor=='W') findViewById(oldBT).setBackgroundResource(R.drawable.white_before);
        if (oldCollor=='G') findViewById(oldBT).setBackgroundResource(R.drawable.grey_before);
        if (newCollor=='W') v.setBackgroundResource(R.drawable.white_affter);
        if (newCollor=='G') v.setBackgroundResource(R.drawable.grey_affter);
        oldBT = newBT;
    }//getInput

    public void pickNumber(View v) {
        boolean valid = false;
        Button b = (Button) v;
        String PN = b.getText().toString();
        Button mButton = (Button) findViewById(oldBT);
        mButton.setText(PN);
        int x = Character.getNumericValue(mButton.getTag().toString().charAt(1));
        int y = Character.getNumericValue(mButton.getTag().toString().charAt(2));
        sdk[x - 1][y - 1][0] = Integer.parseInt(PN);
        sdk[x - 1][y - 1][1] = 1;
        valid = validCellCheck(x - 1, y - 1);
        if (!valid) {
            mButton.setTextColor(Color.RED);
            cellErr=true;
            resolt = 0;}
        else {
            InputCount++;
            cellErr=false;
            mButton.setTextColor(Color.BLACK);
            resolt = 2;
            if(InputCount==81) Toast.makeText(this, "Well Done!", Toast.LENGTH_LONG).show();
        }
     //   valid=true;
    }

    public void dell(View v) {
     //   if (!HintOn) {
            Button mButton = (Button) findViewById(oldBT);
            mButton.setText("");
            int x = Character.getNumericValue(mButton.getTag().toString().charAt(1));
            int y = Character.getNumericValue(mButton.getTag().toString().charAt(2));
            resolt = 2;
            sdk[x - 1][y - 1][0] = 0;
            sdk[x - 1][y - 1][1] = 0;
            InputCount=InputCount-1;
            cellErr=false;
            clearHistory();
            inputCheck(v);
       // }
     //   else Toast.makeText(MainActivity.this, "DEL not allowed after HINT", Toast.LENGTH_LONG).show();
    }

    public void solve(View v) {
        i = 0;
        j = 0;
        resolt = 2;
        if (sdk[i][j][1] > 0) {
            thatReturn = forward(i, j);
            i = thatReturn[0];
            j = thatReturn[1];
            resolt = thatReturn[2];
        }
        clearHistory();
        inputCheck(v);
       // boardCheck(v);
        if (!OK||cellErr)Toast.makeText(MainActivity.this, "Please change INPUT", Toast.LENGTH_SHORT).show();
        while (resolt == 2 && OK && !cellErr) {
            numCheck = 0;
            while (numCheck < 9 && i > -1 && i < 9) { //check 9 numbers for each sdk[i][j]
                sdk[i][j][0] = upInCircle(sdk[i][j][0], 1, 9, 1);// NEXT SDK
                int[] history = sdk[i][j]; // when we go Forward History is empty, when going Back we have History
                boolean historyCheck = triedThisSDK(history);
                int ii = i;
                int jj = j;
                int InBoxI = i;
                int InBoxJ = j;
                int CornerI = i / 3;
                CornerI = CornerI * 3;
                int CornerJ = j / 3;
                CornerJ = CornerJ * 3; // find beginning of BOX
                VectorsCheck = 1;
                while (VectorsCheck < 9 && i > -1 && i < 9) {//check 9 times the new SDK in 3 vectors
                    ii = upInCircle(ii, 0, 8, 1);
                    jj = upInCircle(jj, 0, 8, 1);
                    InBoxJ = upInCircle(InBoxJ, CornerJ, (CornerJ + 2), 1);
                    if (InBoxJ == CornerJ) InBoxI = upInCircle(InBoxI, CornerI, (CornerI + 2), 1);
                    if (sdk[i][j][0] == sdk[ii][j][0] || sdk[i][j][0] == sdk[i][jj][0] || sdk[i][j][0] == sdk[InBoxI][InBoxJ][0] || historyCheck) { //if find match in one of 3 vectors or history
                        VectorsCheck = 10;
                    }// check failed - will try SDK++
                    else VectorsCheck++;
                } // VectorCheck
                if (VectorsCheck == 10) numCheck++;
                else numCheck = 9;
            } //numCheck

            if (VectorsCheck == 9 && numCheck == 9) {//all tests for this SDK are OK, can move FORWARD
                thatReturn = forward(i, j);
                i = thatReturn[0];
                j = thatReturn[1];
                resolt = thatReturn[2];
            } // send forward

            if (VectorsCheck == 10 && numCheck == 9) {
                thatReturn = backward(i, j);
                i = thatReturn[0];
                j = thatReturn[1];
                resolt = thatReturn[2];
            } // send backward
        } //resolt

        if (resolt == 0 && sdk[8][8][1]==0)
            Toast.makeText(MainActivity.this, "Cant find a solution", Toast.LENGTH_LONG).show(); //no solution
        if (resolt == 1) {
           // we have a solution
        }
        // boardCheck();
    } // solve

    public void calc(View v){
        solve(v);
        InputCount=81;
        if (resolt==1) showResults();
    }

    public void hint(View v){
        HintOn=true;
        int count=0 ,x, y;
        find=false;
        Random rand = new Random();
        solve(v);
        if(resolt==1) {
            while (!find && count < 82) {
                count++;
                x = 1 + rand.nextInt(9);
                y = 1 + rand.nextInt(9);
                if (sdk[x - 1][y - 1][1] == 0) {
                    String buttonID = "b" + (x) + (y);
                    int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                    Button b = (Button) findViewById(resID);
                    b.setText(String.valueOf(sdk[x - 1][y - 1][0]));
                    b.setTextColor(Color.BLUE);
                    sdk[x - 1][y - 1][1] = 2;
                    find = true;
                    InputCount++;
                    if (InputCount == 81)
                        Toast.makeText(this, "Well Done!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void showResults() {
        for (int jjj = 0; jjj < 9; jjj++) { // write results on screen
            for (int iii = 0; iii < 9; iii++) {
                String s="";
                if(sdk[iii][jjj][0]!=0) s= String.valueOf(sdk[iii][jjj][0]);
                String buttonID = "b" + (iii + 1) + (jjj + 1);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                Button b = (Button) findViewById(resID);
                b.setText(s);
                if (sdk[iii][jjj][1] ==0 || sdk[iii][jjj][1] ==2) b.setTextColor(Color.BLUE);
                else b.setTextColor(Color.BLACK);
            }
        }
    }


    public int[] forward(int i, int j) {
        boolean notFree = true;
        if (sdk[i][j][1] == 0) {
            sdk[i][j][2]++;//add 1 to flag triedThisSDK
            sdk[i][j][2 + sdk[i][j][2]] = sdk[i][j][0];
        }  //store SDK that we tried in history
        while (notFree) {
            if (isOdd(i) && j == 0) i++; // I forward in 1
            else if (isOdd(i) && j != 0) j--; // J forward in 1
            else if (!isOdd(i) && j != 8) j++; // J forward in 2
            else if (!isOdd(i) && j == 8) i++; // I forward in 2
            if (i > 8) {
                resolt = 1;
                notFree = false;
            } else if (sdk[i][j][1] == 0) notFree = false;
        }
        thatReturn[0] = i;
        thatReturn[1] = j;
        thatReturn[2] = resolt;
        return thatReturn;
    }

    public int[] backward(int i, int j) {
        boolean stopBack = false;
        while (!stopBack) {
            if (sdk[i][j][1] == 0)
                for (int inHistory = 0; inHistory < 12; inHistory++) sdk[i][j][inHistory] = 0;
            if (!isOdd(i) && j != 0) j--; // J back in 2
            else if (!isOdd(i) && j == 0) i--; // I back in 2
            else if (isOdd(i) && j != 8) j++; // J back in 1
            else if (isOdd(i) && j == 8) i--; // I back in 1
            // d = new Date();
            //currentTime=d.getTime();
            //diff=currentTime-startTime;
            if (i < 0) {
                stopBack = true;
                resolt = 0;
            }
            //if(i<0 || diff >20000){stopBack=true; resolt=0;}
            else if ((sdk[i][j][1] == 0) && (sdk[i][j][2] < 9)) stopBack = true;
        }
        thatReturn[0] = i;
        thatReturn[1] = j;
        thatReturn[2] = resolt;
        return thatReturn;
    }

    public boolean triedThisSDK(int[] crntVector) {
        int ans = 0;
        if (crntVector[2] > 0) {
            for (int jmp = 3; jmp < (3 + crntVector[2]); jmp++) {
                if (crntVector[0] == crntVector[jmp]) ans = 1;
            }
        }
        return ans != 0;
    }

    public static int upInCircle(int current, int min, int max, int step) {
        if (current + step <= max) return current + step;
        else return min;
    }

    public static boolean isOdd(int numToCheck) {
        int tt = numToCheck / 2;
        return tt * 2 != numToCheck;
    }

    public void clearHistory() {
        for (int clearI = 0; clearI < 9; clearI++)
            for (int clearJ = 0; clearJ < 9; clearJ++) {
                for (int index = 2; index < 12; index++) sdk[clearI][clearJ][index] = 0;
                if (sdk[clearI][clearJ][1] != 0) {
                   // if(sdk[clearI][clearJ][1]>1) sdk[clearI][clearJ][1] = 1;
                    sdk[clearI][clearJ][2] = 1;
                    sdk[clearI][clearJ][3] = sdk[clearI][clearJ][0];
                }
            }
    }

    public boolean validCellCheck(int inI, int inJ) {
        boolean toreturn = true;
        int ii = inI;
        int jj = inJ;
        int InBoxI = inI;
        int InBoxJ = inJ;
        int CornerI = inI / 3;
        CornerI = CornerI * 3;
        int CornerJ = inJ / 3;
        CornerJ = CornerJ * 3; // find beginning of BOX
        int VC = 0;
        while (VC < 8) {
            ii = upInCircle(ii, 0, 8, 1);
            jj = upInCircle(jj, 0, 8, 1);
            InBoxJ = upInCircle(InBoxJ, CornerJ, (CornerJ + 2), 1);
            if (InBoxJ == CornerJ) InBoxI = upInCircle(InBoxI, CornerI, (CornerI + 2), 1);
          //  if (sdk[inI][inJ][0] == sdk[ii][inJ][0] || sdk[inI][inJ][0] == sdk[inI][jj][0] || sdk[inI][inJ][0] == sdk[InBoxI][InBoxJ][0])
            if ((sdk[ii][inJ][1]!=0&&sdk[inI][inJ][0] == sdk[ii][inJ][0]) ||
                (sdk[inI][jj][1]!=0&&sdk[inI][inJ][0] == sdk[inI][jj][0]) ||
                (sdk[InBoxI][InBoxJ][1]!=0&&sdk[inI][inJ][0] == sdk[InBoxI][InBoxJ][0]))
                VC = 10;
            else VC++;
        }
        if (VC == 10) toreturn = false;
        return toreturn;
    }

    public void boardCheck(View v) {
        OK = true;
        int boardI = 0;
        while (OK && boardI < 9) {
            int boardJ = 0;
            while (OK && boardJ < 9) {
                if (sdk[boardI][boardJ][0] > 0) OK = validCellCheck(boardI, boardJ);
                else OK = false;
                boardJ++;
            }
            boardI++;
        }
        if (OK) Toast.makeText(MainActivity.this, "Solution is OK!", Toast.LENGTH_LONG).show();
        else Toast.makeText(MainActivity.this, "Not a valid solution!", Toast.LENGTH_LONG).show();
    }

    public void inputCheck(View v) {
        OK = true;
        int boardI = 0;
        while (boardI < 9) {
            int boardJ = 0;
            while (boardJ < 9) {
                if (sdk[boardI][boardJ][0] > 0) OK = validCellCheck(boardI, boardJ);
                String buttonID = "b" + (boardI + 1) + (boardJ + 1);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                Button b = (Button) findViewById(resID);
                if (!OK) b.setTextColor(Color.RED);
                else if(sdk[boardI][boardJ][1]==0||sdk[boardI][boardJ][1]==2) b.setTextColor(Color.BLUE);
                    else b.setTextColor(Color.BLACK);
                boardJ++;
            }
            boardI++;
        }
        if (OK) resolt = 2;
        else {
            Toast.makeText(MainActivity.this, "Please change INPUT", Toast.LENGTH_SHORT).show();
            resolt = 0;
        }
    }

    public void clearBoard(View v) {
        HintOn=false;
        resolt=2;
        InputCount=0;
        for (int clearI = 0; clearI < 9; clearI++)
            for (int clearJ = 0; clearJ < 9; clearJ++)
                for (int index = 0; index < 12; index++) {
                    sdk[clearI][clearJ][index] = 0;
                    String buttonID = "b" + (clearI + 1) + (clearJ + 1);
                    int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                    Button b = (Button) findViewById(resID);
                    b.setText("");
                }
    }

}

//***************

