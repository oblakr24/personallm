package feature.image

import feature.sharedimage.SharedImage

sealed interface ImageAction {
    data class OnImageResultReceived(val image: SharedImage?) : ImageAction
}