package ipca.budget.movieapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

object BackEnd {
    private val client = OkHttpClient()

    fun requestMovieAPI(scope: CoroutineScope, term: String, callback: (ArrayList<MovieandSeries>)->Unit ) {
        scope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://utelly-tv-shows-and-movies-availability-v1.p.rapidapi.com/lookup?term=$term&country=uk")
                .get()
                .addHeader("X-RapidAPI-Key", "d00f3fb5edmsh16e0248d8afd00bp1f940ajsn4aec02b20ad7")
                .addHeader(
                    "X-RapidAPI-Host",
                    "utelly-tv-shows-and-movies-availability-v1.p.rapidapi.com"
                )
                .build()


            val response = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val result = response.body!!.string()
                // println(result)

                val jsonObject = JSONObject(result)
                val jsonObjectStatus = jsonObject.getInt("status_code")
                if (jsonObjectStatus == 200) {
                    val movies = arrayListOf<MovieandSeries>()
                    val jsonArrayArticles = jsonObject.getJSONArray("results")
                    for (index in 0 until jsonArrayArticles.length()) {
                       //val imdburl = jsonArrayArticles.getJSONObject(index).getJSONObject("external_ids").getJSONObject("imdb").getString("url")
                       //println(imdburl)
                        val jsonObjectMovie = jsonArrayArticles.getJSONObject(index)
                        val movie = MovieandSeries.fromJSON(jsonObjectMovie)
                        movies.add(movie)
                    }
                   scope.launch(Dispatchers.Main) {
                       callback(movies)
                   }
                }

            }
        }

    }

    fun fetchImage(scope: CoroutineScope, url:String, callback:(Bitmap)->Unit) {
        scope.launch (Dispatchers.IO){
            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request).execute().use { response ->
                val inputStream = response.body?.byteStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                scope.launch (Dispatchers.Main){
                    callback(bitmap)
                }
            }
        }
    }

}