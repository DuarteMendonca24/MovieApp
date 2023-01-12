package ipca.budget.movieapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
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
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MainActivity : AppCompatActivity() {

    val fireStoreDatabase = FirebaseFirestore.getInstance()
    var movies = arrayListOf<MovieandSeries>()
    val adapter = MovieAdapter()
    var inputName:String? = null
    private val Chanel_ID = "chanel_id_example_01"
    private val notificationId = 101
    var firebaseAuth: FirebaseAuth? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createnNotificationChannel()

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

            R.id.recommended -> {
                val intent = Intent(this@MainActivity, Recommendation::class.java)
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

            val textViewMovieTitle = rootView.findViewById<TextView>(R.id.MovieTitletwo)
            val imageViewMovieImage = rootView.findViewById<ImageView>(R.id.MovieImagetwo)
            val toggleButton =  rootView.findViewById<ToggleButton>(R.id.favbuttontwo);
            val sendButton =  rootView.findViewById<ImageButton>(R.id.sendButtontwo);

            textViewMovieTitle.text = movies[position].title

            toggleButton.setOnCheckedChangeListener { _, isChecked ->

                movies[position].isFavourite = isChecked
                val intent = Intent(this@MainActivity, Favorites::class.java)
                intent.putExtra("isfavourite", movies[position].isFavourite)

                var title : String? = movies[position].title
                var imdb : String? = movies[position].url
                var picture : String? = movies[position].urlToImage
                firebaseAuth = FirebaseAuth.getInstance()


                val user : String? = firebaseAuth?.currentUser?.email

                if(movies[position].isFavourite){

                    // var user  = intent.getStringExtra("user",userName.toString())

                    val favourites: MutableMap<String, Any> = HashMap()
                    favourites["Title"] = title.toString()
                    favourites["Imdb"] = imdb.toString()
                    favourites["Picture"] = picture.toString()
                    Toast.makeText(this@MainActivity,"Movie added to favourites",Toast.LENGTH_SHORT).show()

                    fireStoreDatabase.collection("Users").document(user.toString()).collection("Favourites").document(title.toString()).set(favourites)
                    //fireStoreDatabase.collection("Users").document(user.toString()).collection("Recommendations").document(title.toString()).set(favourites)
                }else{
                    fireStoreDatabase.collection("Users").document(user.toString()).collection("Favourites")
                        .document(movies[position].title.toString())
                        .delete()
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

                         Toast.makeText(this@MainActivity,"Movie removed from favourites",Toast.LENGTH_SHORT).show()
                }


            }

            sendButton.setOnClickListener {


                val builder = AlertDialog.Builder(this@MainActivity)
                val input = EditText(this@MainActivity)
                builder.setTitle("Enter the username of the person you want to send")

                builder.setView(input)

                builder.setPositiveButton("OK") { dialog, which ->
                    inputName = input.text.toString()
                    var title : String? = movies[position].title
                    var imdb : String? = movies[position].url
                    var picture : String? = movies[position].urlToImage

                    // var user  = intent.getStringExtra("user",userName.toString())

                    val favourites: MutableMap<String, Any> = HashMap()
                    favourites["Title"] = title.toString()
                    favourites["Imdb"] = imdb.toString()
                    favourites["Picture"] = picture.toString()
                    firebaseAuth = FirebaseAuth.getInstance()
                    val userName: String? = firebaseAuth?.currentUser?.email
                    if(inputName.toString() != userName){
                        sendNotification(inputName.toString())
                    }
                    else{
                        Toast.makeText(this@MainActivity,"Recommendation Not Sent : Invalid User",Toast.LENGTH_SHORT).show()
                   }


                   // fireStoreDatabase.collection("Users")
                   //     .get()
                   //     .addOnCompleteListener{
                   //
                   //         val result:StringBuffer = StringBuffer()
                   //         if(it.isSuccessful){
                   //             for(document in it.result!!){
                   //                 if(document.id == inputName){
                   //
                   //                 }
                   //             }
                   //         }
                   //     }









                    fireStoreDatabase.collection("Users").document(inputName.toString()).collection("Recommendations")
                        .document(movies[position].title.toString())
                        .set(favourites)



                    // Do something with the input name, such as saving it to a variable or displaying it in a text view
                }

                builder.setNegativeButton("Cancel") { dialog, which ->
                    // Handle cancel button click
                }

                val dialog = builder.create()
                dialog.show()

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
    private fun createnNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel: NotificationChannel = NotificationChannel(Chanel_ID,name,importance).apply{
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }


    }
    private fun sendNotification(reciever:String){
        val builder = NotificationCompat.Builder(this,Chanel_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recommendation Sent")
            .setContentText("Your recommendation was successfully sent to $reciever")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId , builder.build())
        }
    }

    companion object {
        const val EXTRA_ARTICLE_STRING = "article_string"
    }

}