package com.sarayrah.abdallah.uploadimage

class ImageUploadInfo {

    lateinit var imageName: String

    lateinit var imageURL: String

    constructor() {

    }

    constructor(name: String, url: String) {

        this.imageName = name
        this.imageURL = url
    }

}

