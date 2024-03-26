package com.oxingaxin.todograss.common.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

@Service
class S3Service(private val amazonS3: AmazonS3) {
    @Value("\${aws.s3.bucket-name}")
    private lateinit var bucketName: String

    fun uploadFile(bytes: ByteArray, fileName: String) {
        val inputStream: InputStream = ByteArrayInputStream(bytes)
        val metadata: ObjectMetadata = ObjectMetadata()
        metadata.contentLength = bytes.size.toLong()

        val putObjectRequest = PutObjectRequest(bucketName, fileName, inputStream, metadata)

        amazonS3.putObject(putObjectRequest)
    }
}