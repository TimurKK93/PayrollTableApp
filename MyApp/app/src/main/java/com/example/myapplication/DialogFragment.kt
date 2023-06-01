//package com.example.myapplication
//
//import android.annotation.SuppressLint
//import android.app.Dialog
//import android.content.Context
//import android.os.Bundle
//import android.view.View
//import android.widget.TextView
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatDialogFragment
//
//
//class ExampleDialog : AppCompatDialogFragment() {
//    private var listener: ExampleDialogListener? = null
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val inflater = requireActivity().layoutInflater
//        val view: View = inflater.inflate(R.layout.fragment_dialog, null)
//        val editTextSalary = view.findViewById<TextView>(R.id.edit_salary)
//        val editTextShiftCount = view.findViewById<TextView>(R.id.edit_shift_count)
//
//        val pref = requireActivity().getSharedPreferences("APP", Context.MODE_PRIVATE)
//        editTextSalary.text = pref?.getString("salary_plan_text", "")!!
//        editTextShiftCount.text = pref.getString("shift_plan_text", "")!!
//        editTextSalary.isEnabled = pref.getBoolean("salary_disabled", true)
//        editTextShiftCount.isEnabled = pref.getBoolean("shift_disabled", true)
//
//        return AlertDialog.Builder(requireActivity()).setView(view)
//            .setNegativeButton("Закрыть") { _, _ -> }.setPositiveButton("Ok") { _, _ ->
//                val salary = editTextSalary.text.toString()
//                val shiftCount = editTextShiftCount.text.toString()
//
//                if (salary != "" && shiftCount != "") {
//                    listener!!.applyTexts(salary, shiftCount)
//                }
//            }.setNeutralButton("Сбросить план") { _, _ ->
//                listener!!.applyClearText(clear = "")
//
//            }.create()
//
//
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        listener = try {
//            context as ExampleDialogListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException(
//                context.toString() + "must implement ExampleDialogListener"
//            )
//        }
//
//    }
//
//    interface ExampleDialogListener {
//        fun applyTexts(salary: String?, shiftCount: String?)
//        fun applyClearText(clear: String?)
//
//    }
//}
