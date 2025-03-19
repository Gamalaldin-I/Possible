package com.example.possible.ui.profile.children

import DialogBuilder
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.possible.databinding.ActivityChildrenBinding
import com.example.possible.model.Child
import com.example.possible.repo.local.SharedPref
import com.example.possible.repo.local.database.LocalRepoImp
import com.example.possible.ui.MainActivity
import com.example.possible.ui.profile.children.addChild.AddChildActivity
import com.example.possible.util.adapter.ChildrenAdapter
import com.example.possible.util.listener.ChildListener

class Children : AppCompatActivity(), ChildListener {
    private var deletedPos = -1
    private lateinit var db: LocalRepoImp
    private lateinit var binding: ActivityChildrenBinding
    private lateinit var childrenList: ArrayList<Child>
    private lateinit var adapter: ChildrenAdapter
    private var mode = ""
    private lateinit var pref: SharedPref
    private lateinit var viewModel: ChildrenViewModel

    private val perReqCode = 100

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildrenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        initializeComponents()
        checkAndRequestPermissions()
        observeViewModel { setUpAdapter() }
        observeRemote()
        mode = intent.getStringExtra("mode")!!
        controlForViewMode()
        binding.backArrowIV.setOnClickListener { finish() }
        binding.addChild.setOnClickListener {
            val intent = Intent(this, AddChildActivity::class.java)
            intent.putExtra("mode", "add")
            startActivity(intent)
        }
        specialistView()
    }

    private fun initializeComponents() {
        db = LocalRepoImp(this)
        pref = SharedPref(this)
        viewModel = ViewModelProvider(this)[ChildrenViewModel::class.java]

        if (pref.getCounter() == 0) {
            if(pref.getRole()=="User")

            {viewModel.getChildrenFromApi(pref)}
            else
            { viewModel.getAllChildrenFromApi(pref) }

            pref.setCounter(1)
        } else {
            viewModel.getChildren(db)
        }
    }

    private fun observeViewModel(onEnd: () -> Unit) {
        viewModel.children.observe(this) {
            childrenList = it as ArrayList<Child>
            if (childrenList.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.hint.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.hint.visibility = View.GONE
                if (deletedPos != -1) {
                    adapter.onDelete(deletedPos, childrenList)
                    deletedPos = -1
                }
                else{
                onEnd()}
            }
        }
    }
    private fun observeRemote(){
        viewModel.childrenRemote.observe(this) {
            viewModel.storeUserChildrenIntoRoom(it, db, this)
        }
        viewModel.allChildren.observe(this) {
            viewModel.storeAllChildrenIntoRoom(it, db, this)
        }
    }






    private fun setUpAdapter() {
        adapter = ChildrenAdapter(childrenList, this, pref)
        binding.recyclerView.adapter = adapter
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_IMAGES)
        } else {
            arrayOf(READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(this, permissions, perReqCode)
    }

    override fun onDelete(position: Int, child: Child) {
        deletedPos = position
        DialogBuilder.showAlertDialog(this, "Delete Child",
            "Are you sure you want to delete this child?",
            "Yes", "No",
            {
                viewModel.deleteChild({
                    binding.loadingView.visibility = View.VISIBLE
                }, child.id, {
                    viewModel.getChildren(db)
                    binding.loadingView.visibility = View.GONE
                }, db, pref)
            }, {})
    }



    override fun onResume() {
        super.onResume()
        viewModel.getChildren(db)
    }

    override fun onClick(child: Child) {
        if(mode=="select"){
            DialogBuilder.showAlertDialog(this,
                "Select this Child for Training",
                "Hello!"
                ,"Yes",
                "No",{
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("childId", child.id)
                    startActivity(intent)
                },{})

        }
        else{
            // Display the URI when an item is clicked
            val intent = Intent(this, AddChildActivity::class.java)
            intent.putExtra("mode", "edit")
            intent.putExtra("childId", child.id)
            startActivity(intent)

        }
    }
    private fun controlForViewMode(){
        if(mode=="select"){
            binding.backArrowIV.visibility=View.GONE
            binding.hintOfSelection.visibility=View.VISIBLE
        }
        else{
            binding.backArrowIV.visibility=View.VISIBLE
            binding.hintOfSelection.visibility=View.GONE
        }
    }
    private fun specialistView(){
        if(pref.getRole()=="Specialist"){
        binding.addChild.visibility=View.GONE}
        else{
            binding.addChild.visibility=View.VISIBLE
        }
    }
}
