/** Name : Khiem Dinh Phi
 *  ID : 111667279
 *  The Weather App Asynchronously Displays the Weather of any city the user types into an EditText
 *  including the temperature, current weather condition, wind, pressure, sunrise, sunset as well as
 *  using the Picasso Package to get the right image display for a weather condition.
 */

package com.example.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class MainActivity extends AppCompatActivity {

    private String city = "stony brook";
    private static final String API_KEY = "6db7b2c101f58376711f95536baaaf6c";
    private static final String DEFAULT_CITY = "stony brook";

    TextView addressTxt, updated_atTxt, statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt,
            sunsetTxt, windTxt, pressureTxt, humidityTxt;
    EditText userInput;

    ImageView weatherIcon;

    /** The method executes a GET request to the OpenWeather API in order to receive
     * a JSON String representation of the data for the weather at a user specified city.
     *
     * @param targetURL - URL to get API Data
     * @return - String representation of the JSON Object returned from the API
     */
    public static String executeGet(String targetURL){
        URL url;
        HttpURLConnection connection = null;
        try{
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            InputStream is;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK){
                is = connection.getErrorStream();
            }else{
                is = connection.getInputStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null){
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally{
            if (connection != null){
                connection.disconnect();
            }
        }

    }

    /** The method takes a user String describes a city and acts as a helper method to
     * for other methods to interact with the executeGet method above.
     *
     * @param city - the String representing a city the user wants to see the weather of.
     * @return - JSON String response from the OpenWeather API.
     */
    public String getJSONString(String city){
        String response = executeGet("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + API_KEY);
        return response;
    }

    /** The method takes a String representation of a JSON object received from the OpenWeather API
     * and populate the view based on the value of different fields in the JSON object.
     *
     * @param result - the resulting String representation of a JSON object sent by the OpenWeather API
     */
    public void populateView(String result){
        try{
            JSONObject jsonObj = new JSONObject(result);
            JSONObject main = jsonObj.getJSONObject("main");
            JSONObject sys = jsonObj.getJSONObject("sys");
            JSONObject wind = jsonObj.getJSONObject("wind");
            JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

            Long updatedAt = jsonObj.getLong("dt");
            String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(new Date(updatedAt * 1000));
            String temp = main.getString("temp") + "°C";
            String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
            String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
            String pressure = main.getString("pressure");
            String humidity = main.getString("humidity");

            Long sunrise = sys.getLong("sunrise");
            Long sunset = sys.getLong("sunset");
            String windSpeed = wind.getString("speed");
            String weatherDescription = weather.getString("description");

            String address = jsonObj.getString("name") + ", " + sys.getString("country");

            // Getting The Weather Icon
            String iconCode = weather.getString("icon");
            String iconURL = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

            Picasso.get().load(iconURL).into(weatherIcon);

            // Populating the data into our view
            addressTxt.setText(address);
            updated_atTxt.setText(updatedAtText);
            statusTxt.setText(weatherDescription.toUpperCase());
            tempTxt.setText(temp);
            temp_minTxt.setText(tempMin);
            temp_maxTxt.setText(tempMax);
            sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(new Date(sunrise * 1000)));
            sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(new Date(sunset * 1000)));
            windTxt.setText(windSpeed);
            pressureTxt.setText(pressure);
            humidityTxt.setText(humidity);

            // Views populated hide the loader + show the main container
            findViewById(R.id.loader).setVisibility(View.GONE);
            findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

            // Reset The textview to empty string
            userInput.setText("");

        }catch (JSONException e){

            Toast.makeText(this, "The City You Specified Does Not Exist", Toast.LENGTH_SHORT).show();
            findViewById(R.id.loader).setVisibility(View.GONE);
            findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

        }
    }

    /**The onCreate method creates the view of the Weather application whilst also calling an Async Task in order
     * to populate the view by retrieving data from the OpenWeather API.
     *
     * @param savedInstanceState - the previously saved instant of the Weather Application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressTxt = findViewById(R.id.address);
        userInput = findViewById(R.id.user_input);
        updated_atTxt = findViewById(R.id.updated_at);
        statusTxt = findViewById(R.id.status);
        tempTxt = findViewById(R.id.temp);
        temp_minTxt = findViewById(R.id.temp_min);
        temp_maxTxt = findViewById(R.id.temp_max);
        sunriseTxt = findViewById(R.id.sunrise);
        sunsetTxt = findViewById(R.id.sunset);
        windTxt = findViewById(R.id.wind);
        pressureTxt = findViewById(R.id.pressure);
        humidityTxt = findViewById(R.id.humidity);
        weatherIcon = findViewById(R.id.status_picture);

        new weatherTask().execute();
    }

    /** The weatherTask class is a class that extends AsyncTask to allow for Async action
     *  that would execute the GET request and the populate the view according to the results from the GET request
     */
    protected class weatherTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing the Progress Bar
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args){
            String response = getJSONString(city);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
           populateView(result);
        }
    }

    /**The method is called when the user clicks the see weather button, the method is simply a helper
     * method that starts another Async activity to fetch new data based on a new input string given by the user.
     *
     * @param view - current view
     */
    public void updateView(View view){
        city = userInput.getText().toString(); // update before calling the weather task
        new weatherTask().execute();
    }


}