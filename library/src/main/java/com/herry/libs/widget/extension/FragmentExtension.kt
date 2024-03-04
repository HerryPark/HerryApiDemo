@file:Suppress("unused")

package com.herry.libs.widget.extension

import android.os.Bundle
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.herry.libs.app.nav.BottomNavHostFragment
import com.herry.libs.app.nav.NavBundleUtil

fun Fragment.getNavCurrentDestinationID(): Int = findNavController().currentDestination?.id ?: 0

fun Fragment.hasNavDestinationID(@IdRes id: Int): Boolean {
    return findNavController().hasNavDestinationID(id)
}

private fun NavController.hasNavDestinationID(@IdRes id: Int): Boolean {
    return this.currentBackStack.value.firstOrNull { it.destination.id == id } != null
// for Navigation 2.5.3 or lower
//    return this.backQueue.firstOrNull { backStack -> backStack.destination.id == id } != null
}

fun Fragment.addNestedNavHostFragment(@IdRes containerViewId: Int, navHostFragment: NavHostFragment?, tag: String? = null, listener: ((requestKey: String, bundle: Bundle) -> Unit)? = null): Boolean {
    navHostFragment ?: return false
    childFragmentManager.beginTransaction()
        .replace(containerViewId, navHostFragment, tag)
        .setPrimaryNavigationFragment(navHostFragment) // this is the equivalent to app:defaultNavHost="true"
        .commit()

    if (listener != null) {
        childFragmentManager.setFragmentResultListener(navHostFragment.id.toString(), this, listener)
    }

    return true
}

fun Fragment.addNestedNavHostFragment(
    @IdRes containerViewId: Int,
    @NavigationRes graphResId: Int,
    startDestinationArgs: Bundle? = null,
    tag: String? = null,
    listener: ((requestKey: String, bundle: Bundle) -> Unit)? = null
): NavHostFragment {
    val navHostFragment = NavHostFragment.create(graphResId, startDestinationArgs)

    addNestedNavHostFragment(containerViewId, navHostFragment, tag, listener)

    return navHostFragment
}

fun NavHostFragment.setFragmentNotifyListener(listener: ((requestKey: String, bundle: Bundle) -> Unit)) {
    childFragmentManager.setFragmentResultListener(id.toString(), this, listener)
}

fun Fragment.findParentNavHostFragment(): NavHostFragment? {
    var parentFragment: Fragment? = this.parentFragment
    while (parentFragment != null) {
        if (parentFragment is NavHostFragment) {
            return parentFragment
        }

        parentFragment = parentFragment.parentFragment
    }

    return null
}

fun Fragment.findNestedNavHostFragment(@IdRes id: Int): NavHostFragment? {
    return childFragmentManager.findFragmentById(id) as? NavHostFragment
}

fun Fragment.isNestedNavHostFragment(): Boolean {
    if (this is NavHostFragment) {
        return this.findParentNavHostFragment() != null
    }

    return false
}

fun Fragment.isNestedChildFragment(): Boolean {
    val parentFragment = findParentNavHostFragment()
    return parentFragment != null && parentFragment.findParentNavHostFragment() != null
}

fun Fragment.isDialogFragment(): Boolean {
    return (this as? DialogFragment)?.showsDialog ?: false
}

fun NavHostFragment.getCurrentFragment(): Fragment? {
    return childFragmentManager.primaryNavigationFragment
}

/**
 * Pop child fragment until destinationId
 */
fun Fragment.popTo(@IdRes destinationId: Int, bundle: Bundle = Bundle(), inclusive: Boolean = true) {
    findNavController().run {
        popBackStack(destinationId, inclusive)
        notifyTo(destinationId, bundle)
    }
}

/**
 * Pop all child fragment and set to the NavHostFragment
 */
fun Fragment.popToNavHost(bundle: Bundle? = null) {
    val navHostFragment = if (this is NavHostFragment) {
        this
    } else {
        this.findParentNavHostFragment()
    } ?: return

    val targetDestinationId = navHostFragment.findNavController().graph.startDestinationId
    val currentDestinationId = getNavCurrentDestinationID()

    this.popTo(
        destinationId = targetDestinationId,
        bundle = (bundle ?: Bundle().apply { NavBundleUtil.createNavigationBundle(false) }).apply {
            NavBundleUtil.addFromNavigationId(this, currentDestinationId)
        },
        inclusive = false
    )
}

fun Fragment.isNavigateStartDestination(): Boolean {
    val navHostFragment = if (this is NavHostFragment) {
        this
    } else {
        this.findParentNavHostFragment()
    } ?: return false

    val targetDestinationId = navHostFragment.findNavController().graph.startDestinationId
    val currentDestinationId = getNavCurrentDestinationID()

    return targetDestinationId == currentDestinationId
}

private fun Fragment.notifyTo(@IdRes destinationId: Int, bundle: Bundle) {
    setFragmentResult(destinationId.toString(), bundle)
}

fun Fragment.setFragmentResult(requestKey: String, result: Bundle) {
    this.parentFragmentManager.setFragmentResult(requestKey, result)
}

fun Fragment.setFragmentResultListener(requestKey: String, listener: ((resultKey: String, bundle: Bundle) -> Unit)) {
    this.parentFragmentManager.setFragmentResultListener(requestKey, this, listener)
}

/**
 * Sends data to NavHostFragment of this fragment
 */
fun Fragment.notifyToNavHost(bundle: Bundle) {
    val navHostFragment = this.findParentNavHostFragment()

    navHostFragment?.childFragmentManager?.setFragmentResult(
        navHostFragment.id.toString(), bundle
    )
}

/**
 * Sends data to ParentNavHostFragment of the NavHostFragment of this fragment
 */
fun Fragment.notifyToParentNavHost(bundle: Bundle) {
    val navHostFragment = this.findParentNavHostFragment()

    navHostFragment?.parentFragment?.childFragmentManager?.setFragmentResult(
        navHostFragment.id.toString(), bundle
    )
}

/**
 * Sends data to current fragment from NavHostFragment
 */
fun NavHostFragment.notifyToCurrent(bundle: Bundle) {
    val currentDestinationId = findNavController().currentBackStackEntry?.destination?.id
    if (currentDestinationId != null) {
        notifyTo(destinationId = currentDestinationId, bundle = bundle)
    }
}

/**
 * Changes screen to
 */
fun Fragment.navigateTo(navController: NavController? = null, @IdRes destinationId: Int, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    navigateTo(navController = navController, destinationId = destinationId, args = null, singleInstance = singleInstance, onNavigated = onNavigated)
}

fun Fragment.navigateTo(navController: NavController? = null, @IdRes destinationId: Int, args: Bundle? = null, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    navigateTo(navController = navController, destinationId = destinationId, args = args, navOptions = null, singleInstance = singleInstance, onNavigated = onNavigated)
}

fun Fragment.navigateTo(navController: NavController? = null, @IdRes destinationId: Int, args: Bundle? = null, navOptions: NavOptions? = null, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    navigateTo(navController = navController, destinationId = destinationId, args = args, navOptions = navOptions, navigatorExtras = null, singleInstance = singleInstance, onNavigated = onNavigated)
}

fun Fragment.navigateTo(navController: NavController? = null, @IdRes destinationId: Int, args: Bundle? = null, navOptions: NavOptions? = null, navigatorExtras: Navigator.Extras? = null, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    navigateTo(navController = navController, destinationId = destinationId, args = args, navOptions = navOptions, navigatorExtras = navigatorExtras, isCheckParent = true, singleInstance = singleInstance, onNavigated = onNavigated)
}

/**
 * Navigate via the given [NavDirections]
 *
 * @param directions directions that describe this navigation operation
 */
fun Fragment.navigateTo(navController: NavController? = null, directions: NavDirections, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    navigateTo(navController = navController, destinationId = directions.actionId, args = directions.arguments, singleInstance = singleInstance, onNavigated = onNavigated)
}

/**
 * Navigate via the given [NavDirections]
 *
 * @param directions directions that describe this navigation operation
 * @param navOptions special options for this navigation operation
 */
fun Fragment.navigateTo(navController: NavController? = null, directions: NavDirections, navOptions: NavOptions?, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    navigateTo(navController = navController, destinationId = directions.actionId, args = directions.arguments, navOptions = navOptions, singleInstance = singleInstance, onNavigated = onNavigated)
}

/**
 * Navigate via the given [NavDirections]
 *
 * @param directions directions that describe this navigation operation
 * @param navigatorExtras extras to pass to the [Navigator]
 */
fun Fragment.navigateTo(navController: NavController? = null, directions: NavDirections, navigatorExtras: Navigator.Extras, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    navigateTo(navController = navController, destinationId = directions.actionId,args = directions.arguments, navOptions = null, navigatorExtras = navigatorExtras, singleInstance = singleInstance, onNavigated = onNavigated)
}

private fun onNavigated(fragment: Fragment, onNavigated: ((navigatedFragment: Fragment) -> Unit)) {
    val navHostFragment = if (fragment is NavHostFragment) fragment else fragment.getNavHostFragment()
    val fragmentManger = navHostFragment?.childFragmentManager
    fragmentManger?.registerFragmentLifecycleCallbacks(object: FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            fragmentManger.unregisterFragmentLifecycleCallbacks(this)
            onNavigated(f)
        }
    }, false)
}

/**
 * Navigate to a destination from the current navigation graph. This supports both navigating
 * via an {@link NavDestination#getAction(int) action} and directly navigating to a destination.
 *
 * @param navController navigation controller for destination nav id
 * @param destinationId an {@link NavDestination#getAction(int) action} id or a destination id to
 *              navigate to
 * @param args arguments to pass to the destination
 * @param navOptions special options for this navigation operation
 * @param navigatorExtras extras to pass to the Navigator
 * @param isCheckParent true - checked destination id at parent navigation controller when destination nav id is not existed at current navigation controller
 * @param singleInstance true - If a destination fragment exists, it is not called.
 */
fun Fragment.navigateTo(navController: NavController? = null, @IdRes destinationId: Int, args: Bundle?, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?, isCheckParent: Boolean = true, singleInstance: Boolean = false, onNavigated: ((navigatedFragment: Fragment) -> Unit)? = null) {
    val findNavController = navController ?: findNavController()
    try {
        if (singleInstance && findNavController.hasNavDestinationID(destinationId)) {
            return
        }
        findNavController.navigate(destinationId, args, navOptions, navigatorExtras)
        if (onNavigated != null) {
            onNavigated(this, onNavigated)
        }
    } catch (ex: IllegalArgumentException) {
        if (isCheckParent) {
            try {
                val navControllers = findAllNavControllers()
                navControllers.forEach { parentNavController ->
                    try {
                        navigateTo(parentNavController, destinationId, args, navOptions, navigatorExtras, isCheckParent = false, singleInstance = singleInstance, onNavigated = onNavigated)
                        return
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
            }
        } else {
            throw ex
        }
    }
}

// find all nav controllers from closest
fun Fragment.findAllNavControllers(): ArrayList<NavController> {
    val navControllers = arrayListOf<NavController>()
    var parent = parentFragment
    while (parent != null) {
        if (parent is NavHostFragment) {
            navControllers.add(parent.navController)
        }
        parent = parent.parentFragment
    }
    return navControllers
}

// find one nav controller by fragment id
fun Fragment.findNavControllerById(@IdRes id: Int): NavController {
    var parent = parentFragment
    while (parent != null) {
        if (parent is NavHostFragment && parent.id == id) {
            return parent.navController
        }
        parent = parent.parentFragment
    }
    throw RuntimeException("NavController with specified id not found")
}

fun NavHostFragment.getFragmentByViewID(): Fragment? {
    return childFragmentManager.findFragmentById(this.id)
}

fun NavHostFragment.isCurrentStartDestinationFragment(): Boolean {
    val currentFragmentDestinationId = findNavController().currentBackStackEntry?.destination?.id ?: 0
    val parentStartFragmentDestinationId = findNavController().graph.startDestinationId

    return currentFragmentDestinationId != 0 && currentFragmentDestinationId == parentStartFragmentDestinationId
}

fun Fragment.isCurrentNavigateTo(@IdRes navigateId: Int): Boolean = getNavCurrentDestinationID() == navigateId

fun Fragment.isParentViewVisible() : Boolean {
    return isParentViewVisible(view?.parent as? View)
}

private fun isParentViewVisible(parentView: View?) : Boolean {
    parentView ?: return true
    if (parentView.isVisible) {
        return isParentViewVisible(parentView.parent as? View)
    }

    return false
}

fun Fragment.getNavHostFragment() : NavHostFragment? {
    val navHostFragment = this.findParentNavHostFragment()

    return if (navHostFragment?.isCurrentStartDestinationFragment() == true) {
        navHostFragment
    } else {
        null
    }
}

fun NavHostFragment.getBackEntryCounts(): Int = this.childFragmentManager.backStackEntryCount

fun Fragment.isNavigateToEnabled(): Boolean {
    return try {
        findNavController()
        true
    } catch (ex: Exception) {
        false
    }
}

fun Fragment.isDialogNavigator(): Boolean {
    return try { findNavController().currentDestination is DialogFragmentNavigator.Destination } catch (_: Exception) { false }
}


class NavAnim {
    @AnimRes @AnimatorRes var enterAnim = -1
    @AnimRes @AnimatorRes var exitAnim = -1
    @AnimRes @AnimatorRes var popEnterAnim = -1
    @AnimRes @AnimatorRes var popExitAnim = -1
}

fun BottomNavHostFragment.setNavigate(@IdRes destinationId: Int, navAnim: NavAnim? = null) {
    val navController = this.navController
    if (navController.currentDestination?.id == destinationId) {
        return
    }

    try {
        this.navigateTo(
            navController = navController,
            destinationId = destinationId,
            navOptions = NavOptions.Builder().apply {
                this.setLaunchSingleTop(true).setRestoreState(true)
                if (navAnim != null) {
                    this.setEnterAnim(navAnim.enterAnim)
                        .setExitAnim(navAnim.exitAnim)
                        .setPopEnterAnim(navAnim.popEnterAnim)
                        .setPopExitAnim(navAnim.popExitAnim)
                }
                val entryDestination = navController.graph.findStartDestination()
                this.setPopUpTo(destinationId = entryDestination.id, inclusive = false, saveState = true)
            }.build(),
            singleInstance = true
        )
    } catch (_: IllegalArgumentException) {
    }
}