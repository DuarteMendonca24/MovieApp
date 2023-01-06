package ipca.budget.movieapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class Wishlist : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu1,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when(item.itemId){
            R.id.search -> {
                val intent = Intent(this@Wishlist, MainActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.favourites -> {
                val intent = Intent(this@Wishlist, Favorites::class.java)
                startActivity(intent)
                return true
            }

            else -> {
                return false
            }
        }
    }
}