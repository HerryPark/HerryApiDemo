package com.herry.test.app.nestedfragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.herry.libs.util.BundleUtil
import com.herry.libs.widget.extension.findNestedNavHostFragment
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.popToNavHost
import com.herry.libs.widget.extension.setFragmentNotifyListener
import com.herry.test.R
import com.herry.test.app.base.nestednav.BaseNestedNavFragment

class NestedNavFragmentsMainFragment : BaseNestedNavFragment() {

    private var container: View? = null

    private var subNavHostFragment: NavHostFragment? = null
    private var overlayNavHostFragment: NavHostFragment? = null
    private var overlayContainer: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.nested_nav_fragments_main_fragment, container, false)

            init(this.container)
        }

        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        val subNavHost = findNestedNavHostFragment(R.id.nested_nav_fragments_main_fragment_sub_container)
        if (subNavHost != null) {
            subNavHost.setFragmentNotifyListener { from, bundle ->
                onSubScreenResults(bundle)
            }

            addNestedNavHostFragment(subNavHost)

            subNavHostFragment = subNavHost
        }

        overlayContainer = view.findViewById(R.id.nested_nav_fragments_main_fragment_overlay_container)
        val overlayNavHost = findNestedNavHostFragment(R.id.nested_nav_fragments_main_fragment_overlay_container)
        if (overlayNavHost != null) {
            overlayNavHost.setFragmentNotifyListener { from, bundle ->
                onOverlayScreenResults(bundle)
            }
            addNestedNavHostFragment(overlayNavHost)
            overlayNavHostFragment = overlayNavHost
        }

        val sub2NavHost = findNestedNavHostFragment(R.id.nested_nav_fragments_main_fragment_sub2_container)
        if (sub2NavHost != null) {
            sub2NavHost.setFragmentNotifyListener { from, bundle ->
//                onOverlayScreenResults(bundle)
            }
            addNestedNavHostFragment(sub2NavHost)
        }

        view.findViewById<View>(R.id.nested_nav_fragments_main_fragment_bottom_show_sub_3).setOnClickListener {
            subNavHostFragment?.navigateTo(destinationId = R.id.nested_nav_fragments_sub13_fragment)
        }

        view.findViewById<View>(R.id.nested_nav_fragments_main_fragment_bottom_popup_to_sub1).setOnClickListener {
            subNavHostFragment?.popToNavHost()
        }

        view.findViewById<View>(R.id.nested_nav_fragments_main_fragment_bottom_close_overlay).setOnClickListener {
            overlayNavHostFragment?.popToNavHost()
        }

        view.findViewById<View>(R.id.nested_nav_fragments_main_fragment_bottom_show_overlay2).setOnClickListener {
            showOverlay(2)
        }

        view.findViewById<View>(R.id.nested_nav_fragments_main_fragment_bottom_show_second).setOnClickListener {
            navigateTo(destinationId = R.id.action_nested_nav_fragments_main_to_second)
        }

        view.findViewById<View>(R.id.nested_nav_fragments_main_fragment_bottom_show_second_with_id).setOnClickListener {
            navigateTo(destinationId = R.id.nested_nav_fragments_second_fragment)
        }
    }
    private fun onSubScreenResults(bundle: Bundle) {
        when (bundle.getInt("from")) {
            R.id.nested_nav_fragments_sub11_fragment -> {
                Toast.makeText(requireContext(), "from sub 1", Toast.LENGTH_SHORT).show()
            }
            R.id.nested_nav_fragments_sub12_fragment -> {
                Toast.makeText(requireContext(), "from sub 2", Toast.LENGTH_SHORT).show()
                showOverlay(BundleUtil[bundle, "overlay_type", 1])
            }
            R.id.nested_nav_fragments_sub13_fragment -> {
                Toast.makeText(requireContext(), "from sub 3", Toast.LENGTH_SHORT).show()
                navigateTo(directions = NestedNavFragmentsMainFragmentDirections.actionNestedNavFragmentsMainToSecond())
            }
        }
    }

    private fun onOverlayScreenResults(bundle: Bundle) {
        when (bundle.getInt("from")) {
        }
    }

    private fun showOverlay(type: Int) {
        when (type) {
            1 -> {
                overlayNavHostFragment?.navigateTo(destinationId = R.id.nested_nav_fragments_overlay_main1_fragment)
            }
            else -> {
                overlayNavHostFragment?.navigateTo(destinationId = R.id.nested_nav_fragments_overlay_main2_fragment)
            }
        }
    }

    override fun onNavigateUpResult(from: Int, result: Bundle) {
        when (from) {
            R.id.action_nested_nav_fragments_main_to_second -> {
                val context = this@NestedNavFragmentsMainFragment.context ?: return
                Toast.makeText(context, "from second screen $from by action id", Toast.LENGTH_SHORT).show()
            }

            R.id.nested_nav_fragments_second_fragment -> {
                Toast.makeText(requireContext(), "from second screen by destination id", Toast.LENGTH_SHORT).show()
            }

            R.id.nested_nav_fragments_4th_fragment -> {
                Toast.makeText(requireContext(), "from 4 ${BundleUtil.get(result, "aa", Int::class)}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
