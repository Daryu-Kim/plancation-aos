package com.daryukim.plancation

import android.annotation.SuppressLint
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TodoItemAdapter(
  private val todoItemList: List<ScheduleModel>,
) :
  RecyclerView.Adapter<TodoItemAdapter.TodoItemViewHolder>() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var todoItem: ScheduleModel
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
    return TodoItemViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return todoItemList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: TodoItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
    todoItem = todoItemList[position]

    holder.itemCheckBox.isChecked = todoItem.eventCheckUsers.contains(auth.currentUser?.uid)
    holder.itemTitle.text = todoItem.eventTitle
    holder.itemCount.text = "${todoItem.eventCheckUsers.size} / ${todoItem.eventUsers.size}"
    holder.itemCheckBox.setOnClickListener {
      modifyUserChecked(holder, holder.itemCheckBox.isChecked, position)
    }
  }

  private fun modifyUserChecked(holder: TodoItemViewHolder, isChecked: Boolean, position: Int) {
    val eventTodoRef = db.collection("Calendars")
      .document("A9PHFsmDLUWbaYDdy2XX")
      .collection("Events")
      .document(todoItem.eventID)

    eventTodoRef
      .update("eventCheckUsers", if (isChecked) FieldValue.arrayUnion(auth.currentUser?.uid) else FieldValue.arrayRemove(auth.currentUser?.uid))
      .addOnSuccessListener { _ ->
        readDocument(holder, eventTodoRef, position)
      }
      .addOnFailureListener { _ ->
        Toast.makeText(holder.itemView.context, "체크 처리에 실패했습니다!", Toast.LENGTH_SHORT).show()
        holder.itemCheckBox.isChecked = !isChecked
      }
  }

  @SuppressLint("SetTextI18n")
  private fun readDocument(holder: TodoItemViewHolder, ref: DocumentReference, position: Int) {
    ref.get()
      .addOnSuccessListener { document ->
        if (document != null) {
          todoItem = document.data?.let { ScheduleModel.fromDocument(it) }!!
          holder.itemCount.text = "${todoItem.eventCheckUsers.size} / ${todoItem.eventUsers.size}"
        }
      }
      .addOnFailureListener { exception ->
        // 에러가 발생했습니다.
      }
  }

  inner class TodoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnCreateContextMenuListener {
    val itemLayout: View = itemView.findViewById(R.id.todo_item_layout)
    val itemTitle: TextView = itemView.findViewById(R.id.todo_item_title)
    val itemTime: TextView = itemView.findViewById(R.id.todo_item_time)
    val itemCheckBox: CheckBox = itemView.findViewById(R.id.todo_item_checkbox)
    val itemCount: TextView = itemView.findViewById(R.id.todo_item_count)

    init {
      itemLayout.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
      menu?.add(adapterPosition, 0, 0 ,"참여자 목록")
      menu?.add(adapterPosition, 1, 1 ,"삭제")
    }
  }
}