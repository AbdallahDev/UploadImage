package com.sarayrah.abdallah.uploadimage

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // Folder path for Firebase Storage.
    private var Storage_Path = "All_Image_Uploads/"

    // Root Database Name for Firebase Database.
    private var Database_Path = "All_Image_Uploads_Database"

    // Creating URI.
    private var FilePathUri: Uri? = null

    // Creating StorageReference and DatabaseReference object.
    private var storageReference: StorageReference? = null
    private var databaseReference: DatabaseReference? = null

    // Image request code for onActivityResult() .
    private var Image_Request_Code = 7

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().reference

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path)

        ButtonChooseImage.setOnClickListener {
            // Creating intent.
            val intent = Intent()

            // Setting intent type as image to select image from phone storage.
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Please Select Image"),
                    Image_Request_Code)
        }

        ButtonUploadImage.setOnClickListener {
            UploadImageFileToFirebaseStorage()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Image_Request_Code) {

            FilePathUri = data?.data

            // Getting selected image into Bitmap.
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, FilePathUri)

            // Setting up bitmap selected image into ImageView.
            ShowImageView?.setImageBitmap(bitmap)

            // After selecting image change choose button above text.
            ButtonChooseImage?.text = "Image Selected"

        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    private fun GetFileExtension(uri: Uri): String {

        val contentResolver = contentResolver

        val mimeTypeMap = MimeTypeMap.getSingleton()

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    private fun UploadImageFileToFirebaseStorage() {

        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {

            // Setting progressDialog Title.
            progressDialog?.setTitle("Image is Uploading...")

            // Showing progressDialog.
            progressDialog?.show()

            // Creating second StorageReference.
            val storageReference2nd = storageReference?.child(Storage_Path +
                    System.currentTimeMillis() + "." + GetFileExtension(FilePathUri!!))

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd?.putFile(FilePathUri!!)?.addOnSuccessListener { taskSnapshot ->
                // Getting image name from EditText and store into string variable.
                val TempImageName = ImageNameEditText?.text.toString().trim()

                // Hiding the progressDialog after done uploading.
                progressDialog?.dismiss()

                // Showing toast message after done uploading.
                Toast.makeText(applicationContext, "Image Uploaded Successfully ",
                        Toast.LENGTH_LONG).show()

                val imageUploadInfo = ImageUploadInfo(TempImageName,
                        taskSnapshot.downloadUrl.toString())

                // Getting image upload ID.
                val ImageUploadId = databaseReference?.push()?.key

                // Adding image upload id s child element into databaseReference.
                databaseReference?.child(ImageUploadId)?.setValue(imageUploadInfo)

            }?.addOnFailureListener { exception ->
                // Hiding the progressDialog.
                progressDialog?.dismiss()

                // Showing exception erro message.
                Toast.makeText(this@MainActivity, exception.message,
                        Toast.LENGTH_LONG)
                        .show()
            }?.addOnProgressListener {
                // Setting progressDialog Title.
                progressDialog?.setTitle("Image is Uploading...")
            }
        } else {
            Toast.makeText(this@MainActivity, "Please Select Image or Add Image Name",
                    Toast.LENGTH_LONG).show()
        }
    }
}
