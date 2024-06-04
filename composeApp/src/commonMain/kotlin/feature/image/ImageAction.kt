package feature.image

import feature.camera.SharedImage

sealed interface ImageAction {
    data class OnImageResultReceived(val image: SharedImage?) : ImageAction
}