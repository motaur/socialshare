package com.example.socialshare

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private var shareDialog: ShareDialog = ShareDialog(this)

  private var id = "564701673564676"
  var text = "post text is predefined"
  private var bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ALPHA_8)

  var imagePath = ""

  companion object {
    //image pick code
    private const val IMAGE_PICK_CODE = 1000
    //Permission code
    private const val PERMISSION_CODE = 1001

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
      && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
      && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
    {
      //permission denied
      val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
      //show popup to request runtime permission
      requestPermissions(permissions, PERMISSION_CODE)
    }

    img_pick_btn.setOnClickListener {
      pickImageFromGallery()
    }

    fb_share_btn.setOnClickListener {
      facebookShare()
    }

    insta_share_btn.setOnClickListener {
      instagramShare()
    }
  }

  private fun pickImageFromGallery() {
    //Intent to pick image
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, IMAGE_PICK_CODE)
  }

  //handle requested permission result
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    when(requestCode){
      PERMISSION_CODE -> {
        if (grantResults.isNotEmpty() && grantResults[0] ==
          PackageManager.PERMISSION_GRANTED){
          //permission from popup granted
          pickImageFromGallery()
        }
        else{
          //permission from popup denied
          Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  //handle result of picked image
  @Override
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
      //image_view.setImageURI(data?.data)

      // Let's read picked image data - its URI
      val pickedImage: Uri = data!!.data!!
      // Let's read picked image path using content resolver
      val filePath = arrayOf(MediaStore.Images.Media.DATA)
      val cursor = contentResolver.query(pickedImage, filePath, null, null, null)
      cursor!!.moveToFirst()
      imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]))

      val options = BitmapFactory.Options()
      options.inPreferredConfig = Bitmap.Config.ARGB_8888
      bitmap = BitmapFactory.decodeFile(imagePath, options)

      image_view.setImageBitmap(bitmap)
    }
  }

  private fun facebookShare() {

    try {
      val sharePhoto = SharePhoto.Builder()
        .setBitmap(bitmap)
        .build()

      val content = SharePhotoContent.Builder()
        .addPhoto(sharePhoto)
        .setShareHashtag(
          ShareHashtag.Builder()
            .setHashtag(text)
            .build()
        )
        .setPlaceId(id)
        .build()
      shareDialog.show(content)
    }
    catch (e: Exception){
      Toast.makeText(this, "Looks like Facebook app not installed", Toast.LENGTH_SHORT).show()
    }

  }

  private fun instagramShare() {
    val attributionLinkUrl = "https://developers.facebok.com"

    val shareIntent = Intent("com.instagram.share.ADD_TO_STORY")

    try{

      val path = MediaStore.Images.Media.insertImage(
        contentResolver,
        bitmap,
        "",
        ""
      )

      val uri = Uri.parse(path)
      shareIntent.setDataAndType(uri, "image/*")
      shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
      shareIntent.putExtra("content_url", attributionLinkUrl)

      shareIntent.putExtra("top_background_color", "#33FF33")
      shareIntent.putExtra("bottom_background_color", "#FF00FF")
      grantUriPermission("com.instagram.android", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
      startActivityForResult(shareIntent, 42)
  }
    catch (e: Exception){
      Toast.makeText(this, "Looks like Instagram app Not installed", Toast.LENGTH_SHORT).show()
    }
  }
}


