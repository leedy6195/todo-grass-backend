package com.oxingaxin.todograss.common.aws

import com.amazonaws.services.s3.AmazonS3
import com.oxingaxin.todograss.member.domain.dto.MemberUpdateRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class S3Controller {
    @Autowired
    private lateinit var amazonS3: AmazonS3

    @Value("\${aws.s3.bucket-name}")
    private lateinit var bucketName: String

    @GetMapping("/images/{imageName}")
    fun getImage(@PathVariable imageName: String): ResponseEntity<ByteArray> {
        val s3Object = amazonS3.getObject(bucketName, imageName)
        val objectData = s3Object.objectContent.readAllBytes()
        val headers = HttpHeaders().apply {
            contentType = MediaType.IMAGE_JPEG
            contentLength = objectData.size.toLong()
        }
        return ResponseEntity.ok().headers(headers).body(objectData)
    }
}