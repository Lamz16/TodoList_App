package com.lamz.todolistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lamz.todolistapp.data.TodoItem


class TodoAdapter(private val empList: ArrayList<TodoItem>) : ListAdapter<TodoItem,TodoAdapter.ViewHolder>(
    DIFF_CALLBACK) {

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onCheckboxChanged(position: Int, isChecked: Boolean)
    }

    fun setOnItemClickListener(clickListener: OnItemClickListener){
        mListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.todo_items, parent, false)
        return ViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTodoItem = empList[position]
        holder.tvTitle.text = currentTodoItem.title
        holder.tvDetail.text = currentTodoItem.detail
        val isComplete = currentTodoItem.completed
        holder.isComplete.isChecked = !(isComplete =="no" || isComplete=="")

        holder.isComplete.setOnCheckedChangeListener { _, isChecked ->
            mListener.onCheckboxChanged(position, isChecked)
        }

    }

    override fun getItemCount(): Int {
        return empList.size
    }

    class ViewHolder(itemView: View, clickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val tvTitle : TextView = itemView.findViewById(R.id.titleTask)
        val tvDetail : TextView = itemView.findViewById(R.id.detailTask)
        val isComplete : CheckBox = itemView.findViewById(R.id.cekList)

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }

        }

    }


    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TodoItem>() {
            override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem == newItem
            }
        }

        private const val TAG = "ListProfileAdapter"
    }

}
