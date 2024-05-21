package com.herry.test.app.base.nav

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.herry.libs.app.nav.BottomNavHostFragment
import com.herry.libs.app.nav.NavBundleUtil
import com.herry.libs.app.nav.NavMovement
import com.herry.libs.widget.extension.*
import com.herry.test.R
import com.herry.test.app.base.BaseActivity
import com.herry.test.app.base.nestednav.NestedNavMovement

@SuppressWarnings("unused")
abstract class BaseNavActivity : BaseActivity() {

    private var navController: NavController? = null

    private var navHostFragment: NavHostFragment? = null

    private var fragmentNavigateUpResult: Bundle? = null

    private val navActivityViewModel: SavedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())

        if (navActivityViewModel.getNavigateManager().value == null) {
            navActivityViewModel.setNavigateManager(NavigationStack())
        }

        // sets base NavHostFragment
        navHostFragment = supportFragmentManager.findFragmentById(getNavHostFragment()) as? NavHostFragment
        navHostFragment?.run {
            addOnBackStackChangedListener(this, true)
        }

        navController = navHostFragment?.findNavController()

        onSetupStartDestination()
    }

    protected open fun onSetupStartDestination() {
        setStartDestination(getStartDestination())
    }

    protected fun setStartDestination(@IdRes startDestination: Int) {
        navController?.let {
            val navGraph = it.navInflater.inflate(getGraph())
            if (startDestination != 0) {
                navGraph.setStartDestination(startDestination)
            }
            it.setGraph(navGraph, getDefaultBundle())
        }
    }

    @LayoutRes
    protected open fun getContentView(): Int = R.layout.activity_navigation

    @IdRes
    protected open fun getNavHostFragment(): Int = R.id.activity_navigation_fragment

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val bundle = getDefaultBundle()
        if (bundle != null) {
            navController?.setGraph(getGraph(), bundle)
        } else {
            navController?.setGraph(getGraph())
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

    @Deprecated("Deprecated in Java")
    final override fun onBackPressed() {
        if (navigateUpResult()) {
            if (!navigateUp()) {
                finish(getNavigationUpResult())
            }
        }
    }

    private fun navigateUp(): Boolean = getActiveNavHostFragment()?.navController?.navigateUp() ?: false

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
        return navActivityViewModel.getNavigateManager().value?.getActiveHost()
    }

    private fun getActiveFragment(): Fragment? {
        return getActiveNavHostFragment()?.childFragmentManager?.primaryNavigationFragment
    }

    abstract fun getGraph(): Int

    private fun getDefaultBundle(): Bundle? {
        return if (intent != null) intent.getBundleExtra(NavMovement.NAV_BUNDLE) else null
    }

    @IdRes
    protected open fun getStartDestination(): Int {
        var startDestination = intent?.getIntExtra(NavMovement.NAV_START_DESTINATION, 0) ?: 0
        if (startDestination == 0) {
            val navGraph = navController?.navInflater?.inflate(getGraph())
            startDestination = navGraph?.startDestinationId ?: 0
        }

        return startDestination
    }

    @Suppress("unused")
    fun finishActivity(bundle: Bundle?) {
        setNavigationUpResult(bundle)
        navController?.currentDestination?.let {
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

    interface OnFragmentManagerBackStackChangedListener : FragmentManager.OnBackStackChangedListener {
        fun isBaseHost(): Boolean

        fun host(): NavHostFragment
    }

    private fun addOnBackStackChangedListener(navHostFragment: NavHostFragment, isBase: Boolean) {
        val navigateManager = navActivityViewModel.getNavigateManager().value ?: kotlin.run {
            navActivityViewModel.setNavigateManager(NavigationStack()).value
        }

        navigateManager ?: return

        navigateManager.addHost(navHostFragment)

        navHostFragment.childFragmentManager.addOnBackStackChangedListener(
            object : OnFragmentManagerBackStackChangedListener {
                override fun isBaseHost(): Boolean = isBase

                override fun host(): NavHostFragment = navHostFragment

                override fun onBackStackChanged() {
                    // checks whether onBackStackChangedListener calling is navigate() or navigateUp()
                    val navigationStack: NavigationStack = navActivityViewModel.getNavigateManager().value ?: return
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
        )
    }

    fun addChildNavHostFragment(navHostFragment: NavHostFragment) {
        addOnBackStackChangedListener(navHostFragment, false)
    }

    internal fun setNavigationUpResult(result: Bundle?) {
        this.fragmentNavigateUpResult = result
    }

    private fun getNavigationUpResult(): Bundle? = this.fragmentNavigateUpResult
}

class SavedViewModel: ViewModel() {
    private val navigateManager = MutableLiveData<NavigationStack>()

    fun getNavigateManager(): LiveData<NavigationStack> = this.navigateManager

    fun setNavigateManager(value: NavigationStack): LiveData<NavigationStack>{
        this.navigateManager.value = value
        return this.navigateManager
    }
}

@Suppress("unused")
class NavigationStack {
    private val hosts = HashMap<Int, NavHostFragment>()
    // saves
    private val stack: MutableList<Int> = mutableListOf()
    // saves NavHostFragment.ID and entry counts
    private val hostBackStackEntryCounts: HashMap<Int, Int> = HashMap()

    fun addHost(item: NavHostFragment) {
        hosts[item.id] = item
        hostBackStackEntryCounts[item.id] = item.getBackEntryCounts()
    }

    fun removeHost(item: NavHostFragment) {
        hosts.remove(item.id)
        hostBackStackEntryCounts.remove(item.id)
    }

    internal fun pushNavigate(item: NavHostFragment) {
        stack.add(item.id)
    }

    internal fun popNavigate() {
        stack.removeLastOrNull()
    }

    fun popUpToHost(item: NavHostFragment) {
        val iterator = stack.iterator()
        for (id in iterator) {
            if (id == item.id) {
                iterator.remove()
            }
        }
    }

    fun getBackEntryCounts(item: NavHostFragment): Int = hostBackStackEntryCounts[item.id] ?: 0

    fun setBackEntryCounts(item: NavHostFragment, counts: Int) {
        hostBackStackEntryCounts[item.id] = counts
    }

    fun getActiveHost(): NavHostFragment? {
        val activeHostId = hosts.keys.firstOrNull { it == stack.lastOrNull() }
        if (activeHostId != null) {
            return hosts[activeHostId]
        }

        return null
    }
}