package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentTableBinding
import java.util.*
import kotlin.math.roundToInt


class TableFragment : Fragment() {
    private lateinit var binding: FragmentTableBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var salary: Int = 0
    private var average: Int = 0
    private var shifts: Int = 0
    private var positionSum: Int = 0
    private var shiftHourSum: Int = 0
    private var nonProfileHourSum: Int = 0
    private var fragmentKey: String? = null


    fun setFragmentKeys(fragmentKeys: Bundle?) {
        fragmentKey = fragmentKeys.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTableBinding.inflate(inflater, container, false)

        sharedPreferences =
            requireContext().getSharedPreferences(fragmentKey, Context.MODE_PRIVATE)

        loadTableData()
        loadData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tableLayout = binding.tb

        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        val editorActionListener = TextView.OnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                calculate()
                textView.clearFocus()
                textView.hideKeyboard()
                true
            } else {
                false
            }
        }

        // Loop through the table rows and configure the EditText views
        for (i in 0 until tableLayout.childCount) {
            val tableRow = tableLayout.getChildAt(i) as ViewGroup

            for (j in 0 until tableRow.childCount) {
                val editText = tableRow.getChildAt(j) as EditText
                editText.setOnEditorActionListener(editorActionListener)
            }
        }



        fun getAllData(): MutableList<String> {
            val allData = mutableListOf<String>()
            for (i in 0 until binding.tb.childCount) {
                val rowItems = binding.tb.getChildAt(i) as TableRow
                for (j in 0 until rowItems.childCount) {
                    val cell = rowItems.getChildAt(j) as TextView
                    if (cell.text.toString() != "") allData.add(cell.text.toString())
                }
            }
            return allData
        }
        binding.clearBtn.setOnClickListener {
            if (getAllData().size != 0) {
                clearTable()
            }
        }
        update()
    }


    private fun getColumnData(column: Int): MutableList<Int> {
        val data = mutableListOf<Int>()
        val count = binding.tb.childCount
        for (i in 0 until count) {
            val rowItems = binding.tb.getChildAt(i) as TableRow
            val cell = rowItems.getChildAt(column) as TextView
            if (cell.text.isNotEmpty()) {
                data.add(cell.text.toString().toInt())
            }
        }
        return data
    }

    private fun calculate() {
        val columnOne = getColumnData(0)
        val columnTwo = getColumnData(1)
        val columnThree = getColumnData(2)

// ------------------------------------------- Считаем зарплату------------------------------------------------------------
        val salaryText: Int
        val salaryPercent: Int
        var meter = 0


//----------------------------------------Убираем лишний час если указанно больше непрофиля----------------------------------
        for (i in 0 until binding.tb.childCount) {
            val row = binding.tb.getChildAt(i) as? TableRow
            val cell1 = row?.getChildAt(1) as? TextView // assuming first column contains TextViews
            val cell2 = row?.getChildAt(2) as? TextView // assuming second column contains TextViews

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

        if ((columnOne.sum() != 0) && (columnTwo.sum() != 0)) {
            if (columnOne.sum() / columnTwo.sum() >= 80) { // 80 - это план в час(не почасовая оплата)
                salaryText =
                    (((columnOne.sum() * 2.2 + columnTwo.sum() * 110 + columnThree.sum() * 210 - meter * 110) * 1.2 * 0.13).roundToInt())
                salaryPercent =
                    ((((columnOne.sum() * 2.2 + columnTwo.sum() * 110 + columnThree.sum() * 210 - meter * 110) * 1.2) - salaryText).roundToInt())
            } else {
                salaryText =
                    (((columnOne.sum() * 2) + columnTwo.sum() * 110 + columnThree.sum() * 210 - meter * 110) * 1.2 * 0.13).roundToInt()
                salaryPercent =
                    ((((columnOne.sum() * 2) + columnTwo.sum() * 110 + columnThree.sum() * 210 - meter * 110) * 1.2) - salaryText).roundToInt()
            }

        } else {
            salaryText = ((columnThree.sum() * 210 - meter * 110) * 1.2 * 0.13).roundToInt()
            salaryPercent =
                (((columnThree.sum() * 210 - meter * 110) * 1.2) - salaryText).roundToInt()
        }

        salary = salaryPercent

// ------------------------------------------- Считаем зарплату-----------------------------------------

//---------------------------------------------Считаем среднее------------------------------------------
        average = if ((columnOne.sum() != 0)) {
            columnOne.sum() / columnOne.size
        } else {
            0
        }
//---------------------------------------------Считаем среднее------------------------------------------

// --------------------------------------------Считаем смены------------------------------------------
        shifts = 0
        for (i in 0 until binding.tb.childCount) {
            val row = binding.tb.getChildAt(i) as? TableRow
            if (row != null && row.childCount == 3) {
                // Check if both columns have data
                val column1 = (row.getChildAt(0) as? TextView)?.text?.toString()
                val column2 = (row.getChildAt(1) as? TextView)?.text?.toString()
                val column3 = (row.getChildAt(2) as? TextView)?.text?.toString()
                if (!column1.isNullOrEmpty() || !column2.isNullOrEmpty() || !column3.isNullOrEmpty()) {
                    shifts++
                }
            }
        }
// --------------------------------------------Считаем смены------------------------------------------

        positionSum = columnOne.sum()
        shiftHourSum = columnTwo.sum()
        nonProfileHourSum = columnThree.sum()
        update()
    }

    private fun clearTable() {
        val builder = AlertDialog.Builder(this.context)
//      builder.setTitle("Переименовать таблицу")
        builder.setMessage("Вы уверенны, что хотите очистить таблицу?")


        builder.setPositiveButton("Да") { dialog, _ ->
            for (i in 0 until binding.tb.childCount) {
                val rowItems = binding.tb.getChildAt(i) as TableRow
                for (j in 0 until rowItems.childCount) {
                    val cell = rowItems.getChildAt(j) as TextView
                    cell.text = ""
                    binding.textView17.text = "Итог: 0"
                    binding.textView16.text = "0 ч."
                    binding.textView11.text = "0 ч."
                    binding.textView3.text = "Cмен: 0"
                    binding.textView5.text = "Среднее: 0"
                    binding.textView4.text = "Заработано: 0 ₽"


                }
            }
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            dialog.dismiss()
        }

        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveTableData() {
        val editor = sharedPreferences.edit()
        for (i in 0 until binding.tb.childCount) {
            val rowItems = binding.tb.getChildAt(i) as TableRow
            for (j in 0 until rowItems.childCount) {
                val cell = rowItems.getChildAt(j) as TextView
                val value = cell.text.toString()
                editor.putString("cell_$i$j", value)
            }
        }
        editor.apply()
    }

    private fun loadTableData() {
        for (i in 0 until binding.tb.childCount) {
            val rowItems = binding.tb.getChildAt(i) as TableRow
            for (j in 0 until rowItems.childCount) {
                val cell = rowItems.getChildAt(j) as TextView
                val value = sharedPreferences.getString("cell_$i$j", "")
                cell.text = value
            }
        }
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()
        editor.putInt("salary", salary)
        editor.putInt("average", average)
        editor.putInt("shifts", shifts)
        editor.putInt("positionSum", positionSum)
        editor.putInt("shiftHourSum", shiftHourSum)
        editor.putInt("nonProfileHourSum", nonProfileHourSum)
        editor.apply()
    }

    private fun loadData() {
        salary = sharedPreferences.getInt("salary", 0)
        average = sharedPreferences.getInt("average", 0)
        shifts = sharedPreferences.getInt("shifts", 0)
        positionSum = sharedPreferences.getInt("positionSum", 0)
        shiftHourSum = sharedPreferences.getInt("shiftHourSum", 0)
        nonProfileHourSum = sharedPreferences.getInt("nonProfileHourSum", 0)
    }

    private fun update() {
        binding.textView4.text =
            "Заработано: ${String.format(Locale.CANADA_FRENCH, "%,d", salary)} ₽"
        binding.textView5.text = "Среднее: $average"
        binding.textView3.text = "Смен: $shifts"
        binding.textView17.text = "Итог: $positionSum"
        binding.textView16.text = "$shiftHourSum ч."
        binding.textView11.text = "$nonProfileHourSum ч."
        saveData()
        saveTableData()
    }
}
