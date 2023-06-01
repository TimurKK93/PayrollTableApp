package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.contains
import androidx.core.view.isEmpty
import androidx.core.view.size
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.math.roundToInt


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity()/*, ExampleDialog.ExampleDialogListener*/ {

    private lateinit var bindingClass: ActivityMainBinding

    var averageValue: Int = 0
    var salaryValue: Int = 0
    var shiftCountValue: Int = 0

    //    var planText: String = ""
    var pref: SharedPreferences? = null
    var gson = Gson()


    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        installSplashScreen()
        super.onCreate(savedInstanceState)
        bindingClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)


        pref = getSharedPreferences("APP", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        bindingClass.textView17.text =
            pref?.getString("total", bindingClass.textView17.text.toString())!!
        bindingClass.textView16.text =
            pref?.getString("total_shift", bindingClass.textView16.text.toString())
        bindingClass.textView5.text =
            pref?.getString("average", bindingClass.textView5.text.toString())!!
        bindingClass.workPlan.text =
            pref?.getString("work_plan", bindingClass.workPlan.text.toString())!!
        bindingClass.textView11.text =
            pref?.getString("non_core", bindingClass.textView11.text.toString())!!
        bindingClass.textView3.text =
            pref?.getString("shift", bindingClass.textView3.text.toString())!!
        bindingClass.textView4.text =
            pref?.getString("salary", bindingClass.textView4.text.toString())!!
//        bindingClass.textInp.text = pref?.getString("plan_text", "")!!

        salaryValue = pref?.getInt("salary_plan_text_num", 0)!!
        shiftCountValue = pref?.getInt("shift_plan_text_num", 0)!!
        averageValue = pref?.getInt("average_plan_value", 0)!!


        //      Чтение данных таблицы
        val jsonText = pref?.getString("column_one", getColumnDataString(0).toString())!!
        val type: Type = object : TypeToken<MutableList<String?>?>() {}.type
        val columnOneData = gson.fromJson<String>(jsonText, type) as MutableList<String>

        for (i in 0 until 16) {
            val rowItems = bindingClass.tb.getChildAt(i) as TableRow
            val cell = rowItems.getChildAt(0) as TextView
            cell.text = columnOneData[i]
        }

        val jsonText2 = pref?.getString("column_two", getColumnDataString(1).toString())!!
        val type2: Type = object : TypeToken<MutableList<String?>?>() {}.type
        val columnTwoData = gson.fromJson<String>(jsonText2, type2) as MutableList<String>

        for (i in 0 until 16) {
            val rowItems = bindingClass.tb.getChildAt(i) as TableRow
            val cell = rowItems.getChildAt(1) as TextView
            cell.text = columnTwoData[i]
        }

        val jsonText3 = pref?.getString("column_three", getColumnDataString(2).toString())!!
        val type3: Type = object : TypeToken<MutableList<String?>?>() {}.type
        val columnThreeData = gson.fromJson<String>(jsonText3, type3) as MutableList<String>

        for (i in 0 until 16) {
            val rowItems = bindingClass.tb.getChildAt(i) as TableRow
            val cell = rowItems.getChildAt(2) as TextView
            cell.text = columnThreeData[i]
        }

        //      Чтение данных таблицы


//        fun openDialog() {
//            val exampleDialog = ExampleDialog()
//            exampleDialog.show(supportFragmentManager, "example dialog")
//        }
//
//        bindingClass.plan.setOnClickListener {
//            openDialog()
//
//        }


        val count = bindingClass.tb.childCount

        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        // Функция очистки таблицы

        fun alertDialog() {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Предупреждение")
            builder.setMessage("Вы уверенны, что хотите очистить таблицу?")
            builder.setPositiveButton("да") { _, _ ->
                for (i in 0 until count) {
                    val rowItems = bindingClass.tb.getChildAt(i) as TableRow
                    for (j in 0 until rowItems.childCount) {
                        val cell = rowItems.getChildAt(j) as TextView
                        cell.text = ""
                        bindingClass.textView17.text = "Итог: 0"
                        bindingClass.textView16.text = "0 ч."
                        bindingClass.textView11.text = "0 ч."
                        bindingClass.textView3.text = "Cмен: 0"
                        bindingClass.workPlan.text = ""
                        bindingClass.textView5.text = "Среднее: 0"
                        bindingClass.textView4.text = "Заработанно: 0 ₽"

                        editor?.remove("total")
                        editor?.remove("total_shift")
                        editor?.remove("average")
                        editor?.remove("work_plan")
                        editor?.remove("non_core")
                        editor?.remove("shift")
                        editor?.remove("salary")
                        editor?.remove("column_one")
                        editor?.remove("column_two")
                        editor?.remove("column_three")
                        editor?.apply()


//                        if (averageValue != 0) {
//                            bindingClass.textInp.text =
//                                "Придерживайтесь сбора $averageValue позиций"
//                            editor?.putString("plan_text", bindingClass.textInp.text.toString())
//                            editor?.apply()
//                        }

                    }
                }
            }
            builder.setNegativeButton("нет") { _, _ ->
            }
            builder.show()
        }


        fun getData(): MutableList<String> {
            val allData = mutableListOf<String>()
            for (i in 0 until count) {
                val rowItems = bindingClass.tb.getChildAt(i) as TableRow
                for (j in 0 until rowItems.childCount) {
                    val cell = rowItems.getChildAt(j) as TextView
                    if (cell.text.toString() != "") allData.add(cell.text.toString())
                }
            }
            return allData
        }


        bindingClass.clearBtn.setOnClickListener {
            if (getData().size != 0) {
                alertDialog()
            }
        }


        //перебрать все элементы Table Layout
        for (i in 0 until count) {
            val rowItems = bindingClass.tb.getChildAt(i) as TableRow
            for (j in 0 until rowItems.childCount) {
                val cell = rowItems.getChildAt(j) as TextView
                cell.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        cell.clearFocus()
                        cell.hideKeyboard()


                        val columnOne = getColumnData(0)
                        val columnTwo = getColumnData(1)
                        val columnThree = getColumnData(2)

                        val newData = columnOne
                        val newData2 = columnTwo
                        val newData3 = columnThree

                        var rowCount = 0
                        for (i in 0 until count) {
                            val row = bindingClass.tb.getChildAt(i) as? TableRow
                            if (row != null && row.childCount == 3) {
                                // Check if both columns have data
                                val column1 = (row.getChildAt(0) as? TextView)?.text?.toString()
                                val column2 = (row.getChildAt(1) as? TextView)?.text?.toString()
                                val column3 = (row.getChildAt(2) as? TextView)?.text?.toString()
                                if (!column1.isNullOrEmpty() || !column2.isNullOrEmpty() || !column3.isNullOrEmpty()) {
                                    rowCount++
                                }
                            }
                        }

// ------------------------------------------- Считаем зарплату-----------------------------------------
                        var salaryText: Int
                        var salaryPercent = 0
                        var meter = 0

//----------------------------------------Убираем лишний час если указанно больше непрофиля----------------------------------
                        for (i in 0 until count) {
                            val row = bindingClass.tb.getChildAt(i) as? TableRow
                            val cell1 =
                                row?.getChildAt(1) as? TextView // assuming first column contains TextViews
                            val cell2 =
                                row?.getChildAt(2) as? TextView // assuming second column contains TextViews

                            // check if cells 1 and 2 are not null or empty
                            if (cell1?.text?.isNotEmpty() == true && cell2?.text?.isNotEmpty() == true) {
                                val sum = cell1.text.toString().toInt() + cell2.text.toString()
                                    .toInt() // calculate sum of cells 1 and 2
                                if (sum >= 12) {
                                    meter++ // increment meter if sum is greater than 12
                                }
                            }
                        }
//----------------------------------------Убираем лишний час если указанно больше непрофиля----------------------------------

                        if ((newData.sum() != 0) && (newData2.sum() != 0)) {
                            if (newData.sum() / newData2.sum() >= 80) {
                                salaryText =
                                    (((newData.sum() * 2 + newData2.sum() * 80 + newData3.sum() * 180 - meter * 80) * 1.2 * 0.13).roundToInt())
                                salaryPercent =
                                    ((((newData.sum() * 2 + newData2.sum() * 80 + newData3.sum() * 180 - meter * 80) * 1.2) - salaryText).roundToInt())
                            } else {
                                salaryText =
                                    (((newData.sum() * 1.8) + newData2.sum() * 80 + newData3.sum() * 180 - meter * 80) * 1.2 * 0.13).roundToInt()
                                salaryPercent =
                                    ((((newData.sum() * 1.8) + newData2.sum() * 80 + newData3.sum() * 180 - meter * 80) * 1.2) - salaryText).roundToInt()
                            }
                            if (newData.sum() / newData2.sum() >= 80) {
                                bindingClass.workPlan.text =
                                    "Опережаете план на ${newData.sum() - newData2.sum() * 80} позиций."
                            } else {
                                bindingClass.workPlan.text =
                                    "Отстаете от плана на ${newData2.sum() * 80 - newData.sum()} позиций."
                            }


                        } else {
                            salaryText =
                                ((newData3.sum() * 180 - meter * 80) * 1.2 * 0.13).roundToInt()
                            salaryPercent =
                                (((newData3.sum() * 180 - meter * 80) * 1.2) - salaryText).roundToInt()

                            bindingClass.workPlan.text = ""
                        }


// ------------------------------------------- Считаем зарплату-----------------------------------------


                        bindingClass.textView17.text = "Итог: ${columnOne.sum()}"
                        bindingClass.textView16.text = "${columnTwo.sum()} ч."
                        bindingClass.textView11.text = "${columnThree.sum()} ч."
                        bindingClass.textView3.text = "Смен: ${rowCount}"


                        // Разбивка числа на разряды
                        val salary = String.format(Locale.CANADA_FRENCH, "%,d", salaryPercent)
                        bindingClass.textView4.text = "Заработанно: $salary ₽"

// ------------------------------------------- Считаем среднее-----------------------------------------
                        if ((newData.sum() != 0) && (newData2.sum() != 0)) {
                            bindingClass.textView5.text =
                                "Среднее: ${newData.sum() / newData.size}"
                        } else {
                            bindingClass.textView5.text = "Среднеe: 0"
                        }
// ------------------------------------------- Считаем среднее-----------------------------------------


//                        if (shiftCountValue.toString().toInt() != 0 && salaryValue.toString()
//                                .toInt() != 0
//                        ) {
//
//
//
//
//                            if (salaryPercent >= salaryValue) {
//                                planText = "план выполнен"
//                            } else {
//                                planText = "план не выполнен"
//                            }
//
//
//
//                            if (shiftCountValue > newData.size) {
//                                var diffPosition: Int
//                                var newAverage: Int
//                                if (averageValue * newData.size > newData.sum()) {
//                                    if (shiftCountValue - newData.size != 0) {
//                                        diffPosition =
//                                            ((newData.size * averageValue - newData.sum()).toFloat() / (shiftCountValue - newData.size)).roundToInt()
//                                    } else {
//                                        diffPosition =
//                                            (newData.size * averageValue - newData.sum())
//                                    }
//                                    val newAverage = averageValue + diffPosition
//
//
//                                    if (shiftCountValue != newData.size) {
//
//                                        bindingClass.textInp.text =
//                                            "Придерживайтесь сбора $newAverage позиций"
//                                    } else {
//                                        bindingClass.textInp.text =
//                                            "Рассчет плана закончен, $planText"
//                                    }
//                                } else {
//
//                                    if (shiftCountValue - newData.size != 0) {
//                                        diffPosition =
//                                            ((newData.sum() - newData.size * averageValue).toFloat() / (shiftCountValue - newData.size)).roundToInt()
//                                    } else {
//                                        diffPosition =
//                                            (newData.sum() - newData.size * averageValue)
//                                    }
//
//                                    if (averageValue > diffPosition) {
//                                        newAverage = averageValue - diffPosition
//                                    } else {
//                                        newAverage = diffPosition - averageValue
//                                    }
//
//
//                                    if (shiftCountValue != newData.size) {
//                                        if (salaryPercent < salaryValue) {
//                                            bindingClass.textInp.text =
//                                                "Придерживайтесь сбора $newAverage позиций"
//                                        } else {
//                                            bindingClass.textInp.text = "План выполнен досрочно"
//                                        }
//                                    }
//                                }
//                            } else {
//                                bindingClass.textInp.text = "Рассчет плана закончен, $planText"
//                            }
//
//-------------------------------------сохраняем показатели--------------------------------------------------
                        editor?.putString("total", bindingClass.textView17.text.toString())
                        editor?.putString("average", bindingClass.textView5.text.toString())
                        editor?.putString(
                            "non_core", bindingClass.textView11.text.toString()
                        )
                        editor?.putString("shift", bindingClass.textView3.text.toString())
                        editor?.putString("salary", bindingClass.textView4.text.toString())
                        editor?.putString(
                            "total_shift", bindingClass.textView16.text.toString()
                        )
                        editor?.putString("work_plan", bindingClass.workPlan.text.toString())

                        //                          сохраняем таблицу
                        editor?.putString("column_one", gson.toJson(getColumnDataString(0)))
                        editor?.putString("column_two", gson.toJson(getColumnDataString(1)))
                        editor?.putString(
                            "column_three", gson.toJson(getColumnDataString(2))
                        )

                        //                 cохраняем текст результата плана
//                        editor?.putString("plan_text", bindingClass.textInp.text.toString())
                        editor?.apply()

                        return@OnEditorActionListener true
                    }
                    false
                })
            }
        }
    }


//    override fun applyTexts(salary: String?, shiftCount: String?) {
//
//
//        val reversePercentage = salary.toString().toFloat() / 87
//        val newReversePercentage = reversePercentage * 13
//        val newSalary = salary.toString().toInt() + newReversePercentage
//        val average = ceil(
//            (newSalary / 1.2 - shiftCount.toString().toInt() * 880) / shiftCount.toString()
//                .toInt() / 1.4
//        )
//
//        val columnOne = getColumnData(0)
//        val columnTwo = getColumnData(1)
//        val columnThree = getColumnData(2)
//
//        val newData = columnOne
//        val newData2 = columnTwo
//        val newData3 = columnThree
//
//        val salaryText =
//            ((newData.sum() * 1.4 + newData2.sum() * 100 + newData3.sum() * 80) * 1.2 * 0.13).roundToInt()
//        val salaryPercent =
//            (((newData.sum() * 1.4 + newData2.sum() * 100 + newData3.sum() * 80) * 1.2) - salaryText).roundToInt()
//
//        bindingClass.textInp.text = "Придерживайтесь сбора $average позиций"
//        averageValue = average.toInt()
//        salaryValue = salary.toString().toInt()
//        shiftCountValue = shiftCount.toString().toInt()
//
//
//        if (shiftCountValue.toString().toInt() != 0 && salaryValue.toString().toInt() != 0) {
//            if (salaryPercent >= salaryValue) {
//                planText = "план выполнен"
//            } else {
//                planText = "план не выполнен"
//            }
//
//            if (shiftCountValue > newData.size) {
//                var diffPosition: Int
//                var newAverage: Int
//                if (averageValue * newData.size > newData.sum()) {
//                    if (shiftCountValue - newData.size != 0) {
//                        diffPosition =
//                            ((newData.size * averageValue - newData.sum()).toFloat() / (shiftCountValue - newData.size)).roundToInt()
//                    } else {
//                        diffPosition = (newData.size * averageValue - newData.sum())
//                    }
//                    val newAverage = averageValue + diffPosition
//
//
//                    if (shiftCountValue != newData.size) {
//
//                        bindingClass.textInp.text = "Придерживайтесь сбора $newAverage позиций"
//                    } else {
//                        bindingClass.textInp.text = "Рассчет плана закончен"
//                    }
//                } else {
//                    if (shiftCountValue - newData.size != 0) {
//                        diffPosition =
//                            ((newData.sum() - newData.size * averageValue).toFloat() / (shiftCountValue - newData.size)).roundToInt()
//                    } else {
//                        diffPosition = (newData.sum() - newData.size * averageValue)
//                    }
//                    if (averageValue > diffPosition) {
//                        newAverage = averageValue - diffPosition
//                    } else {
//                        newAverage = diffPosition - averageValue
//                    }
//
//                    if (shiftCountValue != newData.size) {
//                        if (salaryPercent < salaryValue) {
//                            bindingClass.textInp.text =
//                                "Придерживайтесь сбора $newAverage позиций"
//                        } else {
//                            bindingClass.textInp.text = "План выполнен досрочно"
//                        }
//                    }
//                }
//            } else {
//                bindingClass.textInp.text = "Рассчет плана закончен"
//            }
//        }
//
//        val editor = pref?.edit()
//        editor?.putString("salary_plan_text", salary)
//        editor?.putString("shift_plan_text", shiftCount)
//        editor?.putInt("salary_plan_text_num", salaryValue)
//        editor?.putInt("shift_plan_text_num", shiftCountValue)
//        editor?.putInt("average_plan_value", averageValue)
//        editor?.putBoolean("salary_disabled", false)
//        editor?.putBoolean("shift_disabled", false)
//        editor?.putString("plan_text", bindingClass.textInp.text.toString())
//        editor?.apply()
//
//    }


//    override fun applyClearText(clear: String?) {
//        bindingClass.textInp.text = clear
//        salaryValue = 0
//        shiftCountValue = 0
//        averageValue = 0
//        val editor = pref?.edit()
//        editor?.remove("salary_plan_text")
//        editor?.remove("shift_plan_text")
//        editor?.remove("salary_plan_text_num")
//        editor?.remove("shift_plan_text_num")
//        editor?.remove("salary_disabled")
//        editor?.remove("shift_disabled")
//        editor?.remove("plan_text")
//        editor?.remove("average_plan_value")
//        editor?.apply()
//    }


    fun getColumnData(column: Int): MutableList<Int> {
        val data = mutableListOf<Int>()
        val count = bindingClass.tb.childCount
        for (i in 0 until count) {
            val rowItems = bindingClass.tb.getChildAt(i) as TableRow
            val cell = rowItems.getChildAt(column) as TextView
            if (cell.text.isNotEmpty()) {
                data.add(cell.text.toString().toInt())
            }
        }
        return data
    }

    fun getColumnDataString(column: Int): MutableList<String> {
        val data = mutableListOf<String>()
        val count = bindingClass.tb.childCount
        for (i in 0 until count) {
            val rowItems = bindingClass.tb.getChildAt(i) as TableRow
            val cell = rowItems.getChildAt(column) as TextView

            data.add(cell.text.toString())


        }
        return data
    }
}






