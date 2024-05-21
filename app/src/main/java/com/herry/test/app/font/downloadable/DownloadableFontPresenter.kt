package com.herry.test.app.font.downloadable

import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.R
import io.reactivex.Observable

class DownloadableFontPresenter: DownloadableFontContract.Presenter() {

    private val fontNodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: DownloadableFontContract.View) {
        super.onAttach(view)

        view.fontsRoot.beginTransition()
        NodeHelper.addNode(view.fontsRoot, fontNodes)
        view.fontsRoot.endTransition()
    }

    override fun onResume(view: DownloadableFontContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            loadFonts()
        }
    }

    private fun getSelectedFont(): FontListItemForm.Model? = NodeHelper.getChildrenModels<FontListItemForm.Model>(fontNodes).firstOrNull { it.isSelected }

    private fun loadFonts() {
        subscribeObservable(
            observable = Observable.fromCallable {
                val selectedFont = getSelectedFont()?.name ?: ""
                mutableListOf<FontListItemForm.Model>().apply {
                    Fonts.familyNames.forEach { familyName ->
                        add(FontListItemForm.Model(name = familyName, isSelected = familyName.equals(other = selectedFont, ignoreCase = true)))
                    }
                }
            },
            onNext = { list ->
                NodeHelper.transition(fontNodes) { transitionNode ->
                    val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
                    NodeHelper.addModels(nodes, *list.toTypedArray())
                    NodeHelper.upsert(transitionNode, nodes)
                }
            }
        )
    }

    override fun selectFont(model: FontListItemForm.Model) {
        if (model == getSelectedFont() || model.isSelected) return
        NodeHelper.transition(this.fontNodes) { transitionNode ->
            NodeHelper.getChildrenNode<FontListItemForm.Model>(transitionNode).forEach { node ->
                if (node.model == model) {
                    node.model.isSelected = true
                    node.changedNode()
                } else if (node.model.isSelected) {
                    node.model.isSelected = false
                    node.changedNode()
                }
            }
        }

        view?.onUpdateText(model.name)
    }

    private val handlerThread = HandlerThread("FontRequest").apply { start() }
    private val handler = Handler(handlerThread.looper)
    override fun applyFont() {
        val context = view?.getViewContext() ?: return
        val selectedFontName = getSelectedFont()?.name ?: return
        val PROVIDER_AUTHORITY = "com.google.android.gms.fonts"
        val PROVIDER_PACKAGE = "com.google.android.gms"

        val request = FontRequest(
            PROVIDER_AUTHORITY,
            PROVIDER_PACKAGE,
            // Query string to specify the font to download.
            // https://developers.google.com/fonts/docs/android
            "name=${selectedFontName}",
            // The certificate.
            R.array.com_google_android_gms_fonts_certs,
        )

        val callback = object : FontsContractCompat.FontRequestCallback() {
            override fun onTypefaceRetrieved(typeface: Typeface?) {
                view?.onUpdateText(selectedFontName, typeface = typeface)
            }

            override fun onTypefaceRequestFailed(reason: Int) {
                view?.onFailed(reason)
            }
        }
        FontsContractCompat.requestFont(context, request, callback, handler)
    }
}