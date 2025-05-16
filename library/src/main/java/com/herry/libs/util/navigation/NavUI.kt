package com.herry.libs.util.navigation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.view.forEach
import androidx.navigation.ActivityNavigator
import androidx.navigation.AnimBuilder
import androidx.navigation.FloatingWindow
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView
import com.herry.libs.log.Trace
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

object NavUI {
    val navAnimNone: AnimBuilder = AnimBuilder().apply {
        enter = -1
        exit = -1
        popEnter = -1
        popExit = -1
    }

    /**
     * Sets up a [NavigationBarView] for use with a [NavController]. This will call
     * [onNavDestinationSelected] when a menu item is selected. The selected item in the
     * NavigationBarView will automatically be updated when the destination changes.
     *
     * Destinations that implement [androidx.navigation.FloatingWindow] will be ignored.
     *
     * @param navigationBarView The NavigationBarView ([BottomNavigationView] or
     *   [NavigationRailView]) that should be kept in sync with changes to the NavController.
     * @param navController The NavController that supplies the primary menu. Navigation actions on
     *   this NavController will be reflected in the selected item in the NavigationBarView.
     */
    fun setupWithNavController(
        navigationBarView: NavigationBarView,
        navController: NavController,
        animBuilder: AnimBuilder? = null,
        onIsSelectable: (item: MenuItem) -> Boolean,
        onItemSelected: (item: MenuItem) -> Unit,
        onItemUnselected: (item: MenuItem) -> Unit,
        onItemReselected: (item: MenuItem) -> Unit
    ) {
        val weakReference = WeakReference(navigationBarView)
        navController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?
                ) {
                    val view = weakReference.get()
                    if (view == null) {
                        navController.removeOnDestinationChangedListener(this)
                        return
                    }
                    if (destination is FloatingWindow) {
                        return
                    }
                    view.menu.forEach { item ->
                        if (destination.matchDestination(item.itemId)) {
                            item.isChecked = true
                        }
                    }
                }
            }
        )

        navigationBarView.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
            private val selectedMenuItem: AtomicReference<MenuItem> = AtomicReference()
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val isSelected = if (onIsSelectable(item)) {
                    // changes the bottom navigation bar selected item by changing the start destination ID of the nav controller
                    onNavDestinationSelected(item, navController, animBuilder)
                } else {
                    false
                }

                if (isSelected) {
                    val unselectedMenuItem = selectedMenuItem.get()
                    if (unselectedMenuItem != null) {
                        // to do unselected menu item
                        onItemUnselected(unselectedMenuItem)
                    }

                    // to do selected menu item
                    selectedMenuItem.set(item)
                    // wait navigation is changed, and then notify the item selected
                    onItemSelected(item)
                }

                return isSelected
            }
        })

        navigationBarView.setOnItemReselectedListener { item ->
            onItemReselected(item)
        }
    }

    /**
     * Attempt to navigate to the [NavDestination] associated with the given MenuItem. This MenuItem
     * should have been added via one of the helper methods in this class.
     *
     * Importantly, it assumes the [menu item id][MenuItem.getItemId] matches a valid
     * [action id][NavDestination.getAction] or [destination id][NavDestination.id] to be navigated
     * to.
     *
     * By default, the back stack will be popped back to the navigation graph's start destination.
     * Menu items that have `android:menuCategory="secondary"` will not pop the back stack.
     *
     * @param item The selected MenuItem.
     * @param navController The NavController that hosts the destination.
     * @return True if the [NavController] was able to navigate to the destination associated with
     *   the given MenuItem.
     */
    private fun onNavDestinationSelected(item: MenuItem, navController: NavController, animBuilder: AnimBuilder?): Boolean {
        val builder = NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(true)

        if (animBuilder != null) {
            builder
                .setEnterAnim(animBuilder.enter)
                .setExitAnim(animBuilder.exit)
                .setPopEnterAnim(animBuilder.popEnter)
                .setPopExitAnim(animBuilder.popExit)
        } else if (navController.currentDestination?.parent?.findNode(item.itemId) is ActivityNavigator.Destination) {
            builder
                .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim)
        } else {
            builder
                .setEnterAnim(androidx.navigation.ui.R.animator.nav_default_enter_anim)
                .setExitAnim(androidx.navigation.ui.R.animator.nav_default_exit_anim)
                .setPopEnterAnim(androidx.navigation.ui.R.animator.nav_default_pop_enter_anim)
                .setPopExitAnim(androidx.navigation.ui.R.animator.nav_default_pop_exit_anim)
        }

        if (item.order and Menu.CATEGORY_SECONDARY == 0) {
            builder.setPopUpTo(
                navController.graph.findStartDestination().id,
                inclusive = false,
                saveState = true
            )
        }

        val options = builder.build()
        return try {
            navController.navigate(item.itemId, null, options)
            // Return true only if the destination we've navigated to matches the MenuItem
            navController.currentDestination?.matchDestination(item.itemId) == true
        } catch (e: IllegalArgumentException) {
            val name = NavDestination.getDisplayName(navController.context, item.itemId)
            Trace.w("Ignoring onNavDestinationSelected for MenuItem $name as it cannot be found " +
                    "from the current destination ${navController.currentDestination}", e)
            false
        }
    }

    /**
     * Determines whether the given `destId` matches the NavDestination. This handles both the
     * default case (the destination's id matches the given id) and the nested case where the given
     * id is a parent/grandparent/etc of the destination.
     */
    private fun NavDestination.matchDestination(@IdRes destId: Int): Boolean = this.hierarchy.any { it.id == destId }
}