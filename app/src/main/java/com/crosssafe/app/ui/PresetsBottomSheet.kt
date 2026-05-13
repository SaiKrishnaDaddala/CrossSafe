package com.crosssafe.app.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crosssafe.app.R
import com.crosssafe.app.data.PresetRepository
import com.crosssafe.app.model.Preset
import com.crosssafe.app.model.PresetCategory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout

class PresetsBottomSheet : BottomSheetDialogFragment() {

    private var onPresetSelected: ((Preset) -> Unit)? = null
    private lateinit var repo: PresetRepository
    private var currentCategory: PresetCategory? = null
    private var searchQuery = ""

    fun setOnPresetSelected(callback: (Preset) -> Unit) {
        onPresetSelected = callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_presets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = PresetRepository(requireContext())

        val searchBox = view.findViewById<EditText>(R.id.searchBox)
        val tabLayout = view.findViewById<TabLayout>(R.id.categoryTabs)
        val recycler = view.findViewById<RecyclerView>(R.id.presetsRecycler)

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                updateList(recycler)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tabLayout.addTab(tabLayout.newTab().setText("All"))
        PresetCategory.values().forEach { cat ->
            tabLayout.addTab(tabLayout.newTab().setText(cat.label))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentCategory = if (tab.position == 0) null
                else PresetCategory.values()[tab.position - 1]
                updateList(recycler)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        updateList(recycler)
    }

    private fun updateList(recycler: RecyclerView) {
        val all = repo.getAllPresets()
        val filtered = all.filter { preset ->
            val matchesCategory = currentCategory == null || preset.category == currentCategory
            val matchesSearch = searchQuery.isBlank() || preset.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        recycler.adapter = PresetGridAdapter(filtered, repo) { preset ->
            onPresetSelected?.invoke(preset)
            dismiss()
        }
    }

    companion object {
        fun newInstance() = PresetsBottomSheet()
    }
}

class PresetGridAdapter(
    private val presets: List<Preset>,
    private val repo: PresetRepository,
    private val onSelect: (Preset) -> Unit
) : RecyclerView.Adapter<PresetGridAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emoji: TextView = itemView.findViewById(R.id.cardEmoji)
        val name: TextView = itemView.findViewById(R.id.cardName)
        val pinBtn: TextView = itemView.findViewById(R.id.btnPin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_preset_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val preset = presets[position]
        holder.emoji.text = preset.emoji
        holder.name.text = preset.name
        holder.pinBtn.text = if (preset.isPinned) "★" else "☆"
        holder.itemView.setOnClickListener { onSelect(preset) }
        holder.pinBtn.setOnClickListener {
            if (preset.isPinned) repo.unpinPreset(preset.id)
            else repo.pinPreset(preset.id)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = presets.size
}
