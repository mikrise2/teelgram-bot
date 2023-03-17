import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.LatLng
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitLocationMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first


suspend fun BehaviourContext.shareLocation() {
    onCommand("share_location") {
        val user = createIfNotExist(it.chat)
        val location = waitLocationMessage(
            getSendText(
                it.chat,
                "share_location"
            )
        ).filter { location -> location.chat == it.chat }.first().content.location
        users.entries.filter { entry -> entry.value.username in user.friends }.forEach { entry ->
            sendMessageBundled(
                entry.key,
                entry.value.language.locale,
                "location_notification",
                user.preferredName,
                getAddress(location.latitude, location.longitude)
            )
        }
        sendMessageBundled(it.chat, "location_delivered")
    }
}

fun getAddress(latitude: Double, longitude: Double): String {
    val context = GeoApiContext.Builder()
        .apiKey(System.getenv("GOOGLE_MAPS_API_KEY"))
        .build()
    val results = GeocodingApi.reverseGeocode(context, LatLng(latitude, longitude)).await()
    context.shutdown()
    return results[0].formattedAddress
}