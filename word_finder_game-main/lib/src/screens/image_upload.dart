import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:wordfinderx/src/common/app_colors.dart';
import 'package:wordfinderx/src/common/app_colors.dart';
import 'package:wordfinderx/src/widgets/cutstom_text_button.dart';

class ImageScreen extends StatefulWidget {
  /// The page name to navigate to.
  static const String pageName = 'ImageScreen';

  /// Returns State object for this widget.
  @override
  _ImageScreenState createState() => _ImageScreenState();
}

/// State class for SearchScreen widget.
class _ImageScreenState extends State<ImageScreen> {
  var height;
  var width;
  XFile? _imageFile;

  @override
  Widget build(BuildContext context) {
    height = MediaQuery.of(context).size.height;
    width = MediaQuery.of(context).size.width;
    return Scaffold(
      appBar: AppBar(),
      // key: _scaffoldKey,
      backgroundColor: AppColors.backgroundColor,
      body: _buildBody(),
    );
  }

  _buildBody() {
    return Container(
      alignment: Alignment.center,
      height: height,
      width: width,
      decoration: BoxDecoration(color: AppColors.secondaryColor),
      child: Column(
        children: [
          _imageFile == null
              ? SizedBox(
                  height: height * 0.6,
                  child: Image.asset(
                    'assets/images/imgPic.png',
                    fit: BoxFit.fill,
                  ))
              : SizedBox(
                  height: height * 0.6,
                  child: Image.file(
                    (File(_imageFile!.path)),
                    fit: BoxFit.cover,
                  ),
                ),
          SizedBox(
            height: 20,
          ),
          CustomTextButton(
            buttonWidth: 80,
            text: 'Select',
            onTap: () {
              showPicker(context);
            },
          ),
        ],
      ),
    );
  }

  void showPicker(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (BuildContext bc) {
        return SafeArea(
          child: Container(
            child: Wrap(
              children: <Widget>[
                ListTile(
                    leading: Icon(Icons.photo_library),
                    title: Text('Photo Library'),
                    onTap: () {
                      imgFromGallery();
                      Navigator.of(context).pop();
                    }),
                ListTile(
                  leading: Icon(Icons.photo_camera),
                  title: Text('Camera'),
                  onTap: () {
                    imgFromCamera();
                    Navigator.of(context).pop();
                  },
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  imgFromGallery() async {
    final ImagePicker _picker = ImagePicker();
    final pickedFile = await _picker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 500,
      maxHeight: 500,
    );
    setState(() {
      if (pickedFile != null) {
        _imageFile = pickedFile;
      }
    });
  }

  imgFromCamera() async {
    final ImagePicker _picker = ImagePicker();
    final pickedFile = await _picker.pickImage(
      source: ImageSource.camera,
      maxWidth: 500,
      maxHeight: 500,
    );

    setState(() {
      if (pickedFile != null) {
        _imageFile = pickedFile;
      }
    });
  }
}
