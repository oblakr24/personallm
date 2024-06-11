package feature.main

import di.VMContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.RouteNavigator

@Inject
class MainComponent(
    private val nav: RouteNavigator,
    @Assisted private val pageNavigator: PageNavigation,
    @Assisted private val vmContext: VMContext,
): VMContext by vmContext, RouteNavigator by nav, PageNavigation by pageNavigator {

    fun onAction(action: MainAction) {

    }
}
