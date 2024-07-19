package com.lelestacia.nutapostest2.ui.tambah_transaksi

import android.Manifest
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lelestacia.nutapostest2.BuildConfig
import com.lelestacia.nutapostest2.R
import com.lelestacia.nutapostest2.databinding.DialogFinanceTypeBinding
import com.lelestacia.nutapostest2.databinding.DialogImageSelectionBinding
import com.lelestacia.nutapostest2.databinding.DialogViewImageBinding
import com.lelestacia.nutapostest2.databinding.FragmentTambahTransaksiBinding
import com.lelestacia.nutapostest2.domain.model.Transaction
import com.lelestacia.nutapostest2.util.updateVisibility
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.navigation.koinNavGraphViewModel
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.util.Date
import java.util.UUID

class TambahTransaksiFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var binding: FragmentTambahTransaksiBinding? = null
    private val vm: TambahTransaksiViewModel by koinNavGraphViewModel(R.id.nav_graph)

    private val galleryContract =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                vm.updateUri(it)
            }
        }

    private val cameraContract =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) {
            if (it) {
                binding?.ivTransactionProof?.load(vm.uiState.value.imageUri)
            } else {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_add_transaction, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_save -> {
                    saveTransaction()
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTambahTransaksiBinding.inflate(inflater, container, false)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            requireActivity().addMenuProvider(menuProvider)
        }
        handleBundle()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener()
        bindView()
    }

    private fun handleBundle() {
        val bundle = arguments
        bundle?.let {
            val transaction =
                if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable(KEY, Transaction::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    it.getParcelable(KEY)
                }

            transaction?.let { validTransaction ->
                binding?.edtReceiver?.setText(validTransaction.receiver)
                binding?.edtSender?.setText(validTransaction.sender)
                binding?.edtValue?.setText(validTransaction.amount.toInt().toString())
                binding?.edtDescription?.setText(validTransaction.description)
                binding?.edtTransactionType?.setText(validTransaction.type)
                vm.insertBitmap(validTransaction.image)
            }
        }
    }

    private fun saveTransaction() = viewLifecycleOwner.lifecycleScope.launch {

        val bundle = arguments
        val transactionBundle: Transaction? =
            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle?.getParcelable(KEY, Transaction::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle?.getParcelable(KEY)
            }

        val bitmap: Bitmap? =
            if (vm.uiState.value.imageBitmap != null) {
                vm.uiState.value.imageBitmap
            } else {
                if (vm.uiState.value.imageUri != Uri.EMPTY) {
                    val inputStream =
                        requireActivity().contentResolver.openInputStream(vm.uiState.value.imageUri)
                    BitmapFactory.decodeStream(inputStream)
                } else {
                    null
                }

            }

        val date = Date()
        val type = binding?.edtTransactionType?.text.toString()


        val transaction = Transaction(
            id = transactionBundle?.id ?: 0,
            receiver = binding?.edtReceiver?.text.toString(),
            sender = binding?.edtSender?.text.toString(),
            amount = binding?.edtValue?.text?.toString()?.toDoubleOrNull() ?: 0.toDouble(),
            description = binding?.edtDescription?.text.toString(),
            type = type.ifBlank { getString(R.string.tv_another_income) },
            image = bitmap,
            date = transactionBundle?.date ?: date.toInstant().toEpochMilli()
        )

        vm.saveTransaction(transaction)

        Toast.makeText(requireContext(), "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()

        binding?.edtReceiver?.setText("")
        binding?.edtSender?.setText("")
        binding?.edtValue?.setText("")
        binding?.edtDescription?.setText("")
        binding?.edtTransactionType?.setText("")
    }

    private fun bindView() = viewLifecycleOwner.lifecycleScope.launch {
        vm.uiState.collectLatest { state ->
            binding?.layoutImageMenu?.updateVisibility(state.isImageMenuVisible)
            binding?.tvSelectImage?.updateVisibility(state.isImagePlaceholderTextVisible)

            if (state.imageBitmap != null) {
                binding?.ivTransactionProof?.setImageBitmap(state.imageBitmap)
            } else {
                binding?.ivTransactionProof?.load(state.imageUri)
            }

            if (state.isImageDialogVisible) {
                val dialogBinding = DialogImageSelectionBinding.inflate(layoutInflater)
                val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                    .setView(dialogBinding.root)
                    .setOnDismissListener {
                        vm.showOrDismissImageDialog(visibility = false)
                    }
                    .show()

                dialogBinding.btnCamera.setOnClickListener {
                    launchCamera()
                    dialog.dismiss()
                }

                dialogBinding.btnGallery.setOnClickListener {
                    launchGallery()
                    dialog.dismiss()
                }
            }

            if (state.isFinanceTypeDialogVisible) {
                val dialogBinding = DialogFinanceTypeBinding.inflate(layoutInflater)
                val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                    .setView(dialogBinding.root)
                    .setOnDismissListener {
                        vm.showOrDismissFinanceTypeDialog(visibility = false)
                    }
                    .show()

                dialogBinding.tvNonPendapatan.setOnClickListener {
                    binding?.edtTransactionType?.setText((it as TextView).text.toString())
                    dialog.dismiss()
                }

                dialogBinding.tvPendapatanLainnya.setOnClickListener {
                    binding?.edtTransactionType?.setText((it as TextView).text.toString())
                    dialog.dismiss()
                }
            }

            if (state.isInformationSheetVisible) {
                val sheet = BottomSheetDialog(requireContext())

                sheet.setContentView(R.layout.fragment_transaction_information)
                sheet.setOnDismissListener {
                    vm.showOrDismissSheetInformation(visibility = false)
                }

                sheet.show()
            }
        }
    }

    private fun setOnClickListener() {
        binding?.edtLayoutTransactionType?.setEndIconOnClickListener {
            vm.showOrDismissFinanceTypeDialog(visibility = true)
        }

        binding?.tvMore?.setOnClickListener {
            vm.showOrDismissSheetInformation(true)
        }

        binding?.cvTransactionProof?.setOnClickListener {
            if (vm.uiState.value.imageUri != Uri.EMPTY || vm.uiState.value.imageBitmap != null) {
                val dialogBinding =
                    DialogViewImageBinding.inflate(LayoutInflater.from(context))

                if (vm.uiState.value.imageBitmap != null) {
                    dialogBinding.root.setImageBitmap(vm.uiState.value.imageBitmap)
                } else {
                    dialogBinding.root.setImageURI(vm.uiState.value.imageUri)
                }

                MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                    .setView(dialogBinding.root)
                    .show()
            } else {
                vm.showOrDismissImageDialog(true)
            }
        }

        binding?.btnChangeImage?.setOnClickListener {
            vm.showOrDismissImageDialog(true)
        }

        binding?.btnDeleteImage?.setOnClickListener {
            vm.updateUri(Uri.EMPTY)
        }

        binding?.btnSave?.setOnClickListener {
            saveTransaction()
        }
    }

    private fun launchCamera() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.CAMERA
            )
        ) {
            viewLifecycleOwner.lifecycleScope.launch {
                val photoFile = File(requireContext().externalCacheDir, "${UUID.randomUUID()}.jpg")

                val uri: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${BuildConfig.APPLICATION_ID}.provider",
                    photoFile
                )

                cameraContract.launch(uri)
                vm.updateUri(uri)
            }
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this,
                    CAMERA_CODE,
                    Manifest.permission.CAMERA
                ).build()
            )
        }
    }

    private fun launchGallery() {

        if (VERSION.SDK_INT <= 32) {
            if (
                EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                galleryContract.launch("image/*")
            } else {
                EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(
                        this,
                        GALLERY_CODE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ).build()
                )
            }
        } else {
            if (
                EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            ) {
                galleryContract.launch("image/*")
            } else {
                EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(
                        this,
                        GALLERY_CODE,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ).build()
                )
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            GALLERY_CODE -> launchGallery()
            CAMERA_CODE -> launchCamera()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        vm.resetState()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            requireActivity().removeMenuProvider(menuProvider)
        }
    }

    companion object {
        private const val CAMERA_CODE = 100
        private const val GALLERY_CODE = 101
        const val KEY = "data"
    }
}