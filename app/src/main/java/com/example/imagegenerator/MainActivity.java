package com.example.imagegenerator;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText inputText;
    MaterialButton generateBtn;
    ProgressBar progressBar;
    ImageView imageView;

    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

            // Initializes all the layout elements to be programatically handled.
        inputText = findViewById(R.id.input_text);
        generateBtn = findViewById(R.id.generate_btn);
        progressBar = findViewById(R.id.progress_bar);
        imageView = findViewById(R.id.image_view);

            // Button saves the current string written in the EditText portion to text

        generateBtn.setOnClickListener((v) -> {
            String text = inputText.getText().toString().trim();
            if (text.isEmpty()) {
                inputText.setError("Text cannot be empty.");
                return;
            }
            callAPI(text); // Generates an API call taking the user inputted text
                            // as an argument.

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets; // Applies the constraints for the layout.
        });
    }

    void callAPI(String text) {
        //call method
        setInProgress(true);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt", text);
            jsonBody.put("size", "256x256");
            // Filters out only the required outputs from the API
        } catch (Exception e) {
            e.printStackTrace(); // Whenever an exception occurs, The program will output
                                // The error in LogCat.
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization", "Bearer sk-proj-A1DAFwqVUO5kQnqtVs3eT3BlbkFJxZsimE7c1Fb2XK2ReV50")
                .post(requestBody)
                .build();
        // Builds the API call, and sets the endpoint and applies the API key as a header.

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(getApplicationContext(), "Failed to generate image", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String imageURL = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
                    loadImage(imageURL);
                    // Updates the ImageView with the image generated.
                    setInProgress(false);
                    // Changes the current indicator of generation.
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    void setInProgress(boolean inProgress){
        runOnUiThread(()->{
            if(inProgress){
                progressBar.setVisibility(ImageView.VISIBLE);
                generateBtn.setVisibility(ImageView.GONE);
                // When generating, the progress bar becomes visible, and hides the
                // Image View
            } else{
                progressBar.setVisibility(ImageView.GONE);
                generateBtn.setVisibility(ImageView.VISIBLE);
                // After generation completes, progress bar becomes invisible
                // and Image View reloads
            }
        });

    }

    void loadImage(String url){
        runOnUiThread(()->{
            Picasso.get().load(url).into(imageView);
            // updates the imageview using the Picasso libraries to convert URL
            // to physical image.
        });
    }

}