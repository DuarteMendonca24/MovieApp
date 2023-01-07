package ipca.budget.movieapp

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Favorites : AppCompatActivity() {
    val fireStoreDatabase = FirebaseFirestore.getInstance()

    val adapter = MovieAdapter()
    val favourites = arrayListOf<MovieandSeries>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        fireStoreDatabase.collection("Favourites")
            .get()
            .addOnCompleteListener {
                val result: StringBuffer = StringBuffer()
                if (it.isSuccessful) {
                    for (document in it.result!!)
                        result.append(document.data.getValue("Title")).append("\n")
                            .append(document.data.getValue("Imdb")).append("\n")
                            .append(document.data.getValue("Picture")).append("\n")
                            .append(document.data.getValue("User")).append("\n")

                    var arrayResult = result.split("\n")
                    for(i in 0..arrayResult.size - 2 step 4) {
                        var movie = MovieandSeries()
                        movie.title = arrayResult[i]
                        movie.url = arrayResult[i + 1]
                        movie.urlToImage = arrayResult[i + 2]
                        movie.user = arrayResult[i + 3]
                        lateinit var firebaseAuth: FirebaseAuth
                        firebaseAuth = FirebaseAuth.getInstance()
                        if(movie.user == firebaseAuth.currentUser?.email){
                            favourites.add(movie)
                        }

                    }
                }
                findViewById<ListView>(R.id.FavouritesList).adapter = adapter
            }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu1, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.search -> {
                val intent = Intent(this@Favorites, MainActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.wishlist -> {
                val intent = Intent(this@Favorites, Wishlist::class.java)
                startActivity(intent)
                return true
            }

            else -> {
                return false
            }
        }
    }



    inner class MovieAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return favourites.size
        }

        override fun getItem(position: Int): Any {
            return favourites[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, view: View?, p2: ViewGroup?): View {
            val rootView = layoutInflater.inflate(R.layout.row_moviesandseries, p2, false)

            val textViewMovieTitle = rootView.findViewById<TextView>(R.id.MovieTitle)
            val imageViewMovieImage = rootView.findViewById<ImageView>(R.id.MovieImage)
            val toggleButton = rootView.findViewById<ToggleButton>(R.id.favbutton);

            textViewMovieTitle.text = favourites[position].title
            favourites[position].isFavourite = intent.getBooleanExtra("isfavourite", true)

            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                favourites[position].isFavourite = isChecked


                 fireStoreDatabase.collection("Favourites").document(favourites[position].title.toString())
                    .delete()
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                     favourites.remove(favourites[position])
                     notifyDataSetChanged()



            }


            //imageViewMovieImage.text = movies[position].title

            favourites[position].urlToImage?.let {
                BackEnd.fetchImage(lifecycleScope, it) { bitmap ->
                    imageViewMovieImage.setImageBitmap(bitmap)
                }
            }

            rootView.setOnClickListener {
                Log.d("MainActivity", favourites[position].title ?: "")
                val intent = Intent(this@Favorites, MovieSeriesDetail::class.java)
                intent.putExtra("url", favourites[position].url)
                //intent.putExtra("body", articles[position].content)

                // val intent = Intent(this@MainActivity, WebView::class.java)
                // intent.putExtra(EXTRA_ARTICLE_STRING, movies[position].toJSON().toString() )
                startActivity(intent)

            }


            return rootView
        }

    }
}