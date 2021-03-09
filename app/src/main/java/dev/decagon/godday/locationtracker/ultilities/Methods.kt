package dev.decagon.godday.locationtracker.ultilities

import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dev.decagon.godday.locationtracker.R

object Methods {

    /**
     * This method is used to bind image to image views. it uses Glide library.
     */
    internal fun bindImage(imgUrl: String?, imgView: ImageView) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imgView.context)
                .load(imgUri).apply(
                    RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image))
                .into(imgView)
        }
    }

    /**
     * This method is used in extracting the index out of a url string
     * it returns the number before the last / in the url
     */
    internal fun getIndexFromUrl(url: String): String {
        val index = url.split("/")
        val int = index.lastIndex
        return index.elementAt(int - 1)
    }

}