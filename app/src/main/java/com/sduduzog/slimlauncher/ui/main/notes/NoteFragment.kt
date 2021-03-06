package com.sduduzog.slimlauncher.ui.main.notes


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.sduduzog.slimlauncher.MainActivity
import com.sduduzog.slimlauncher.Observer
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.data.Note
import com.sduduzog.slimlauncher.ui.main.DoubleClickListener
import kotlinx.android.synthetic.main.note_fragment.*
import java.security.MessageDigest
import java.util.*


class NoteFragment : Fragment(), Observer {

    private lateinit var note: Note
    private lateinit var viewModel: NotesViewModel
    private lateinit var initialDigest: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            note = if (it !=null && it.containsKey("note")) {
                it.get("note") as Note
            } else {
                Note("", -1L)
            }
        }
        initialDigest = hash(note.title + note.body)
        viewModel = ViewModelProviders.of(this).get(NotesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.note_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (note.edited == -1L) {
            editBody()
        } else {
            bodyEditText.visibility = View.GONE
            textBody.visibility = View.VISIBLE
            textBody.text = note.body
            titleEditText.setText(note.title.orEmpty())
            bodyEditText.setText(note.body)
            titleEditText.isEnabled = false
        }
        titleEditText.setOnEditorActionListener { _, _, _ ->
            editBody()
            true
        }
        textBody.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View) {
                titleEditText.isEnabled = true
                editBody()
            }

            override fun onSingleClick(v: View) {
                // Do nothing
            }
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        with(context as MainActivity) {
            this.dispatcher.attachObserver(this@NoteFragment)
        }
    }

    override fun onDetach() {
        super.onDetach()
        with(context as MainActivity) {
            this.dispatcher.detachObserver(this@NoteFragment)
        }
    }

    override fun update(on: String) {
        saveNote()
    }

    private fun editBody() {
        textBody.visibility = View.GONE
        bodyEditText.visibility = View.VISIBLE
        if (bodyEditText.requestFocus()) {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(bodyEditText, InputMethodManager.SHOW_IMPLICIT)
            bodyEditText.setSelection(note.body.length)
        }
    }

    private fun saveNote() {
        val body = bodyEditText.text.toString()
        val title = titleEditText.text.toString()
        val newNote = Note(body, Date().time)
        newNote.title = if (title.isEmpty()) null else title
        newNote.body = body.trim()
        newNote.id = note.id
        val currentDigest = hash(newNote.title + newNote.body)
        if (body.isEmpty()) return
        if (initialDigest == currentDigest) return
        if (note.id == null) viewModel.saveNote(newNote) else viewModel.updateNote(newNote)

    }

    private fun hash(input: String): String {
        val bytes = input.toByteArray(charset("UTF-8"))
        val md = MessageDigest.getInstance("MD5")
        md.update(bytes)
        return String(md.digest())
    }
}
