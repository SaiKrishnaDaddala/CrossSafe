package com.crosssafe.app

import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crosssafe.app.data.PresetRepository
import com.crosssafe.app.databinding.ActivityManagePresetsBinding
import com.crosssafe.app.model.Preset
import java.util.Collections

class ManagePinnedPresetsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagePresetsBinding
    private lateinit var repo: PresetRepository
    private lateinit var adapter: PinnedPresetsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagePresetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage pinned presets"

        repo = PresetRepository(this)
        val pinned = repo.getPinnedPresets().toMutableList()

        adapter = PinnedPresetsAdapter(pinned) { dragHandle, viewHolder ->
            itemTouchHelper.startDrag(viewHolder)
        }

        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                Collections.swap(adapter.items, from, to)
                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val preset = adapter.items[pos]
                repo.unpinPreset(preset.id)
                adapter.items.removeAt(pos)
                adapter.notifyItemRemoved(pos)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                savePinnedOrder()
            }
        }

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerPinned)
        binding.recyclerPinned.layoutManager = LinearLayoutManager(this)
        binding.recyclerPinned.adapter = adapter
    }

    private fun savePinnedOrder() {
        val ids = adapter.items.map { it.id }
        val prefs = getSharedPreferences("crosssafe_prefs", MODE_PRIVATE)
        prefs.edit().putString("pinned_preset_ids", ids.joinToString(",")).apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

class PinnedPresetsAdapter(
    val items: MutableList<Preset>,
    private val onDragStart: (View, RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<PinnedPresetsAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emoji: TextView = itemView.findViewById(R.id.pinnedEmoji)
        val name: TextView = itemView.findViewById(R.id.pinnedName)
        val dragHandle: ImageView = itemView.findViewById(R.id.dragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pinned_preset, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val preset = items[position]
        holder.emoji.text = preset.emoji
        holder.name.text = preset.name
        holder.dragHandle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onDragStart(holder.dragHandle, holder)
            }
            false
        }
    }

    override fun getItemCount() = items.size
}
