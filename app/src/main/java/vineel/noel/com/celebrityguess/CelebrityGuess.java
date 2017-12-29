package vineel.noel.com.celebrityguess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CelebrityGuess extends AppCompatActivity {
    List<String> celebUrls = new ArrayList<>();
    List<String> celebNames = new ArrayList<>();
    int chosenCeleb = 0;
    int locationOfCorrectAns = 0;
    String[] answers = new String[4];
    ImageView ivCelebPic;
    Button btnChooseCeleb1,btnChooseCeleb2,btnChooseCeleb3,btnChooseCeleb4;

    public void celebChosen(View view) {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAns))){
            Toast.makeText(getApplicationContext(),"Correct!",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),"Wrong! it was "+celebNames.get(chosenCeleb),Toast.LENGTH_LONG).show();
        }

        createQuestion();
    }

    public void createQuestion(){
        Random random = new Random();
        chosenCeleb = random.nextInt(celebUrls.size());
        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage;

        try {
            celebImage = imageTask.execute(celebUrls.get(chosenCeleb)).get();
            ivCelebPic.setImageBitmap(celebImage);

            locationOfCorrectAns = random.nextInt(4);

            for(int i=0; i<4; i++){
                if(locationOfCorrectAns == i){
                    answers[i] = celebNames.get(chosenCeleb);
                }else{
                    int incorrectAns = random.nextInt(celebUrls.size());
                    while(incorrectAns == chosenCeleb){
                        incorrectAns = random.nextInt(celebUrls.size());
                    }
                    answers[i] = celebNames.get(incorrectAns);
                }
            }

            btnChooseCeleb1.setText(answers[0]);
            btnChooseCeleb2.setText(answers[1]);
            btnChooseCeleb3.setText(answers[2]);
            btnChooseCeleb4.setText(answers[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection = null;
            String result = "";

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.celebrity_layout);
        ivCelebPic = (ImageView) findViewById(R.id.ivCelebPic);
        btnChooseCeleb1 = (Button) findViewById(R.id.btnChooseCeleb1);
        btnChooseCeleb2 = (Button) findViewById(R.id.btnChooseCeleb2);
        btnChooseCeleb3 = (Button) findViewById(R.id.btnChooseCeleb3);
        btnChooseCeleb4 = (Button) findViewById(R.id.btnChooseCeleb4);

        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");
            Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splitResult[0]);
            while (matcher.find()){
                celebUrls.add(matcher.group(1));
            }

            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splitResult[0]);
            while(matcher.find()){
                celebNames.add(matcher.group(1));
            }

            createQuestion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}