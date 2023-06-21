package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val fragmentList = mutableListOf<TableFragment>()
    private lateinit var tabIds: MutableList<String>
    private lateinit var tabLayout: TabLayout
    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tabLayout

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentPosition = tab.position
                showSelectedFragment(currentPosition)
                Log.d("Calc", "выбранна вкладка $currentPosition")
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                showPopupMenu(tab.view, tab.position)
            }
        })

        binding.fabAdd.setOnClickListener {
            addNewTab()
        }
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        tabIds = mutableListOf()

        val tabCount = getTabCountFromSharedPreferences()
        if (tabCount == 0) {
            val initialFragmentKey = UUID.randomUUID().toString()
            addNewTab(initialFragmentKey)
            saveTabCountToSharedPreferences()
            saveFragmentKeyToSharedPreferences(0, initialFragmentKey)
        } else {
            for (i in 0 until tabCount) {
                val fragmentKey = getFragmentKeyFromSharedPreferences(i)
                if (fragmentKey != null) {
                    addNewTab(fragmentKey)
                }
            }
        }
    }

    private fun addFragment(fragment: TableFragment) {
        fragmentList.add(fragment)
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    private fun addNewTab(fragmentKey: String? = null) {
        val newFragment = TableFragment()
        if (fragmentList.isEmpty()) {
            addFragment(newFragment)
            addTabToTabLayout(0)
            showSelectedFragment(0)
        } else {
            currentPosition = fragmentList.size
            addFragment(newFragment)
            addTabToTabLayout(currentPosition)
            showSelectedFragment(currentPosition)
            tabLayout.selectTab(tabLayout.getTabAt(currentPosition))
        }

        tabLayout.post {
            tabLayout.setScrollPosition(fragmentList.size - 1, 0f, true)
        }


        val newFragmentKey = fragmentKey ?: UUID.randomUUID().toString()
        newFragment.arguments = Bundle().apply {
            putString("key", newFragmentKey)
        }
        tabIds.add(newFragmentKey)
        saveTabCountToSharedPreferences()
        saveTabIdsToSharedPreferences()

//Передаю ключ для сохранения данных фрагемнта в Fragment

        newFragment.setFragmentKeys(newFragment.arguments)
    }

    private fun addTabToTabLayout(position: Int) {
        val tab = tabLayout.newTab()
        tab.text = "Таблица${position + 1}"
        tabLayout.addTab(tab)
    }

    private fun showSelectedFragment(position: Int) {
        val selectedFragment = fragmentList[position]
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, selectedFragment)
            .commit()
    }

    private fun renameTab(position: Int) {
        val currentTab = tabLayout.getTabAt(position)
        val currentTitle = currentTab?.text.toString()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Переименовать таблицу")

        val input = EditText(this)
        input.setText(currentTitle)
        builder.setView(input)

        builder.setPositiveButton("ОК") { dialog, _ ->
            val newTitle = input.text.toString()
            currentTab?.text = newTitle
            dialog.dismiss()
        }

        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun removeTab(position: Int) {
        if (position >= 0 && position < fragmentList.size) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
//            builder.setTitle("Предупреждение")
            builder.setMessage("Таблица удалится без возможности восстановления. Продолжить?")
            builder.setPositiveButton("Удалить") { dialog, _ ->

                val fragmentToRemove = fragmentList[position]
                val fragmentKey = fragmentToRemove.arguments?.getString("key")
                fragmentList.removeAt(position)
                supportFragmentManager.beginTransaction()
                    .remove(fragmentToRemove)
                    .commit()
                tabLayout.removeTabAt(position)

                removeTabIdFromSharedPreferences(fragmentKey)

                // Обновляем текущую позицию вкладки
                if (currentPosition == position) {
                    // Если удаляется текущая вкладка, переключаемся на предыдущую
                    currentPosition = position - 1
                    if (currentPosition < 0) {
                        currentPosition = 0
                    }
                } else if (currentPosition > position) {
                    // Если удалена вкладка перед текущей, обновляем текущую позицию
                    currentPosition -= 1
                }

                dialog.dismiss()
            }

            builder.setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun saveTabCountToSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.putInt("tabCount", tabIds.size)
        editor.apply()
    }

    private fun saveTabIdsToSharedPreferences() {
        val editor = sharedPreferences.edit()
        for (i in tabIds.indices) {
            editor.putString("tabKey$i", tabIds[i])
        }
        editor.apply()
    }

    private fun saveFragmentKeyToSharedPreferences(index: Int, fragmentKey: String) {
        val editor = sharedPreferences.edit()
        editor.putString("tabKey$index", fragmentKey)
        editor.apply()
    }

    private fun getTabCountFromSharedPreferences(): Int {
        return sharedPreferences.getInt("tabCount", 0)
    }

    private fun getFragmentKeyFromSharedPreferences(index: Int): String? {
        return sharedPreferences.getString("tabKey$index", null)
    }

    private fun removeTabIdFromSharedPreferences(fragmentKey: String?) {
        fragmentKey?.let {
            val editor = sharedPreferences.edit()
            val index = tabIds.indexOf(fragmentKey)
            if (index != -1) {
                editor.remove("tabKey$index")
                tabIds.removeAt(index)

                // Обновляем порядковые номера идентификаторов вкладок
                for (i in index until tabIds.size) {
                    val oldKey = "tabKey${i + 1}"
                    val newKey = "tabKey$i"
                    val value = sharedPreferences.getString(oldKey, null)
                    if (value != null) {
                        editor.putString(newKey, value)
                    }
                    editor.remove(oldKey)
                }

                saveTabIdsToSharedPreferences()
                saveTabCountToSharedPreferences()
            }
            editor.apply()
        }
    }


    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_popup)

        val deleteMenuItem = popupMenu.menu.findItem(R.id.menu_delete)
        deleteMenuItem.isEnabled = fragmentList.size > 1

        if (fragmentList.size == 1)
            deleteMenuItem.icon?.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_rename -> {
                    renameTab(position)
                    true
                }
                R.id.menu_delete -> {
                    removeTab(position)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}





