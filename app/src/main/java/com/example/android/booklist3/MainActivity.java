package com.example.android.booklist3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private EditText userInput;
    private Button SearchBtn;
    private String userBookSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = (EditText) findViewById(R.id.editText);
        SearchBtn = (Button) findViewById(R.id.searchBtn);

        //"userBookSearch" will contain the user's search entry, that will be added to the final URL
        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userBookSearch = userInput.getText().toString().replace(" ", "+");

                if (userBookSearch.trim().length() <= 0 || userBookSearch.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "No Search Enteries", Toast.LENGTH_LONG).show();
                } else {
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute();
                }
            }
        });
    }

    /**
     * Update the screen to display information from the given {@link Event}.
     */
    private void updateUi(Event newBook) {
        // Display the title in the UI
        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(newBook.title);

        // Display the author in the UI
        TextView authorTextView = (TextView) findViewById(R.id.authors);
        authorTextView.setText(newBook.authors);
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first book in the response.
     */
    private class BookAsyncTask extends AsyncTask<URL, Void, Event> {

        @Override
        protected Event doInBackground(URL... urls) {
            // Create URL object

            //URL url = createUrl(USGS_REQUEST_URL);
            URL url = null;
            url = createUrl(userBookSearch.trim());

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            Event book = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link bookAsyncTask}
            return book;
        }

        /**
         * Update the screen with the given book (which was the result of the
         * {@link BookAsyncTask}).
         */
        @Override
        protected void onPostExecute(Event book) {
            if (book == null) {
                return;
            }

            updateUi(book);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {

            String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=";
            String completeUrl = baseUrl + stringUrl.replace(" ", "%20");
            URL url = null;
            try {
                url = new URL(completeUrl);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

                //if connenction was succesful (200) read the input stream and parse the response
                /** if (urlConnection.getResponseCode() == 200){
                 inputStream = urlConnection.getInputStream();
                 jsonResponse = readFromStream(inputStream);
                 } else {
                 Log.e(LOG_TAG, "error response code: " + urlConnection.getResponseCode());
                 }
                 */

            } catch (IOException e) {
                // TODO: Handle the exception
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link Event} object by parsing out information
         * about the first book from the input bookJSON string.
         */
        private Event extractFeatureFromJson(String bookJSON) {
            try {
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                JSONArray featureArray = baseJsonResponse.getJSONArray("items");

                // If there are results in the features array
                if (featureArray.length() > 0) {
                    // Extract out the first feature which is a book
                    JSONObject firstFeature = featureArray.getJSONObject(0);
                    JSONObject properties = firstFeature.getJSONObject("volumeInfo");

                    // Extract the title
                    String title = properties.getString("title");

                    //csaba: creates the "authors" String
                    String authors = "";

                    //csaba: if there is more than one authors, they will be listed with a comma
                    JSONArray authorJson = properties.getJSONArray("authors");
                    if (authorJson.length() > 0) {
                        for (int j = 0; j < authorJson.length(); j++) {
                            authors += authorJson.optString(j) + ", ";
                        }
                    }

                    // Create a new {@link Event} object
                    return new Event(title, authors);
                }
            } catch (JSONException e) {
                return new Event("no internet, try to connect", "");
            }
            return null;
        }
    }
}
