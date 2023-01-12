package ipca.budget.movieapp


import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class MovieandSeries {
    var title       : String? = null
    var url         : String? = null
    var urlToImage  : String? = null
    var isFavourite = false
    //var user  : String? = null

    fun toJSON() : JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("name"      , title      )
        jsonObject.put("url", url )
        jsonObject.put("urlToImage" , urlToImage )


        return jsonObject
    }

    companion object {
        fun fromJSON(jsonObject: JSONObject) : MovieandSeries {
            return MovieandSeries().apply {
                title       = jsonObject.getString("name"      )
                url         = jsonObject.getJSONObject("external_ids").getJSONObject("imdb").getString("url")
                urlToImage  = jsonObject.getString("picture")
            }
        }
    }
}