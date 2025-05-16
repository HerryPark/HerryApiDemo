package com.herry.test.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.NodeView
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.libs.widget.extension.setViewPadding
import com.herry.libs.widget.view.AppButton
import com.herry.test.R

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class TitleForm(
    private val searchCloseStyle: CloseStyle? = CloseStyle.FIRST_BACK_BUTTON,
    private val onSearchTextListener: OnSearchTextListener? = null,
    private val onChangedMode: ((mode: Mode) -> Unit)? = null
) : NodeView<TitleForm.Holder>() {

    companion object {
        private const val UNSET_AUTO_SIZE_UNIFORM_CONFIGURATION_VALUE = -1f
    }

    interface OnSearchTextListener {
        fun onTextChanged(text: String) {}
        fun onFocusChange(text: String, hasFocus: Boolean) {}
        fun onClosed() {}
        fun onEditorActionListener(): TextView.OnEditorActionListener? = null
    }

    private var currentMode: Mode? = null
    private var isModeChangeEnabled = true

    enum class CloseStyle {
        FIRST_BACK_BUTTON,
        TEXT_BUTTON
    }

    enum class Mode {
        NORMAL,
        SEARCH
    }

    inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
        val normalContainer: View? = view.findViewById(R.id.title_form_normal_container)
        val title: AppCompatTextView? = view.findViewById(R.id.title_form_title)
        val icon: AppCompatImageView? = view.findViewById(R.id.title_form_icon)
        val normalStartButton1Container: ViewGroup? = view.findViewById(R.id.title_form_normal_start_button_1_container)
        val normalEndButton1Container: ViewGroup? = view.findViewById(R.id.title_form_normal_end_button_1_container)
        val normalEndButton2Container: ViewGroup? = view.findViewById(R.id.title_form_normal_end_button_2_container)
        val normalEndButton3Container: ViewGroup? = view.findViewById(R.id.title_form_normal_end_button_3_container)
        val normalEndButton4Container: ViewGroup? = view.findViewById(R.id.title_form_normal_end_button_4_container)
        val normalEndButton5Container: ViewGroup? = view.findViewById(R.id.title_form_normal_end_button_5_container)
        val normalStartCustomButtonContainer: ViewGroup? = view.findViewById(R.id.title_form_normal_start_custom_button_container)
        val normalEndCustomButtonContainer: ViewGroup? = view.findViewById(R.id.title_form_normal_end_custom_button_container)

        val searchContainer: View? = view.findViewById(R.id.title_form_search_container)
        val input: EditText? = view.findViewById(R.id.title_form_search_input)
        val clearInput: View? = view.findViewById(R.id.title_form_search_clear)
        val searchStartButton1Container: ViewGroup? = view.findViewById(R.id.title_form_search_start_button_1_container)
        val searchEndButton1Container: ViewGroup? = view.findViewById(R.id.title_form_search_end_button_1_container)
        val searchStartCustomButtonContainer: ViewGroup? = view.findViewById(R.id.title_form_search_start_custom_button_container)
        val searchEndCustomButtonContainer: ViewGroup? = view.findViewById(R.id.title_form_search_end_custom_button_container)
        var closeSearch: View? = null
            private set

        val logo: ImageView? = view.findViewById(R.id.title_form_logo)

        init {
            ViewUtil.setProtectTouchLowLayer(view, true)

            ViewUtil.setProtectTouchLowLayer(normalContainer, true)

            input?.let { input ->
                input.addTextChangedListener(object : TextWatcher {
                    private var before = ""
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                        before = input.text.toString()
                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable) {
                        val after = input.text.toString()
                        if (before != after) {
                            onSearchTextListener?.onTextChanged(after)
                        }
                        clearInput?.isVisible = after.isNotEmpty()
                    }
                })
                input.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        clearFocusAndHideInput(input)
                    }
                    onSearchTextListener?.onFocusChange(input.text.toString(), hasFocus)
                }
                onSearchTextListener?.onEditorActionListener()?.let {
                    input.setOnEditorActionListener(it)
                }
                input.setOnClickListener {
                    if (!input.hasFocus()) {
                        requestFocusAndShowInput(input)
                    }
                }
            }

            clearInput?.setOnClickListener {
                input?.let { input ->
                    if (!input.text.isNullOrEmpty()) {
                        input.setText("")
                        requestFocusAndShowInput(input)
                    }
                }
            }

            ViewUtil.setProtectTouchLowLayer(searchContainer, true)
        }

        fun onBind() {
            closeSearch = when (searchCloseStyle) {
                CloseStyle.FIRST_BACK_BUTTON -> setIconActionButton(mode = Mode.SEARCH, to = ActionButton.START_FIRST, icon = R.drawable.ic_back)
                CloseStyle.TEXT_BUTTON -> setTextActionButton(mode = Mode.SEARCH, to = ActionButton.END_FIRST, text = R.string.button_cancel, styleRes = R.style.AppButton_TextOnly)
                else -> null
            }?.apply {
                setOnSingleClickListener {
                    onSearchTextListener?.onClosed()
                    setMode(Mode.NORMAL)
                }
            }
        }
    }

    override fun onLayout(): Int = R.layout.title_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindHolder(context: Context, holder: Holder) {
        holder.onBind()
    }

    fun setPadding(padding: Rect) {
        holder?.view?.setViewPadding(padding.left, padding.top, padding.right, padding.bottom)
    }

    fun setNormalPadding(padding: Rect) {
        holder?.normalContainer?.setViewPadding(padding.left, padding.top, padding.right, padding.bottom)
    }

    fun setSearchPadding(padding: Rect) {
        holder?.searchContainer?.setViewPadding(padding.left, padding.top, padding.right, padding.bottom)
    }

    fun setModeChangeEnabled(enabled: Boolean) {
        isModeChangeEnabled = enabled
    }

    fun setMode(mode: Mode) {
        if (this.currentMode == mode || !isModeChangeEnabled) {
            return
        }

        this.currentMode = mode

        holder?.normalContainer?.isVisible = mode == Mode.NORMAL
        holder?.searchContainer?.isVisible = mode == Mode.SEARCH

        onChangedMode?.invoke(mode)
    }

    fun getCurrentMode(): Mode = this.currentMode ?:  Mode.NORMAL

    fun setBackground(drawable: Drawable?) {
        holder?.view?.background = drawable
    }

    open fun setTitle(title: String) {
        holder?.title?.text = title
    }

    open fun setTitle(@StringRes resId: Int) {
        holder?.title?.setText(resId)
    }

    fun setTitleStyle(typeface: Typeface) {
        holder?.title?.typeface = typeface
    }

    fun setTitleSize(@Px maxSize: Int? = null, @Px minSize: Int? = null) {
        if (maxSize == null && minSize == null) return

        holder?.title?.let { view ->
            if (maxSize != null) {
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, maxSize.toFloat())
            }

            val textAutoSizeMiniSize = TextViewCompat.getAutoSizeMinTextSize(view)
            val textAutoSizeMaxSize = TextViewCompat.getAutoSizeMaxTextSize(view)
            val textAutoSizeStepGranularity =TextViewCompat.getAutoSizeStepGranularity(view)

            try {
                val minimumSize = ViewUtil.convertDpToPx(1f).toInt()
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    view,
                    kotlin.math.max(minimumSize, minSize ?: textAutoSizeMiniSize),
                    kotlin.math.max(minimumSize, maxSize ?: textAutoSizeMaxSize),
                    kotlin.math.max(minimumSize, textAutoSizeStepGranularity),
                    TypedValue.COMPLEX_UNIT_PX
                )
            } catch (_: Exception) {
            }
        }
    }

    fun setTitleSize(@Px size: Int) {
        holder?.title?.let { view ->
            TextViewCompat.setAutoSizeTextTypeWithDefaults(view, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        }
    }

    fun setTitleMaxLines(lines: Int, ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END) {
        holder?.title?.let { view ->
            view.maxLines = lines
            view.ellipsize = ellipsize
        }
    }

    fun setTitleVisible(isVisible: Boolean) {
        holder?.title?.isVisible = isVisible
    }

    fun setTitleGravity(gravity: Int, ignoreButtons: Boolean = true) {
        val titleView = holder?.title ?: return
        titleView.gravity = gravity

        val constraintLayout = holder?.normalContainer as? ConstraintLayout ?: return
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        if (ignoreButtons) {
            constraintSet.connect(titleView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(titleView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        } else {
            val textViewStartTargetView = if (holder?.normalStartButton1Container?.isVisible == true) holder?.normalStartButton1Container else holder?.icon
            val textViewStartTargetViewId = textViewStartTargetView?.id ?: ConstraintSet.PARENT_ID
            constraintSet.connect(titleView.id, ConstraintSet.START, textViewStartTargetViewId, if (textViewStartTargetViewId == ConstraintSet.PARENT_ID) ConstraintSet.START else ConstraintSet.END)

            val textViewEndTargetViewId = holder?.normalEndButton5Container?.id ?: ConstraintSet.PARENT_ID
            constraintSet.connect(titleView.id, ConstraintSet.END, textViewEndTargetViewId, if (textViewEndTargetViewId == ConstraintSet.PARENT_ID) ConstraintSet.END else ConstraintSet.START)
        }

        constraintSet.applyTo(constraintLayout)
    }

    fun setTitlePadding(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
        holder?.title?.let { view ->
            view.setViewPadding(
                start = start ?: view.paddingStart,
                top = top ?: view.paddingTop,
                end = end ?: view.paddingEnd,
                bottom = bottom ?: view.paddingBottom
            )
        }
    }

    fun setTitleColor(colors: ColorStateList) {
        holder?.title?.setTextColor(colors)
    }

    fun setTitleColor(@ColorInt color: Int) {
        holder?.title?.setTextColor(color)
    }

    fun getCurrentSearchText(): String? = holder?.input?.text?.toString()

    fun setIcon(@DrawableRes icon: Int?): View? {
        val iconView = holder?.icon ?: return null
        if (icon == null) {
            iconView.isVisible = false
        } else {
            iconView.isVisible = true
            iconView.setImageResource(icon)
        }
        return iconView
    }

    // start_first           end_fifth | end_fourth | end_third | end_second | end_first
    enum class ActionButton {
        START_FIRST,
        END_FIRST,
        END_SECOND,
        END_THIRD,
        END_FOURTH,
        END_FIFTH,

        START_CUSTOM,
        END_CUSTOM;

        fun isCustom(): Boolean = this == START_CUSTOM || this == END_CUSTOM
    }

    protected open fun createActionButton(context: Context, mode: Mode = Mode.NORMAL, to: ActionButton, @StyleRes styleRes: Int): AppButton? {
        val container = getActionButtonContainer(mode = mode, to = to) ?: return null
        val actionButton = AppButton(context, styleRes)
        container.removeAllViews()
        container.addView(actionButton)
        container.isVisible = true
        return actionButton
    }

    fun clearActionButton(mode: Mode = Mode.NORMAL, to: ActionButton) {
        val container = getActionButtonContainer(mode = mode, to = to) ?: return
        container.removeAllViews()
        container.isVisible = false
    }

    /* ---------------------------------------------------------------
     * the action button
     * --------------------------------------------------------------- */
    fun setActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, @DrawableRes icon: Int? = null, @StringRes text: Int? = null, @StyleRes styleRes: Int): AppButton? {
        val context = holder?.context ?: return null
        if ((icon == null || icon == 0) && (text == null || text == 0)) return null
        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setIcon(icon)
            this.setText(text)
        }
    }

    fun setActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, icon: Drawable? = null, text: String? = null, @StyleRes styleRes: Int): AppButton? {
        val context = holder?.context ?: return null
        if ((icon == null) && (text == null)) return null
        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setIcon(icon)
            this.setText(text)
        }
    }

    /* ---------------------------------------------------------------
     * the icon action button
     * --------------------------------------------------------------- */
    fun setIconActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, @DrawableRes icon: Int, @StyleRes styleRes: Int = R.style.AppButton_Icon): AppButton? {
        val context = holder?.context ?: return null
        if (icon == 0) return null

        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setIcon(icon)
        }
    }

    fun setIconActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, icon: Drawable?, @StyleRes styleRes: Int = R.style.AppButton_Icon): AppButton? {
        val context = holder?.context ?: return null
        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setIcon(icon)
        }
    }

    /* ---------------------------------------------------------------
     * the text action button
     * --------------------------------------------------------------- */
    fun setTextActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, @StringRes text: Int, @StyleRes styleRes: Int = R.style.AppButton_Standard_Fill_Solid): AppButton? {
        val context = holder?.context ?: return null
        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setText(text)
        }
    }

    fun setTextActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, text: String, @StyleRes styleRes: Int = R.style.AppButton_Standard_Fill_Solid): AppButton? {
        val context = holder?.context ?: return null
        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setText(text)
        }
    }

    /* ---------------------------------------------------------------
     * the icon and text action button
     * --------------------------------------------------------------- */
    fun setIconAndTextActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, @DrawableRes icon: Int, @StringRes text: Int, @StyleRes styleRes: Int = R.style.AppButton_Standard_Fill_Solid): AppButton? {
        val context = holder?.context ?: return null
        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setIcon(icon)
            this.setText(text)
        }
    }

    fun setIconAndTextActionButton(mode: Mode = Mode.NORMAL, to: ActionButton, icon: Drawable?, text: String, @StyleRes styleRes: Int = R.style.AppButton_Standard_Fill_Solid): AppButton? {
        val context = holder?.context ?: return null
        return createActionButton(context = context, mode = mode, to = to, styleRes = styleRes)?.apply {
            this.setIcon(icon)
            this.setText(text)
        }
    }

    fun setActionButtonEnabled(mode: Mode = Mode.NORMAL, to: ActionButton, enabled: Boolean) {
        getActionButton(mode = mode, to = to)?.isEnabled = enabled
    }

    fun setActionButtonVisible(mode: Mode = Mode.NORMAL, to: ActionButton, visible: Boolean) {
        getActionButtonContainer(mode = mode, to = to)?.isVisible = visible
    }

    private fun getActionButtonContainer(mode: Mode, to: ActionButton): ViewGroup? {
        return when (mode) {
            Mode.NORMAL -> when (to) {
                ActionButton.START_FIRST -> holder?.normalStartButton1Container
                ActionButton.END_FIRST -> holder?.normalEndButton1Container
                ActionButton.END_SECOND -> holder?.normalEndButton2Container
                ActionButton.END_THIRD -> holder?.normalEndButton3Container
                ActionButton.END_FOURTH -> holder?.normalEndButton4Container
                ActionButton.END_FIFTH -> holder?.normalEndButton5Container
                ActionButton.START_CUSTOM -> holder?.normalStartCustomButtonContainer
                ActionButton.END_CUSTOM -> holder?.normalEndCustomButtonContainer
            }
            Mode.SEARCH -> when (to) {
                ActionButton.START_FIRST -> holder?.searchStartButton1Container
                ActionButton.END_FIRST -> holder?.searchEndButton1Container
                ActionButton.START_CUSTOM -> holder?.searchStartCustomButtonContainer
                ActionButton.END_CUSTOM -> holder?.searchEndCustomButtonContainer
                else -> throw IllegalArgumentException("cannot access to action button: $to on mode: $mode")
            }
        }
    }

    fun getActionButton(mode: Mode = Mode.NORMAL, to: ActionButton): View? {
        return getActionButton(getActionButtonContainer(mode = mode, to = to))
    }

    private fun getActionButton(container: ViewGroup?): View? {
        return container?.getChildAt(0)
    }

    fun setActionButtonIcon(mode: Mode = Mode.NORMAL, to: ActionButton, @DrawableRes icon: Int, @StyleRes style: Int? = null) {
        val actionButton = getActionButton(mode = mode, to = to) as? AppButton
        if (style != null) {
            actionButton?.setStyle(style)
        }
        actionButton?.setIcon(icon)
    }

    fun removeActionButton(mode: Mode = Mode.NORMAL, to: ActionButton) {
        getActionButton(mode = mode, to = to) ?: return
        getActionButtonContainer(mode = mode, to = to)?.removeAllViews()
    }

    /* ---------------------------------------------------------------
     * the custom button
     * --------------------------------------------------------------- */
    fun setCustomButton(mode: Mode = Mode.NORMAL, to: ActionButton, view: View?, gravity: Int = Gravity.CENTER_VERTICAL): View? {
        if (!to.isCustom()) return null

        val container = getActionButtonContainer(mode = mode, to = to) ?: return null
        container.removeAllViews()
        container.isVisible = view != null

        view ?: return null

        ViewUtil.removeViewFormParent(view)
        container.clipToOutline = true
        container.addView(
            view,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                this.gravity = gravity
            })

        return view
    }

    /* ---------------------------------------------------------------
     * the search container
     * --------------------------------------------------------------- */
    fun setSearchContainerBackground(drawable: Drawable?) {
        holder?.searchContainer?.background = drawable
    }

    fun setSearchText(text: String) {
        holder?.input?.setText(text)
    }

    fun setSearchTextSelection(index: Int) {
        holder?.input?.setSelection(index)
    }

    fun setSearchTextSelectionEnd() {
        val inputText = holder?.input ?: return
        inputText.setSelection(inputText.text.length)
    }

    fun setSearchTextSelectionAll() {
        holder?.input?.selectAll()
    }

    fun setSearchTextInputEnabled(enabled: Boolean) {
        holder?.input?.isEnabled = enabled
    }

    fun hasSearchTextInputFocused(): Boolean {
        return holder?.input?.hasFocus() == true
    }

    fun setSearchHint(hint: String) {
        holder?.input?.hint = hint
    }

    fun isSearchInputTextEnabled(): Boolean = holder?.input?.isEnabled == true

    private fun requestFocusAndShowInput(view: EditText?) {
        val context = view?.context ?: return

        view.isFocusable = true
        view.isFocusableInTouchMode = true
        if (!view.hasFocus()) {
            view.requestFocus()
            view.setSelection(if (null != view.text) view.text?.length ?: 0 else 0)
        }

        view.postDelayed({
            val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(view, 0, null)
        }, context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
    }

    fun clearFocusAndHideInput(view: EditText?) {
        val context = view?.context ?: return

        if (view.hasFocus()) {
            view.clearFocus()
        }
        view.isFocusable = false
        view.isFocusableInTouchMode = false

        hideInput(context, view.windowToken)
    }

    private fun hideInput(context: Context, windowToken: IBinder) {
        val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

    fun requestFocusInputField(focus: Boolean, withKeyboard: Boolean = false) {
        val view: EditText = holder?.input ?: return
        val context = view.context ?: return

        if (focus && currentMode != Mode.SEARCH) {
            if (withKeyboard) {
                hideInput(context, view.windowToken)
            }
            return
        }

        if (withKeyboard) {
            if (focus) requestFocusAndShowInput(view) else clearFocusAndHideInput(view)
        } else {
            ViewUtil.requestFocus(holder?.input, focus)
        }
    }

    /**
     * @param imeAction refer EditorInfo.IME_ACTION_XXX
     */
    fun setInputEditorAction(imeAction: Int) {
        val input = holder?.input ?: return
        input.imeOptions = input.imeOptions or imeAction
    }

    fun setLogoImage(@DrawableRes imageRes: Int?): ImageView? {
        val logo = holder?.logo ?: return null
        if (imageRes == null) {
            logo.isVisible = false
        } else {
            logo.isVisible = true
            logo.setImageResource(imageRes)
        }
        return logo
    }

    // for normal container
    fun setOnTouchListener(listener: View.OnTouchListener) {
        holder?.normalContainer?.setOnTouchListener(listener)
    }
}