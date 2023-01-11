package ipca.budget.movieapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable.Factory
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList
import android.app.AlertDialog
import android.widget.EditText



class MainActivity : AppCompatActivity() {

    val fireStoreDatabase = FirebaseFirestore.getInstance()
    var movies = arrayListOf<MovieandSeries>()
    val adapter = MovieAdapter()
    var inputName:String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ListView>(R.id.MovieList).adapter = adapter
        val searchview = findViewById<SearchView>(R.id.searchview)
        searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText!!.length < 3){

                    movies.clear()
                    adapter.notifyDataSetChanged()
                    return false
                }


                if(newText!!.length >= 3){
                    BackEnd.requestMovieAPI(lifecycleScope, newText){
                        movies = it
                        adapter.notifyDataSetChanged()
                    }
                    return false
                }
                return false
            }
        }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu1,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when(item.itemId){
            R.id.favourites -> {
                val intent = Intent(this@MainActivity, Favorites::class.java)
                startActivity(intent)
                movies.clear()
                val searchview = findViewById<SearchView>(R.id.searchview)
                searchview.setQuery("", false)
                return true
            }


            else -> {
                return false
            }
        }
    }

    inner class MovieAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return movies.size
        }

        override fun getItem(position: Int): Any {
            return movies[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, view: View?, p2: ViewGroup?): View {


            val rootView = layoutInflater.inflate(R.layout.row_moviesandseries,p2,false)

            val textViewMovieTitle = rootView.findViewById<TextView>(R.id.MovieTitle)
            val imageViewMovieImage = rootView.findViewById<ImageView>(R.id.MovieImage)
            val toggleButton =  rootView.findViewById<ToggleButton>(R.id.favbutton);
            val sendButton =  rootView.findViewById<ImageButton>(R.id.sendButton);

            textViewMovieTitle.text = movies[position].title

            toggleButton.setOnCheckedChangeListener { _, isChecked ->

                movies[position].isFavourite = isChecked
                val intent = Intent(this@MainActivity, Favorites::class.java)
                intent.putExtra("isfavourite", movies[position].isFavourite)

                if(movies[position].isFavourite){
                    var title : String? = movies[position].title
                    var imdb : String? = movies[position].url
                    var picture : String? = movies[position].urlToImage
                    lateinit var firebaseAuth: FirebaseAuth
                    firebaseAuth = FirebaseAuth.getInstance()
                    val user : String? = firebaseAuth.currentUser?.email
                   // var user  = intent.getStringExtra("user",userName.toString())

                    val favourites: MutableMap<String, Any> = HashMap()
                    favourites["Title"] = title.toString()
                    favourites["Imdb"] = imdb.toString()
                    favourites["Picture"] = picture.toString()
                    favourites["User"] = user.toString()

                    fireStoreDatabase.collection("Favourites").document(movies[position].title.toString())
                        .set(favourites)

                }else{
                    fireStoreDatabase.collection("Favourites")
                        .document(movies[position].title.toString())
                        .delete()
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                }


            }

            sendButton.setOnClickListener {
                alertDialog()

                var title : String? = movies[position].title
                var imdb : String? = movies[position].url
                var picture : String? = movies[position].urlToImage
                var reciever: String? = inputName
                // var user  = intent.getStringExtra("user",userName.toString())

                val favourites: MutableMap<String, Any> = HashMap()
                favourites["Title"] = title.toString()
                favourites["Imdb"] = imdb.toString()
                favourites["Picture"] = picture.toString()
                favourites["User"] = reciever.toString()

                fireStoreDatabase.collection("Recommendations").document(movies[position].title.toString())
                    .set(favourites)



            }



            //imageViewMovieImage.text = movies[position].title

            movies[position].urlToImage?.let {
                BackEnd.fetchImage(lifecycleScope, it){ bitmap ->
                    imageViewMovieImage.setImageBitmap(bitmap)
                }
            }

            rootView.setOnClickListener {
                Log.d("MainActivity", movies[position].title?:"" )
                val intent = Intent(this@MainActivity, MovieSeriesDetail::class.java)
                intent.putExtra("url", movies[position].url)
                //intent.putExtra("body", articles[position].content)

              // val intent = Intent(this@MainActivity, WebView::class.java)
              // intent.putExtra(EXTRA_ARTICLE_STRING, movies[position].toJSON().toString() )
                startActivity(intent)

            }


            return rootView
        }
    }

    fun alertDialog(){
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        builder.setTitle("Enter Your Name")

        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
             inputName = input.text.toString()
            // Do something with the input name, such as saving it to a variable or displaying it in a text view
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            // Handle cancel button click
        }

        val dialog = builder.create()
        dialog.show()
    }
    companion object {
        const val EXTRA_ARTICLE_STRING = "article_string"
    }

}