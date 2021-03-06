package com.dmitrykazanbaev.virus_game.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.RadioGroup
import android.widget.RelativeLayout
import com.dmitrykazanbaev.virus_game.AbstractLevelActivity
import com.dmitrykazanbaev.virus_game.R
import com.dmitrykazanbaev.virus_game.custom_views.ModificationButton
import java.util.*


class ModificationButtonController
@JvmOverloads constructor(context: Context,
                          attrs: AttributeSet? = null,
                          sourceViewId: Int = 0) : RadioGroup(context, attrs) {

    val center by lazy { Point(height / 2, height / 2) }

    private val sectorSeparatorPaint = Paint()

    private var modificationButtons: List<ModificationButton> = when (sourceViewId) {
        R.id.devices_button -> createDevicesButtonList()
        R.id.propagation_button -> createPropagationButtonList()
        R.id.abilities_button -> createAbilitiesButtonList()
        R.id.resistance_button -> createResistanceButtonList()
        else -> emptyList()
    }

    private val numbers by lazy { Numbers() }

    inner class Numbers {
        val textInCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        var textLayouts: MutableList<StaticLayout> = mutableListOf()

        var offsets: MutableList<Point> = mutableListOf()
            get() {
                val resOffsets = mutableListOf<Point>()
                (-6..4 step 2).mapTo(resOffsets) { Point((center.x + it / 7f * (height / 4f) + height / 50f).toInt(), 0) }
                var index = 0
                (-4 downTo -6).forEach { resOffsets[index++].y = (center.y + it / 6f * (height / 4)).toInt() }
                (-6..-4).forEach { resOffsets[index++].y = (center.y + it / 6f * (height / 4)).toInt() }
                return resOffsets
            }

        init {
            textInCenterPaint.color = Color.WHITE
            textInCenterPaint.typeface = FontCache.getTypeface("DINPro/DINPro.otf", context)
            textInCenterPaint.style = Paint.Style.FILL
        }

        fun getStaticLayouts(canvas: Canvas?): MutableList<StaticLayout> {
            val textPaint = TextPaint(textInCenterPaint)
            val layouts = mutableListOf<StaticLayout>()

            canvas?.let {
                layouts.add(StaticLayout(getRandomHexTextWithCountLetters(4), textPaint,
                        canvas.width, Layout.Alignment.ALIGN_NORMAL,
                        1.0f, 0.0f, false))

                layouts.add(StaticLayout(getRandomHexTextWithCountLetters(5), textPaint,
                        canvas.width, Layout.Alignment.ALIGN_NORMAL,
                        1.0f, 0.0f, false))

                layouts.add(StaticLayout(getRandomHexTextWithCountLetters(6), textPaint,
                        canvas.width, Layout.Alignment.ALIGN_NORMAL,
                        1.0f, 0.0f, false))

                layouts.add(StaticLayout(getRandomHexTextWithCountLetters(6), textPaint,
                        canvas.width, Layout.Alignment.ALIGN_NORMAL,
                        1.0f, 0.0f, false))

                layouts.add(StaticLayout(getRandomHexTextWithCountLetters(5), textPaint,
                        canvas.width, Layout.Alignment.ALIGN_NORMAL,
                        1.0f, 0.0f, false))

                layouts.add(StaticLayout(getRandomHexTextWithCountLetters(4), textPaint,
                        canvas.width, Layout.Alignment.ALIGN_NORMAL,
                        1.0f, 0.0f, false))
            }

            return layouts
        }

        private fun getRandomHexTextWithCountLetters(count: Int): String {
            val source = "0123456789ABCDEF"
            var result = ""
            val random = Random()
            for (i in 0 until count) {
                if (i != 0) result += "\n"
                result += source[random.nextInt(source.length)]
            }

            return result
        }
    }

    init {
        modificationButtons.forEach { addView(it) }

        sectorSeparatorPaint.color = Color.BLACK
        sectorSeparatorPaint.isAntiAlias = true
        sectorSeparatorPaint.strokeWidth = resources.getString(R.dimen.stroke_separatorSector).toFloat()
        sectorSeparatorPaint.style = Paint.Style.STROKE
    }

    fun updateCenterWithX(x: Int) {
        center.x = x
        (0 until childCount)
                .map { getChildAt(it) as ModificationButton }
                .forEach {
                    it.center.x = x
                    it.invalidate()
                }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        modificationButtons.forEach {
            it.layout(0, 0, width, height)
            it.draw(canvas)
        }

        modificationButtons.forEach {
            val sectorSeparatorLine =
                    getSectorSeparatorLine(it.innerRadius, it.outerRadius,
                            it.startAngle, it.sweepAngle)

            canvas?.drawLine(sectorSeparatorLine[0].x.toFloat(),
                    sectorSeparatorLine[0].y.toFloat(),
                    sectorSeparatorLine[1].x.toFloat(),
                    sectorSeparatorLine[1].y.toFloat(), sectorSeparatorPaint)
        }

        drawNumbers(canvas)
    }

    private fun drawNumbers(canvas: Canvas?) {
        numbers.textInCenterPaint.textSize = height / 15f

        if (numbers.textLayouts.isEmpty())
            numbers.textLayouts = numbers.getStaticLayouts(canvas)

        numbers.textLayouts.forEachIndexed { index, textLayout ->
            canvas?.save()

            canvas?.translate(numbers.offsets[index].x.toFloat(), numbers.offsets[index].y.toFloat())
            textLayout.draw(canvas)

            canvas?.restore()
        }
    }

    private fun getSectorSeparatorLine(innerRadius: Float, outerRadius: Float,
                                       startAngle: Float, sweepAngle: Float): MutableList<Point> {
        val startX = center.x + innerRadius * Math.cos((startAngle + sweepAngle) * Math.PI / 180)
        val startY = center.y + innerRadius * Math.sin((startAngle + sweepAngle) * Math.PI / 180)
        val stopX = center.x + outerRadius * Math.cos((startAngle + sweepAngle) * Math.PI / 180)
        val stopY = center.y + outerRadius * Math.sin((startAngle + sweepAngle) * Math.PI / 180)

        return mutableListOf(Point(startX.toInt(), startY.toInt()), Point(stopX.toInt(), stopY.toInt()))
    }


    private fun createPropagationButtonList(): List<ModificationButton> {
        val levelActivity = context as AbstractLevelActivity
        val buttonList = createModificationButtonsWithListener(4)

        buttonList[0].tag = "wifi"
        buttonList[0].icon = ContextCompat.getDrawable(context, R.mipmap.wifi)
        buttonList[0].modificationLevel = levelActivity.user.virus.propagation.wifi.currentLevel

        buttonList[1].tag = "bluetooth"
        buttonList[1].icon = ContextCompat.getDrawable(context, R.mipmap.bluetooth)
        buttonList[1].modificationLevel = levelActivity.user.virus.propagation.bluetooth.currentLevel

        buttonList[2].tag = "ethernet"
        buttonList[2].icon = ContextCompat.getDrawable(context, R.mipmap.ethernet)
        buttonList[2].modificationLevel = levelActivity.user.virus.propagation.ethernet.currentLevel

        buttonList[3].tag = "mobile"
        buttonList[3].icon = ContextCompat.getDrawable(context, R.mipmap.mobile)
        buttonList[3].modificationLevel = levelActivity.user.virus.propagation.mobile.currentLevel

        return buttonList
    }

    private fun createAbilitiesButtonList(): List<ModificationButton> {
        val levelActivity = context as AbstractLevelActivity
        val buttonList = createModificationButtonsWithListener(3)

        buttonList[0].tag = "thief"
        buttonList[0].icon = ContextCompat.getDrawable(context, R.mipmap.thief)
        buttonList[0].modificationLevel = levelActivity.user.virus.abilities.thief.currentLevel

        buttonList[1].tag = "control"
        buttonList[1].icon = ContextCompat.getDrawable(context, R.mipmap.control)
        buttonList[1].modificationLevel = levelActivity.user.virus.abilities.control.currentLevel

        buttonList[2].tag = "spam"
        buttonList[2].icon = ContextCompat.getDrawable(context, R.mipmap.spam)
        buttonList[2].modificationLevel = levelActivity.user.virus.abilities.spam.currentLevel

        return buttonList
    }

    private fun createResistanceButtonList(): List<ModificationButton> {
        val levelActivity = context as AbstractLevelActivity
        val buttonList = createModificationButtonsWithListener(3)

        buttonList[0].tag = "mask"
        buttonList[0].icon = ContextCompat.getDrawable(context, R.mipmap.mask)
        buttonList[0].modificationLevel = levelActivity.user.virus.resistance.mask.currentLevel

        buttonList[1].tag = "invisible"
        buttonList[1].icon = ContextCompat.getDrawable(context, R.mipmap.invisible)
        buttonList[1].modificationLevel = levelActivity.user.virus.resistance.invisible.currentLevel

        buttonList[2].tag = "new_virus"
        buttonList[2].icon = ContextCompat.getDrawable(context, R.mipmap.new_virus)
        buttonList[2].modificationLevel = levelActivity.user.virus.resistance.newVirus.currentLevel

        return buttonList
    }

    private fun createDevicesButtonList(): List<ModificationButton> {
        val levelActivity = context as AbstractLevelActivity
        val buttonList = createModificationButtonsWithListener(3)

        buttonList[0].tag = "phone"
        buttonList[0].icon = ContextCompat.getDrawable(context, R.mipmap.phone)
        buttonList[0].modificationLevel = levelActivity.user.virus.devices.phone.currentLevel

        buttonList[1].tag = "pc"
        buttonList[1].icon = ContextCompat.getDrawable(context, R.mipmap.pc)
        buttonList[1].modificationLevel = levelActivity.user.virus.devices.pc.currentLevel

        buttonList[2].tag = "smart_home"
        buttonList[2].icon = ContextCompat.getDrawable(context, R.mipmap.smart_home)
        buttonList[2].modificationLevel = levelActivity.user.virus.devices.smartHome.currentLevel

        return buttonList
    }

    private fun createModificationButtonsWithListener(countButtons: Int): MutableList<ModificationButton> {
        val buttonList = mutableListOf<ModificationButton>()

        val sectorPartDegree = 360f / countButtons
        var startAngle = when (countButtons) {
            3 -> -90f
            4 -> -45f
            else -> 0f
        }

        repeat(countButtons) {
            val button = createModificationButton(startAngle, sectorPartDegree)
            button.setOnClickListener { onModificationButtonTouch(it) }
            buttonList.add(button)

            startAngle += sectorPartDegree
        }

        return buttonList
    }


    private fun onModificationButtonTouch(view: View) {
        val firstLevelActivity = context as AbstractLevelActivity
        with(firstLevelActivity) {
            when (view.tag) {
                "wifi" -> updateModificationWindow(user.virus.propagation.wifi)
                "bluetooth" -> updateModificationWindow(user.virus.propagation.bluetooth)
                "ethernet" -> updateModificationWindow(user.virus.propagation.ethernet)
                "mobile" -> updateModificationWindow(user.virus.propagation.mobile)
                "thief" -> updateModificationWindow(user.virus.abilities.thief)
                "control" -> updateModificationWindow(user.virus.abilities.control)
                "spam" -> updateModificationWindow(user.virus.abilities.spam)
                "invisible" -> updateModificationWindow(user.virus.resistance.invisible)
                "mask" -> updateModificationWindow(user.virus.resistance.mask)
                "new_virus" -> updateModificationWindow(user.virus.resistance.newVirus)
                "phone" -> updateModificationWindow(user.virus.devices.phone)
                "pc" -> updateModificationWindow(user.virus.devices.pc)
                "smart_home" -> updateModificationWindow(user.virus.devices.smartHome)

                else -> return
            }
        }
    }

    private fun createModificationButton(startAngle: Float, sweepAngle: Float): ModificationButton {
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)

        val button = ModificationButton(context,
                startAngle = startAngle, sweepAngle = sweepAngle)

        button.setBackgroundColor(Color.TRANSPARENT)
        button.layoutParams = params

        return button
    }
}