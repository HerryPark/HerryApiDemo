package com.herry.test.app.base.nav

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.herry.libs.app.nav.BottomNavHostFragment
import com.herry.libs.app.nav.NavBundleUtil
import com.herry.libs.app.nav.NavMovement
import com.herry.libs.log.Trace
import com.herry.libs.widget.extension.getBackEntryCounts
import com.herry.libs.widget.extension.getNavCurrentDestinationID
import com.herry.libs.widget.extension.isCurrentStartDestinationFragment
import com.herry.libs.widget.extension.isNestedNavHostFragment
import com.herry.libs.widget.extension.isParentViewVisible
import com.herry.libs.widget.extension.setFragmentResult
import com.herry.test.R
import com.herry.test.app.base.BaseActivity
import com.herry.test.app.base.nestednav.NestedNavMovement
import java.util.concurrent.ConcurrentHashMap

@SuppressWarnings("unused")
abstract class BaseNavActivity : BaseActivity() {

    private var navHostFragment: NavHostFragment? = null

    private var fragmentNavigateUpResult: Bundle? = null

    private val navActivityViewModel: SavedViewModel by viewModels()

    private fun getNavController(): NavController? = navHostFragment?.findNavController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (navActivityViewModel.getNavigateManager() == null) {
            navActivityViewModel.setNavigateManager(NavigationStack())
        }

        // sets base NavHostFragment
        navHostFragment = supportFragmentManager.findFragmentById(getNavHostFragmentId()) as? NavHostFragment
        findViewById<View?>(android.R.id.content)?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                val navHostFragment = navHostFragment ?: return
                addOnBackStackChangedListener(navHostFragment, true)
            }

            override fun onViewDetachedFromWindow(v: View) {
                val navHostFragment = navHostFragment ?: return
                removeOnBackStackChangedListener(navHostFragment)
            }
        })

        onSetupStartDestination()
    }

    override fun onDestroy() {
        navHostFragment = null
        super.onDestroy()
    }

    protected open fun onSetupStartDestination() {
        setStartDestination(getStartDestination())
    }

    protected fun setStartDestination(@IdRes startDestination: Int) {
        getNavController()?.let {
            val navGraph = it.navInflater.inflate(getGraph())
            if (startDestination != 0) {
                navGraph.setStartDestination(startDestination)
            }
            it.setGraph(navGraph, getNavBundle())
        }
    }

    @LayoutRes
    override fun getContentViewId(): Int = R.layout.activity_navigation

    @IdRes
    protected open fun getNavHostFragmentId(): Int = R.id.activity_navigation_fragment

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val bundle = getNavBundle()
        if (bundle != null) {
            getNavController()?.setGraph(getGraph(), bundle)
        } else {
            getNavController()?.setGraph(getGraph())
        }
    }

    //  This method is called whenever the user chooses to navigate Up within your application's
    //  activity hierarchy from the action bar.
    override fun onSupportNavigateUp(): Boolean {
        if (navigateUpResult()) {
            return navigateUp()
        }
        return super.onSupportNavigateUp()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    final override fun onBackPressed() {
        if (navigateUpResult()) {
            if (!navigateUp()) {
                finish(getNavigationUpResult())
            }
        }
    }

    private fun navigateUp(): Boolean = getActiveNavHostFragment()?.navController?.navigateUp() == true

    private fun isNavigationUpBlocked(fragment: Fragment?): Boolean {
        if (fragment is NavMovement) {
            return fragment.onNavigateUp()
        }

        return false
    }

    private fun navigateUpResult(): Boolean {
        val baseActiveFragment = this.navHostFragment?.childFragmentManager?.primaryNavigationFragment
        if (baseActiveFragment is NestedNavMovement && baseActiveFragment.onInterceptNavigateUp()) {
            // previous processing on base nested navigation fragment
            return false
        }

        // find lasted added fragment for back key processing
        val activeFragment = getActiveFragment()
        val activeNavHostFragment = getActiveNavHostFragment()
        if (activeFragment == null ||
            (activeNavHostFragment is BottomNavHostFragment && activeNavHostFragment.isNavScreenStartDestination())) {
            // do navigate up to all child start fragment of each child NavHostFragments
            val fragments = this.navHostFragment?.childFragmentManager?.fragments ?: mutableListOf()
            for (fragment in fragments) {
                val navHostFragment = findNavHostFragment(fragment)
                // checks sub NavHostFragment
                val nestedNavHostFragments = navHostFragment?.parentFragmentManager?.fragments ?: mutableListOf()
                nestedNavHostFragments.asReversed().forEach {
                    val isFragmentContainerViewVisible = it.isParentViewVisible()
                    val isNestedNavFragment = it.isNestedNavHostFragment()
                    val currentFragment = it.childFragmentManager.primaryNavigationFragment
                    val isCurrentStartDestinationFragment = (it is NavHostFragment) && it.isCurrentStartDestinationFragment()

                    if (isNestedNavFragment && isFragmentContainerViewVisible && isCurrentStartDestinationFragment) {
                        if (isNavigationUpBlocked(currentFragment)) {
                            // blocked start fragment of child the NavHostFragment
                            return false
                        }
                    }
                }
            }

            if (isNavigationUpBlocked(baseActiveFragment)) {
                // blocked start fragment of base the NavHostFragment
                return false
            }
        } else if (activeFragment is NavMovement) {
            if (activeFragment.isTransition()) {
                return false
            }

            if (activeFragment.onNavigateUp()) {
                setNavigationUpResult(null)
                return false
            }

            // sets result of the current screen
            setNavigationUpResult(activeFragment.getNavigateUpResult()?.apply {
                getCurrentDestination()?.let {
                    NavBundleUtil.addFromNavigationId(this, it.id)
                }
            })
        }

        return true
    }

    protected open fun getCurrentDestination(): NavDestination? = getActiveNavHostFragment()?.navController?.currentDestination

    private fun findNavHostFragment(fragment: Fragment?): NavHostFragment? {
        fragment ?: return null

        if (fragment is BaseNavFragment) {
            val fragments = fragment.childFragmentManager.fragments
            for (childFragment in fragments) {
                return findNavHostFragment(childFragment)
            }
        }

        if (fragment is NavHostFragment) {
            return fragment
        }

        return null
    }

    private fun getActiveNavHostFragment(): NavHostFragment? {
        // find from navigation stack
        val activeHost = navActivityViewModel.getNavigateManager()?.getActiveHost()
        // Add safety checks here if activeHost can be non-null but not added
        if (activeHost != null && activeHost.isAdded) {
            return activeHost
        }
        return null
    }

    protected fun getActiveFragment(): Fragment? {
        val activeNavHost = getActiveNavHostFragment()

        // Check the NavHostFragment returned by getActiveNavHostFragment() first
        if (activeNavHost != null && activeNavHost.isAdded && !activeNavHost.isStateSaved) {
            try {
                // It's possible for primaryNavigationFragment to be null even if childFragmentManager is available
                val primaryFragment = activeNavHost.childFragmentManager.primaryNavigationFragment
                if (primaryFragment != null && primaryFragment.isAdded) {
                    return primaryFragment
                }
            } catch (e: IllegalStateException) {
                Trace.w("IllegalStateException while accessing primaryNavigationFragment from activeNavHost", e)
            }
        }

        // Fallback or primary check for the activity's main navHostFragment
        val baseNavHost = this.navHostFragment // Assuming this is the NavHostFragment for the Activity itself
        if (baseNavHost != null && baseNavHost.isAdded && !baseNavHost.isStateSaved) {
            try {
                val primaryFragment = baseNavHost.childFragmentManager.primaryNavigationFragment
                if (primaryFragment != null && primaryFragment.isAdded) {
                    return primaryFragment
                }
            } catch (e: IllegalStateException) {
                Trace.w("IllegalStateException while accessing primaryNavigationFragment from baseNavHost", e)
                // Fragment is not in a state to provide its primary navigation fragment
            }
        }

        return null // Return null if no valid active fragment is found
    }

    abstract fun getGraph(): Int

    protected fun getNavBundle(): Bundle? {
        return if (intent != null) intent.getBundleExtra(NavMovement.NAV_BUNDLE) else null
    }

    @IdRes
    protected open fun getStartDestination(): Int {
        var startDestination = if (intent != null) intent.getIntExtra(NavMovement.NAV_START_DESTINATION, 0) else 0
        if (startDestination == 0) {
            val navGraph = getNavController()?.navInflater?.inflate(getGraph())
            startDestination = navGraph?.startDestinationId ?: 0
        }

        return startDestination
    }

    fun finishActivity(bundle: Bundle?) {
        setNavigationUpResult(bundle)
        getNavController()?.currentDestination?.let {
            NavBundleUtil.addFromNavigationId(getNavigationUpResult(), it.id)
        }
        if (!navigateUp()) {
            finish(bundle)
        }
    }

    private fun finish(bundle: Bundle?) {
        if (bundle != null) {
            val intent = Intent()
            intent.putExtra(NavMovement.NAV_BUNDLE, bundle)
            setResult(
                if (NavBundleUtil.isNavigationResultOk(bundle)) RESULT_OK else RESULT_CANCELED,
                intent
            )
        }

        runOnUiThread { finishAfterTransition() }
    }

    private fun getNavigateManager(): NavigationStack? = navActivityViewModel.getNavigateManager()

    private fun addOnBackStackChangedListener(navHostFragment: NavHostFragment, isBase: Boolean) {
        val navigateManager = getNavigateManager()

        navigateManager ?: return

        val onBackStackChangedListener = object : OnFragmentManagerBackStackChangedListener {
            override fun isBaseHost(): Boolean = isBase

            override fun host(): NavHostFragment = navHostFragment

            override fun onBackStackChanged() {
                // checks whether onBackStackChangedListener calling is navigate() or navigateUp()
                val navigationStack: NavigationStack = getNavigateManager() ?: return
                val previousBackEntryCounts = navigationStack.getBackEntryCounts(navHostFragment)
                val currentBackEntryCounts = host().getBackEntryCounts()

                val activeFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

                when {
                    previousBackEntryCounts < currentBackEntryCounts -> {
                        // called by navigate()
                        navigationStack.pushNavigate(navHostFragment)
                    }
                    previousBackEntryCounts > currentBackEntryCounts -> {
                        // called by navigateUp() or popToNavHost()
                        if (currentBackEntryCounts <= 0) {
                            // pop all
                            navigationStack.popUpToHost(navHostFragment)

                            // process navigate up result data
                            getNavigationUpResult()?.let { result ->
                                if (activeFragment is NavMovement) {
                                    val currentId = activeFragment.getNavCurrentDestinationID()
                                    if (currentId != 0) {
                                        activeFragment.setFragmentResult(requestKey = currentId.toString(), result)
                                    }
                                }

                                setNavigationUpResult(null)
                            }
                        } else {
                            navigationStack.popNavigate()

                            // process navigate up result data
                            getNavigationUpResult()?.let { result ->
                                if (activeFragment is NavMovement) {
                                    val navDestination = navHostFragment.navController.currentDestination
                                    if (navDestination != null) {
                                        val navUpDesId = result.getInt(NavMovement.NAV_UP_DES_ID, 0)
                                        val currentDesId = navHostFragment.navController.currentDestination?.id ?: 0
                                        if (navUpDesId != 0 &&
                                            navUpDesId != currentDesId
                                        ) {
//                                                NavBundleUtil.addFromNavigationId(result, currentDesId)
                                            if (!navigateUp()) {
                                                if (isBase) {
                                                    finish(result)
                                                }
                                            }
                                        } else {
                                            val currentId = activeFragment.getNavCurrentDestinationID()
                                            if (currentId != 0) {
                                                activeFragment.setFragmentResult(requestKey = currentId.toString(), result)
                                            }

                                            setNavigationUpResult(null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        return
                    }
                }

                navigationStack.setBackEntryCounts(navHostFragment, currentBackEntryCounts)
            }
        }
        navigateManager.addHost(navHostFragment, onBackStackChangedListener)
    }

    fun removeOnBackStackChangedListener(navHostFragment: NavHostFragment) {
        getNavigateManager()?.removeHost(navHostFragment)
    }

    fun addChildNavHostFragment(navHostFragment: NavHostFragment) {
        addOnBackStackChangedListener(navHostFragment, false)
    }

    fun removeChildNavHostFragment(navHostFragment: NavHostFragment) {
        removeOnBackStackChangedListener(navHostFragment)
    }

    fun hasChildNavHostFragment(navHostFragment: NavHostFragment): Boolean {
        return getNavigateManager()?.hasHost(navHostFragment) == true
    }

    internal fun setNavigationUpResult(result: Bundle?) {
        this.fragmentNavigateUpResult = result
    }

    private fun getNavigationUpResult(): Bundle? = this.fragmentNavigateUpResult

    private fun clearFocus() {
        navHostFragment?.view?.requestFocus()
    }
}

internal class SavedViewModel: ViewModel() {
    private var navigateManager: NavigationStack? = null

    fun getNavigateManager(): NavigationStack? = this.navigateManager

    fun setNavigateManager(value: NavigationStack) {
        this.navigateManager = value
    }

    override fun onCleared() {
        super.onCleared()
        navigateManager?.cleanup()
        navigateManager = null
    }
}

internal interface OnFragmentManagerBackStackChangedListener : FragmentManager.OnBackStackChangedListener {
    fun isBaseHost(): Boolean

    fun host(): NavHostFragment
}

internal data class NavigationHostData(
    val navHostFragment: NavHostFragment,
    val onBackStackChangedListener: OnFragmentManagerBackStackChangedListener
)

@Suppress("unused")
internal class NavigationStack {
    private val hosts = ConcurrentHashMap<Int, NavigationHostData>()
    // saves
    private val stack: MutableList<Int> = mutableListOf()
    // saves NavHostFragment.ID and entry counts
    private val hostBackStackEntryCounts: ConcurrentHashMap<Int, Int> = ConcurrentHashMap()

    fun addHost(navHostFragment: NavHostFragment, onBackStackChangedListener: OnFragmentManagerBackStackChangedListener) {
        if (hasHost(navHostFragment)) {
            removeHost(navHostFragment)
        }
        hosts[navHostFragment.id] = NavigationHostData(navHostFragment, onBackStackChangedListener)
        navHostFragment.childFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)

        hostBackStackEntryCounts[navHostFragment.id] = navHostFragment.getBackEntryCounts()
    }

    fun removeHost(navHostFragment: NavHostFragment) {
        val hostData = getHost(navHostFragment)
        if (hostData != null) {
            navHostFragment.childFragmentManager.removeOnBackStackChangedListener(hostData.onBackStackChangedListener)
        }
        hosts.remove(navHostFragment.id)

        hostBackStackEntryCounts.remove(navHostFragment.id)
    }

    private fun getHost(navHostFragment: NavHostFragment): NavigationHostData? = hosts[navHostFragment.id]

    fun hasHost(navHostFragment: NavHostFragment) = getHost(navHostFragment) != null

    internal fun pushNavigate(navHostFragment: NavHostFragment) {
        stack.add(navHostFragment.id)
    }

    internal fun popNavigate() {
        stack.removeLastOrNull()
    }

    fun popUpToHost(navHostFragment: NavHostFragment) {
        val iterator = stack.iterator()
        for (id in iterator) {
            if (id == navHostFragment.id) {
                iterator.remove()
            }
        }
    }

    fun getBackEntryCounts(navHostFragment: NavHostFragment): Int = hostBackStackEntryCounts[navHostFragment.id] ?: 0

    fun setBackEntryCounts(navHostFragment: NavHostFragment, counts: Int) {
        hostBackStackEntryCounts[navHostFragment.id] = counts
    }

    fun getActiveHost(): NavHostFragment? {
        val activeHostId = stack.lastOrNull()
        return if (activeHostId != null) {
            hosts[activeHostId]?.navHostFragment
        } else {
            null
        }
    }

    fun cleanup() {
        hosts.values.toMutableList().forEach { hostData ->
            removeHost(hostData.navHostFragment)
        }
        hosts.clear()
        hostBackStackEntryCounts.clear()
        stack.clear()
    }
}