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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val fragmentList = mutableListOf<TableFragment>()
    private lateinit var tabDataList: MutableList<Pair<String, String>>
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
        tabDataList = mutableListOf()

        val tabCount = getTabCountFromSharedPreferences()
        if (tabCount == 0) {
            val initialFragmentKey = UUID.randomUUID().toString()
            addNewTab(initialFragmentKey)
            saveTabCountToSharedPreferences()
            saveTabDataListToSharedPreferences()
        } else {
            for (i in 0 until tabCount) {
                val fragmentKey = getFragmentKeyFromSharedPreferences(i)
                val tabTitle = getTabTitleFromSharedPreferences(i)
                if (fragmentKey != null && tabTitle != null) {
                    addNewTab(fragmentKey, tabTitle)
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

    private fun addNewTab(fragmentKey: String? = null, tabTitle: String? = null) {
        val newFragment = TableFragment()
        if (fragmentList.isEmpty()) {
            addFragment(newFragment)
            addTabToTabLayout(tabTitle ?: "Таблица")
            showSelectedFragment(0)
        } else {
            currentPosition = fragmentList.size
            addFragment(newFragment)
            addTabToTabLayout(tabTitle ?: "Таблица")
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
        tabDataList.add(Pair(newFragmentKey, tabTitle ?: "Таблица"))
        saveTabCountToSharedPreferences()
        saveTabDataListToSharedPreferences()

        // Передаю ключ для сохранения данных фрагмента в Fragment
        newFragment.setFragmentKeys(newFragment.arguments)
    }

    private fun addTabToTabLayout(tabTitle: String) {
        val tab = tabLayout.newTab()
        tab.text = tabTitle
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

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Переименовать таблицу")

        val input = EditText(this)
        input.setText(currentTitle)
        builder.setView(input)

        builder.setPositiveButton("ОК") { dialog, _ ->
            val newTitle = input.text.toString()
            currentTab?.text = newTitle
            val fragmentKey = fragmentList[position].arguments?.getString("key")
            if (fragmentKey != null) {
                val pair = tabDataList.find { it.first == fragmentKey }
                if (pair != null) {
                    tabDataList.remove(pair)
                    tabDataList.add(position, Pair(fragmentKey, newTitle))
                    saveTabDataListToSharedPreferences()
                }
            }
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
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Таблица будет удалена без возможности восстановления. Продолжить?")
            builder.setPositiveButton("Удалить") { dialog, _ ->
                val fragmentToRemove = fragmentList[position]
                val fragmentKey = fragmentToRemove.arguments?.getString("key")

                // Удаление вкладки из списка и обновление TabLayout
                fragmentList.removeAt(position)
                tabLayout.removeTabAt(position)

                fragmentKey?.let {
                    // Удаление данных вкладки из tabDataList и SharedPreferences
                    removeTabDataByFragmentKey(fragmentKey)
                }

                // Показать предыдущую вкладку после удаления текущей вкладки
                currentPosition = if (currentPosition == position) {
                    if (currentPosition > 0) currentPosition - 1 else 0
                } else {
                    currentPosition
                }

                // Переключение на выбранную вкладку
                showSelectedFragment(currentPosition)

                dialog.dismiss()
            }

            builder.setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }


    private fun removeTabDataByFragmentKey(fragmentKey: String) {
        val index = tabDataList.indexOfFirst { it.first == fragmentKey }
        if (index != -1) {
            tabDataList.removeAt(index)
            saveTabDataListToSharedPreferences()
        }
    }

    private fun saveTabDataListToSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.putInt("tabCount", tabDataList.size)
        for (i in tabDataList.indices) {
            val fragmentKey = tabDataList[i].first
            val tabTitle = tabDataList[i].second
            editor.putString("tabKey$i", fragmentKey)
            editor.putString("tabTitle$i", tabTitle)
            editor.putInt("tabOrder$i", i)
        }
        editor.apply()
    }

    private fun saveTabCountToSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.putInt("tabCount", tabDataList.size)
        editor.apply()
    }


    private fun getTabCountFromSharedPreferences(): Int {
        return sharedPreferences.getInt("tabCount", 0)
    }

    private fun getFragmentKeyFromSharedPreferences(index: Int): String? {
        return sharedPreferences.getString("tabKey$index", null)
    }

    private fun getTabTitleFromSharedPreferences(index: Int): String? {
        return sharedPreferences.getString("tabTitle$index", null)
    }


    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_popup)

        val deleteMenuItem = popupMenu.menu.findItem(R.id.menu_delete)
        deleteMenuItem.isEnabled = tabDataList.size > 1

        if (tabDataList.size == 1)
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