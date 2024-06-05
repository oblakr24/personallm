package feature.samplerequest

sealed interface SampleRequestAction {
    data object ExecuteRequestClicked: SampleRequestAction
}