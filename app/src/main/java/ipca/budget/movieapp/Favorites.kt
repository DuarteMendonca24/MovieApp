
package ipca.budget.movieapp

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Favorites : AppCompatActivity() {
    val fireStoreDatabase = FirebaseFirestore.getInstance()

    val adapter = MovieAdapter()
    val favourites = arrayListOf<MovieandSeries>()
    lateinit var firebaseAuth: FirebaseAuth
    var inputName:String? = null
    private val Chanel_ID = "chanel_id_example_01"
    private val notificationId = 101



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        createnNotificationChannel()
        firebaseAuth = FirebaseAuth.getInstance()
        val user : String? = firebaseAuth.currentUser?.email
        fireStoreDatabase.collection("Users").document(user.toString()).collection("Favourites")
            .get()
            .addOnCompleteListener {
                val result: StringBuffer = StringBuffer()
                if (it.isSuccessful) {
                    for (document in it.result!!)
                        result.append(document.data.getValue("Title")).append("\n")
                            .append(document.data.getValue("Imdb")).append("\n")
                            .append(document.data.getValue("Picture")).append("\n")

                    var arrayResult = result.split("\n")
                    for (i in 0..arrayResult.size - 2 step 3) {
                        var movie = MovieandSeries()
                        movie.isFavourite = true
                        movie.title = arrayResult[i]
                        movie.url = arrayResult[i + 1]
                        movie.urlToImage = arrayResult[i + 2]

                        favourites.add(movie)


                    }
                }
                findViewById<ListView>(R.id.FavouritesList).adapter = adapter
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

            val textViewMovieTitle = rootView.findViewById<TextView>(R.id.MovieTitletwo)
            val imageViewMovieImage = rootView.findViewById<ImageView>(R.id.MovieImagetwo)
            val toggleButton = rootView.findViewById<ToggleButton>(R.id.favbuttontwo);
            val sendButton =  rootView.findViewById<ImageButton>(R.id.sendButtontwo);

            toggleButton.isChecked = true
            textViewMovieTitle.text = favourites[position].title

            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                favourites[position].isFavourite = isChecked
                firebaseAuth = FirebaseAuth.getInstance()
                val user : String? = firebaseAuth.currentUser?.email

                if(favourites[position].isFavourite == false) {
                    fireStoreDatabase.collection("Users").document(user.toString()).collection("Favourites")
                        .document(favourites[position].title.toString())
                        .delete()
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "DocumentSnapshot successfully deleted!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                    favourites.remove(favourites[position])
                    notifyDataSetChanged()
                    Toast.makeText(this@Favorites,"Movie removed from favourites",Toast.LENGTH_SHORT).show()
                }


            }

            sendButton.setOnClickListener {


                val builder = AlertDialog.Builder(this@Favorites)
                val input = EditText(this@Favorites)
                builder.setTitle("Enter the username of the person you want to send")

                builder.setView(input)

                builder.setPositiveButton("OK") { dialog, which ->
                    inputName = input.text.toString()
                    var title : String? = favourites[position].title
                    var imdb : String? = favourites[position].url
                    var picture : String? = favourites[position].urlToImage

                    // var user  = intent.getStringExtra("user",userName.toString())

                    val hashfavourites: MutableMap<String, Any> = HashMap()
                    hashfavourites["Title"] = title.toString()
                    hashfavourites["Imdb"] = imdb.toString()
                    hashfavourites["Picture"] = picture.toString()
                    firebaseAuth = FirebaseAuth.getInstance()
                    val userName: String? = firebaseAuth?.currentUser?.email
                    if(inputName.toString() != userName){
                        sendNotification(inputName.toString())
                    }
                    else{
                        Toast.makeText(this@Favorites,"Recommendation Not Sent : Invalid User",Toast.LENGTH_SHORT).show()
                    }


                    fireStoreDatabase.collection("Users").document(inputName.toString()).collection("Recommendations")
                        .document(favourites[position].title.toString())
                        .set(hashfavourites)


                    // Do something with the input name, such as saving it to a variable or displaying it in a text view
                }

                builder.setNegativeButton("Cancel") { dialog, which ->
                    // Handle cancel button click
                }

                val dialog = builder.create()
                dialog.show()

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

}
