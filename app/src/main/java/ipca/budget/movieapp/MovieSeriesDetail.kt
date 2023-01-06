package ipca.budget.movieapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.webkit.WebView
import org.json.JSONObject

class MovieSeriesDetail : AppCompatActivity() {

   var movieImdb : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_series_detail)

        movieImdb = intent.getStringExtra("url")

        movieImdb?.let {
            findViewById<WebView>(R.id.webview).loadUrl(it)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu1,menu)
        return super.onCreateOptionsMenu(menu)
    }
}