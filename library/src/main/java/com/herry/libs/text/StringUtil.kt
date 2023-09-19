package com.herry.libs.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.telephony.PhoneNumberUtils
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.TextView
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import kotlin.math.pow

/**
 * Created by herry.park
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object StringUtil {
    /**
     * Convert integer value to amount string
     * @param amount number
     * @return string value
     */
    fun convertIntToDisplayNumberString(amount: Int, subString: String? = null): String {
        var result = String.format(Locale.getDefault(), "%,d", amount)
        if (subString?.isNotEmpty() == true) {
            result += subString
        }
        return result
    }

    fun convertIntToDisplayNumberString(amount: Long, subString: String? = null): String {
        var result = String.format(Locale.getDefault(), "%,d", amount)
        if (subString?.isNotEmpty() == true) {
            result += subString
        }
        return result
    }

    fun convertIntToDisplayNumberString(amount: Double, subString: String? = null, subCount: Int = 1): String {
        var result = String.format(Locale.getDefault(), "%,." + subCount + "f", amount)
        if (subString?.isNotEmpty() == true) {
            result += subString
        }
        return result
    }

    fun concatDifferentString(vararg stringSpanAttributes: StringSpanAttribute?): CharSequence? {
        if (stringSpanAttributes.isEmpty()) {
            return null
        }
        var result: Spanned? = null
        for (stringSpanAttribute in stringSpanAttributes) {
            if (stringSpanAttribute == null || stringSpanAttribute.string.isEmpty()) {
                continue
            }
            val string = stringSpanAttribute.string
            val stringLength = string.length
            val spannableString = SpannableString(string)
            val spans = stringSpanAttribute.spans
            if (spans.isNotEmpty()) {
                for (span in spans) {
                    if (span.first == null) {
                        continue
                    }
                    spannableString.setSpan(span.first, 0, stringLength, span.second!!)
                }
            }
            result = if (null == result) {
                spannableString
            } else {
                TextUtils.concat(result, spannableString) as Spanned
            }
        }
        return result
    }

    fun applySpans(base: String?, targets: Array<String?>?, spans: Array<SpanObject?>?): SpannableString {
        if (base.isNullOrEmpty()) {
            return SpannableString("")
        }
        if (targets.isNullOrEmpty()) {
            return SpannableString(base)
        }
        if (spans.isNullOrEmpty()) {
            return SpannableString(base)
        }
        val targetsCount = targets.size
        val spansCount = spans.size
        val maxCount = if (targetsCount < spansCount) targetsCount else spansCount
        val applySpannableString = SpannableString(base)
        for (applySpanIndex in 0 until maxCount) {
            val target = targets[applySpanIndex]
            if (target.isNullOrEmpty()) {
                continue
            }
            val spanObject = spans[applySpanIndex] ?: continue
            val targetPattern = Pattern.compile(target)
            val targetMatcher = targetPattern.matcher(base)
            var stop = false
            do {
                if (targetMatcher.find()) {
                    val span = spanObject.span ?: continue
                    applySpannableString.setSpan(span, targetMatcher.start(), targetMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    stop = true
                }
            } while (!stop)
        }
        return applySpannableString
    }

    fun applySpans(base: String, targets: Array<String>?, spans: Array<Any?>?): SpannableString {
        if (base.isEmpty()) {
            return SpannableString("")
        }
        if (targets.isNullOrEmpty()) {
            return SpannableString(base)
        }
        if (spans.isNullOrEmpty()) {
            return SpannableString(base)
        }
        val targetsCount = targets.size
        val spansCount = spans.size
        val maxCount = if (targetsCount < spansCount) targetsCount else spansCount
        val applySpannableString = SpannableString(base)
        for (index in 0 until maxCount) {
            val target = targets[index]
            if (target.isEmpty()) {
                continue
            }
            val span = spans[index] ?: continue
            val targetStartIndex = base.indexOf(target)
            if (0 <= targetStartIndex) {
                applySpannableString.setSpan(
                    span,
                    targetStartIndex, targetStartIndex + target.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
        return applySpannableString
    }

    fun applyStyleSpan(base: String, target: String, style: Int): SpannableString {
        if (base.isEmpty()) {
            return SpannableString("")
        }
        if (target.isEmpty()) {
            return SpannableString(base)
        }
        val applySpannableString = SpannableString(base)
        val targetStartIndex = base.indexOf(target)
        if (0 <= targetStartIndex) {
            applySpannableString.setSpan(
                StyleSpan(style),
                targetStartIndex, targetStartIndex + target.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return applySpannableString
    }

    /**
     *
     * @param base base string
     * @param target target string
     * @param style span style
     * @param flags span flag
     * @return spannable string
     */
    fun applyStyleSpan(base: String, target: String, style: Int, flags: Int): SpannableString {
        if (base.isEmpty()) {
            return SpannableString("")
        }
        if (target.isEmpty()) {
            return SpannableString(base)
        }
        val applySpannableString = SpannableString(base)
        val targetStartIndex = base.indexOf(target)
        if (0 <= targetStartIndex) {
            applySpannableString.setSpan(
                StyleSpan(style),
                targetStartIndex, targetStartIndex + target.length,
                flags
            )
        }
        return applySpannableString
    }

    fun applyAbsoluteSizeSpan(base: String, target: String, size: Int): SpannableString {
        if (base.isEmpty()) {
            return SpannableString("")
        }
        if (target.isEmpty()) {
            return SpannableString(base)
        }
        val applySpannableString = SpannableString(base)
        val targetStartIndex = base.indexOf(target)
        if (0 <= targetStartIndex) {
            applySpannableString.setSpan(
                AbsoluteSizeSpan(size),
                targetStartIndex, targetStartIndex + target.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return applySpannableString
    }

    fun applyForegroundColorSpan(base: String, target: String, color: Int): SpannableString {
        if (base.isEmpty()) {
            return SpannableString("")
        }
        if (target.isEmpty()) {
            return SpannableString(base)
        }
        val applySpannableString = SpannableString(base)
        val targetStartIndex = base.indexOf(target)
        if (0 <= targetStartIndex) {
            applySpannableString.setSpan(
                ForegroundColorSpan(color),
                targetStartIndex, targetStartIndex + target.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return applySpannableString
    }

    @JvmOverloads
    fun applyImageSpan(base: String, target: String, drawable: Drawable?, align: Int = ImageSpan.ALIGN_BASELINE): SpannableString {
        if (base.isEmpty()) {
            return SpannableString("")
        }
        if (target.isEmpty()) {
            return SpannableString(base)
        }
        if (null == drawable) {
            return SpannableString(base)
        }
        drawable.setBounds(0, 0, drawable.intrinsicWidth - 1, drawable.intrinsicHeight - 1)
        val applySpannableString = SpannableString(base)
        val targetStartIndex = base.indexOf(target)
        if (0 <= targetStartIndex) {
            applySpannableString.setSpan(
                ImageSpan(drawable, align),
                targetStartIndex, targetStartIndex + target.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return applySpannableString
    }

    fun applyCenteredImageSpan(base: String, target: String, drawable: Drawable?): SpannableString {
        if (base.isEmpty()) {
            return SpannableString("")
        }
        if (target.isEmpty()) {
            return SpannableString(base)
        }
        if (null == drawable) {
            return SpannableString(base)
        }
        drawable.setBounds(0, 0, drawable.intrinsicWidth - 1, drawable.intrinsicHeight - 1)
        val applySpannableString = SpannableString(base)
        val targetStartIndex = base.indexOf(target)
        if (0 <= targetStartIndex) {
            applySpannableString.setSpan(
                object : ImageSpan(drawable, ALIGN_BOTTOM) {
                    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
                        val b = getDrawable()
                        canvas.save()
                        var transY = bottom - b.bounds.bottom
                        // this is the key
                        transY -= paint.fontMetricsInt.descent / 2
                        canvas.translate(x, transY.toFloat())
                        b.draw(canvas)
                        canvas.restore()
                    }
                },
                targetStartIndex, targetStartIndex + target.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return applySpannableString
    }

    @JvmOverloads
    fun justifyText(textView: TextView?, fitAll: Boolean = false) {
        if (null == textView) {
            return
        }
        val layoutParams = textView.layoutParams
        if (null == layoutParams || ViewGroup.LayoutParams.WRAP_CONTENT == layoutParams.width) {
            return
        }
        val textString = textView.text.toString()
        if (textString.isEmpty()) {
            return
        }
        val isJustify = AtomicBoolean(false)
        val builder = SpannableStringBuilder()
        val textPaint = textView.paint
        textView.post(
            Runnable {
                if (!isJustify.get()) {
                    val lineCount = textView.lineCount
                    val textViewWidth = textView.width
                    for (i in 0 until lineCount) {
                        val lineStart = textView.layout.getLineStart(i)
                        val lineEnd = textView.layout.getLineEnd(i)
                        val lineString = textString.substring(lineStart, lineEnd)
                        if (!fitAll && i == lineCount - 1) {
                            builder.append(SpannableString(lineString))
                            break
                        }
                        val trimSpaceText = lineString.trim { it <= ' ' }
                        val removeSpaceText = lineString.replace(" ".toRegex(), "")
                        val removeSpaceWidth = textPaint.measureText(removeSpaceText)
                        val spaceCount = (trimSpaceText.length - removeSpaceText.length).toFloat()
                        val eachSpaceWidth = (textViewWidth - removeSpaceWidth) / spaceCount
                        val spannableString = SpannableString(lineString)
                        for (j in trimSpaceText.indices) {
                            val c = trimSpaceText[j]
                            if (c == ' ') {
                                val drawable: Drawable = ColorDrawable(0x00ffffff)
                                drawable.setBounds(0, 0, eachSpaceWidth.toInt(), 0)
                                val span = ImageSpan(drawable)
                                spannableString.setSpan(span, j, j + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                            }
                        }
                        builder.append(spannableString)
                    }
                    textView.text = builder
                    isJustify.set(true)
                }
            }
        )
    }

    /**
     * 지정한 방식 (MD5 or SHA-256) 대로 원본 문자열을 암호화함
     *
     * @param source        암호화할 원본 문자열
     * @param encryptType   암호화 방식 ("MD5" or "SHA-256")
     * @return              암호화된 hash string
     */
    fun encryptString(source: String, encryptType: String?): String {
        if (source.isEmpty() || encryptType.isNullOrEmpty()) {
            return source
        }
        try {
            val md = MessageDigest.getInstance(encryptType)
            md.update(source.toByteArray())
            val hash = md.digest()
            val hexString = StringBuilder()
            for (aHash in hash) {
                if (0xff and aHash.toInt() < 0x10) {
                    hexString.append("0").append(Integer.toHexString(0xFF and aHash.toInt()))
                } else {
                    hexString.append(Integer.toHexString(0xFF and aHash.toInt()))
                }
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun loadJSONStringFromInputStream(inputStream: InputStream?): String? {
        if (null == inputStream) {
            return null
        }

        // create jsonobject from file
        try {
            val size = inputStream.available()
            if (0 < size) {
                val buffer = ByteArray(size)
                /*val read = */inputStream.read(buffer)
                //                Trace.d("Read counts = " + read);
                return String(buffer, Charsets.UTF_8)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return null
    }

    fun getFormattedPhoneNumber(number: String): String {
        if (number.isEmpty()) {
            return ""
        }
        val phoneNumber = number.replace(regex = "[^0-9]".toRegex(), "")
        return PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country)
    }

    fun fromHtml(source: String?): Spanned {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    }

    fun composeStringByDivider(divider: String?, vararg args: String?): String {
        val sb = StringBuilder()
        for (arg in args) {
            if (arg.isNullOrEmpty()) {
                continue
            }
            if (sb.isNotEmpty()) {
                sb.append(divider)
            }
            sb.append(arg)
        }
        return sb.toString()
    }

    fun equals(s1: String?, s2: String?): Boolean {
        return s1 == null && s2 == null || s1 != null && s1 == s2
    }

    fun getUrlParameters(parameters: String): Map<String, String> {
        val data: MutableMap<String, String> = HashMap()
        if (parameters.isEmpty()) {
            return HashMap()
        }
        for (q in parameters.split("&").toTypedArray()) {
            try {
                val qa = q.split("=").toTypedArray()
                val name = URLDecoder.decode(qa[0], "UTF-8")
                var value = ""
                if (qa.size == 2) {
                    value = URLDecoder.decode(qa[1], "UTF-8")
                }
                data[name] = value
            } catch (e: Exception) {
                // skip
            }
        }
        return data
    }

    fun getUrlParameter(parameter: String, key: String): String? {
        if (key.isEmpty()) {
            return null
        }
        val data = getUrlParameters(parameter)
        return data[key]
    }

    fun countMatches(str: String, sub: String): Int {
        if (str.isEmpty() || sub.isEmpty()) {
            return 0
        }
        var count = 0
        var idx = 0
        while (str.indexOf(sub, idx).also { idx = it } != -1) {
            count++
            idx += sub.length
        }
        return count
    }

    fun countMatches(str: String, sub: String, start: Int, end: Int): Int {
        if (str.isEmpty() || sub.isEmpty()) {
            return 0
        }
        var count = 0
        var idx = 0
        val targetString = str.substring(start, end)
        if (targetString.isEmpty()) {
            return 0
        }
        while (targetString.indexOf(sub, idx).also { idx = it } != -1) {
            count++
            idx += sub.length
        }
        return count
    }

    fun removeCharAt(s: String?, pos: Int): String? {
        return if (s.isNullOrEmpty()) {
            s
        } else "" + s.substring(0, pos) + s.substring(pos + 1)
    }

    fun removeCharAt(s: String, start: Int, end: Int): String {
        if (s.isEmpty()) {
            return s
        }
        return if (0 <= start && end <= s.length) {
            "" + s.substring(0, start) + s.substring(end)
        } else s
    }

    fun getEndIndexOf(src: String, des: String, excludes: CharArray?): Int {
        if (src.isEmpty()) {
            return -1
        }
        if (des.isEmpty()) {
            return -1
        }
        if (null != excludes) {
            var tempSrc = src
            var tempDes = des
            for (ch in excludes) {
                tempSrc = tempSrc.replace("" + ch.toString().toRegex(), "")
                tempDes = tempDes.replace("" + ch.toString().toRegex(), "")
            }
            if (tempSrc.length < tempDes.length) {
                return -1
            }
        }
        var desIndex = 0
        var srcIndex = 0
        while (srcIndex < src.length) {
            val ch = src[srcIndex]
            if (null != excludes) {
                var skip = false
                for (exclude in excludes) {
                    if (ch == exclude) {
                        skip = true
                        break
                    }
                }
                if (skip) {
                    srcIndex++
                    continue
                }
            }
            if (desIndex >= des.length) {
                break
            }
            if (ch != des[desIndex]) {
                return -1
            }
            desIndex++
            srcIndex++
        }
        return srcIndex - 1
    }

    fun convertDistanceToString(distance: Long): String {
        return if (1000 <= distance) {
            String.format(Locale.getDefault(), "%dkm", distance / 1000)
        } else {
            String.format(Locale.getDefault(), "%dm", distance)
        }
    }

    fun convertDistanceToString(distance: Double): String {
        return if (1000 <= distance) {
            String.format(Locale.getDefault(), "%,.1fkm", distance / 1000)
        } else {
            String.format(Locale.getDefault(), "%dm", distance.toInt())
        }
    }

    fun <M> convertToList(string: String, split: String?, converter: ItemConverter<M>): ArrayList<M> {
        val result = ArrayList<M>()
        if (string.isEmpty()) {
            return result
        }
        val strings: MutableList<String>
        if (split.isNullOrEmpty()) {
            strings = ArrayList()
            strings.add(string)
        } else {
            strings = ArrayList(mutableListOf(*string.split(split).toTypedArray()))
        }
        for (s in strings) {
            val item: M? = converter.convert(s)
            if (null != item) {
                result.add(item)
            }
        }
        return result
    }

    // converts UTF-8 to Java String format
    fun convertUTF8ToString(s: String): String {
        return if (s.isEmpty()) {
            s
        } else try {
            String(s.toByteArray(Charsets.UTF_8))
        } catch (e: UnsupportedEncodingException) {
            s
        }
    }

    // converts Java String format to UTF-8
    fun convertStringToUTF8(s: String): String {
        return if (s.isEmpty()) {
            s
        } else try {
            String(s.toByteArray(), Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            s
        }
    }

    fun getBetweenValue(src: String, start: String, end: String?): String {
        if (src.isEmpty() || start.isEmpty()) {
            return ""
        }
        val startIndex = src.indexOf(start)
        if (startIndex < 0) {
            return ""
        }
        val subSrc = src.substring(startIndex + start.length)
        if (end.isNullOrEmpty()) {
            return subSrc
        }
        val endIndex = subSrc.indexOf(end)
        return if (endIndex < 0) {
            subSrc
        } else subSrc.substring(0, endIndex)
    }

    fun hasRestrictCharacterTypes(source: String?, vararg restrictCharTypes: Int): Boolean {
        if (null == source) {
            return false
        }
        for (element in source) {
            val type = Character.getType(element)
            for (restrictCharType in restrictCharTypes) {
                if (restrictCharType == type) {
                    return true
                }
            }
        }
        return false
    }

    fun removeRestrictCharacterTypes(source: String, vararg restrictCharTypes: Int): String {
        if (source.isEmpty()) {
            return source
        }
        val stringBuilder = StringBuilder()
        for (element in source) {
            val type = Character.getType(element)
            var isRestrictChar = false
            for (restrictCharType in restrictCharTypes) {
                if (restrictCharType == type) {
                    isRestrictChar = true
                    break
                }
            }
            if (!isRestrictChar) {
                stringBuilder.append(element)
            }
        }
        return stringBuilder.toString()
    }

    fun splitNumber(number: Int): IntArray {
        var calNumber = number
        var digitCounts = (kotlin.math.log10(calNumber.toDouble()) + 1).toInt()
        if (calNumber < 0) {
            digitCounts--
        }
        val result = IntArray(digitCounts)
        while (digitCounts-- > 0) {
            result[digitCounts] = calNumber % 10
            calNumber /= 10
        }
        return result
    }

    fun getNumberDigitCounts(number: Int): Int {
        return if (0 == number) {
            1
        } else (kotlin.math.log10(number.toDouble()) + 1).toInt()
    }

    private fun getTenPower(n: Int): Int {
        return 10.toDouble().pow(n.toDouble()).toInt()
    }

    fun getNumberDigit(number: Int, unitOfDigit: Int): Int {
        var digit = 0
        val targetNumber = kotlin.math.abs(number)
        val digitCounts = getNumberDigitCounts(targetNumber)
        if (unitOfDigit in 1..digitCounts) {
            val calculateValue = targetNumber % getTenPower(unitOfDigit)
            digit = calculateValue / getTenPower(unitOfDigit - 1)
        }
        return digit
    }

    fun isNegativeSignValue(number: Int): Boolean {
        return 0 > number
    }

    fun getMapAddress(jibun: String?, road: String?, detail: String?): String {
        val address: String? = if (!jibun.isNullOrEmpty()) {
            jibun
        } else {
            road
        }
        return composeStringByDivider(" ", address, detail)
    }

    fun trim(string: String?): String {
        return string?.trim { it <= ' ' } ?: ""
    }

    @Throws(UnsupportedEncodingException::class)
    fun encodeURLEncoderWithSpaceToPercent(s: String?, enc: String?): String {
        return URLEncoder.encode(s, enc).replace("\\+".toRegex(), "%20")
    }

    fun floorDouble(`val`: Double, pointCounts: Int): Double {
        val bigDecimal = BigDecimal.valueOf(`val`)
        var scale = pointCounts
        if (scale > bigDecimal.scale()) {
            scale = bigDecimal.scale()
        } else if (scale < 0) {
            scale = 0
        }
        return BigDecimal.valueOf(`val`).setScale(scale, RoundingMode.FLOOR).toDouble()
    }

    interface ItemConverter<M> {
        fun convert(item: String?): M
    }
}